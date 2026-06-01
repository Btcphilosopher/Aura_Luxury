package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AuraRepository(
    private val dao: AuraDao,
    private val context: Context
) {
    // Expose flows directly from Room DAO
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allReservations: Flow<List<ReservationEntity>> = dao.getAllReservations()
    val vipProfile: Flow<VipProfileEntity?> = dao.getVipProfile()
    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Initialize database default values if empty
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val existingProducts = dao.getAllProducts().firstOrNull() ?: emptyList()
        if (existingProducts.isEmpty()) {
            Log.d("AuraRepository", "Pre-populating luxury flagship catalog...")
            dao.insertProducts(getPreloadedProducts())
        }

        val existingProfile = dao.getVipProfile().firstOrNull()
        if (existingProfile == null) {
            Log.d("AuraRepository", "Initializing Elite VIP Profile for Eleanor Vance...")
            dao.insertVipProfile(
                VipProfileEntity(
                    id = "vip_current_user",
                    name = "Thomas Vance",
                    email = "tom@ahyx.org",
                    membershipTier = "Private Client",
                    points = 24500,
                    personalShopperName = "Arthur Pendleton",
                    stylePreferences = "Classic Tailoring, Bespoke Suede, Minimalist Earth Tones",
                    brandAffinity = "Rolex, Cartier, Hermes, Yves Saint Laurent, Baccarat",
                    visitHistory = "Paris Flagship (May 2026), London Salon (April 2026), Milan Atelier (Feb 2026)"
                )
            )
        }
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return dao.searchProducts("%$query%")
    }

    suspend fun getProductById(id: String): ProductEntity? {
        return dao.getProductById(id)
    }

    suspend fun makeReservation(
        productId: String,
        size: String,
        quantity: Int,
        storeName: String,
        pickupTime: String,
        notes: String
    ): Boolean = withContext(Dispatchers.IO) {
        val product = dao.getProductById(productId) ?: return@withContext false
        val reservation = ReservationEntity(
            productId = productId,
            productName = product.name,
            brand = product.brand,
            size = size,
            quantity = quantity,
            storeName = storeName,
            pickupTime = pickupTime,
            status = "Confirmed"
        )
        dao.insertReservation(reservation)
        // Deduct in-store stock locally
        val updatedProduct = product.copy(stockInStore = (product.stockInStore - quantity).coerceAtLeast(0))
        dao.insertProducts(listOf(updatedProduct))
        return@withContext true
    }

    suspend fun cancelReservation(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteReservationById(id)
    }

    suspend fun bookAppointment(
        date: String,
        time: String,
        category: String,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val appointment = AppointmentEntity(
            date = date,
            time = time,
            shopperName = "Arthur Pendleton (Private Client Lead)",
            categoryPreference = category,
            notes = notes,
            status = "Confirmed"
        )
        dao.insertAppointment(appointment)
        // Award points for engagement
        val profile = dao.getVipProfile().firstOrNull()
        if (profile != null) {
            dao.insertVipProfile(profile.copy(points = profile.points + 500))
        }
    }

    suspend fun updateVipPoints(pointsChange: Int) = withContext(Dispatchers.IO) {
        val profile = dao.getVipProfile().firstOrNull()
        if (profile != null) {
            dao.insertVipProfile(profile.copy(points = profile.points + pointsChange))
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearChatMessages()
    }

    // Call Gemini API Option B (Direct REST API) as recommended for prototypes in Gemini Skill
    suspend fun sendChatMessage(userContent: String): String = withContext(Dispatchers.IO) {
        // Save user message to database
        val userMsg = ChatMessageEntity(role = "user", content = userContent)
        dao.insertChatMessage(userMsg)

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val fallback = getOfflineConciergeResponse(userContent)
            dao.insertChatMessage(ChatMessageEntity(role = "model", content = fallback))
            return@withContext fallback
        }

        // Get past message history out of database to pass to Gemini
        val history = dao.getAllChatMessages().firstOrNull() ?: emptyList()
        val formattedPrompt = buildString {
            append("You are Arthur Pendleton, the elite Private Client Concierge at 'Aura', the luxury multi-brand department store. ")
            append("The user interacting with you is Thomas Vance, a 'Private Client' tier member (email: tom@ahyx.org) who prefers classic tailoring, bespoke suede, minimalist earth tones. ")
            append("Aura features elite curated brands such as Rolex, Cartier, Hermes, Yves Saint Laurent, Gucci, and Baccarat. ")
            append("Reply elegantly, with extreme luxury service tone. Provide tailored suggestions or coordinates with our floor levels: ")
            append("Floor 1: Haute Perfumery, Fine Tea and Confectionery. ")
            append("Floor 2: French & Italian Accessory Salons, Silk Gallery, Men's Couture Collection. ")
            append("Floor 3: Fine Timepieces Gallery, Exquisite Gems Salon. ")
            append("Floor 4: Maison & Living. ")
            append("Keep answers sophisticated, refined, and succinct. Do not use markdown styling headers that look ugly in chat bubbles. Keep lists styled elegantly.\n\n")
            
            history.takeLast(10).forEach { msg ->
                append(if (msg.role == "user") "Thomas: " else "Arthur: ")
                append(msg.content)
                append("\n")
            }
            append("Arthur: ")
        }

        try {
            val requestBodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", formattedPrompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e("AuraRepository", "Gemini API error: Status ${response.code} $errorBody")
                    val fallback = getOfflineConciergeResponse(userContent)
                    dao.insertChatMessage(ChatMessageEntity(role = "model", content = fallback))
                    return@withContext fallback
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val textResponse = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                val modelAnswer = textResponse?.trim() ?: "I am at your absolute service, Mr. Vance. How may I assist you in our salons today?"
                dao.insertChatMessage(ChatMessageEntity(role = "model", content = modelAnswer))
                return@withContext modelAnswer
            }
        } catch (e: Exception) {
            Log.e("AuraRepository", "Exception calling Gemini: ${e.message}", e)
            val fallback = getOfflineConciergeResponse(userContent)
            dao.insertChatMessage(ChatMessageEntity(role = "model", content = fallback))
            return@withContext fallback
        }
    }

    private fun getOfflineConciergeResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("suit") || q.contains("tailor") || q.contains("wear") || q.contains("fashion") -> {
                "For classic tailoring, Mr. Vance, I highly recommend our double-breasted virgin wool jackets on Floor 2, Men's Couture Collection. I would be delighted to coordinate with a stylist in our private salon."
            }
            q.contains("watch") || q.contains("rolex") || q.contains("time") -> {
                "Our Rolex collections are located in the Fine Timepieces Gallery on Floor 3. Currently, the Submariner Date is available for Private Client viewing. Shall I secure an hour for you?"
            }
            q.contains("gift") || q.contains("home") || q.contains("glass") || q.contains("baccarat") -> {
                "The Baccarat crystal decanter collections are exquisitely presented on Floor 4, Maison & Living. It pairs perfectly with the Golden Dragon Oolong tea from our Tea Salon on Floor 1."
            }
            q.contains("appointment") || q.contains("book") || q.contains("shop") -> {
                "Indeed, Mr. Vance. You can reserve an appointment directly inside our 'Concierge' interface. Your dedicated shopper Arthur Pendleton will be awaiting your arrival."
            }
            else -> {
                "It is our absolute privilege to guide your digital shopping journey at Aura, Mr. Vance. Please let me know how I can tailor your next visit."
            }
        }
    }

    private fun getPreloadedProducts(): List<ProductEntity> {
        return listOf(
            ProductEntity(
                id = "gucci_loafers_1",
                brand = "Gucci",
                name = "Leather Horsebit Loafers",
                category = "Footwear",
                price = 850.00,
                imageUrl = "https://images.unsplash.com/photo-1614252369475-531eba835eb1?q=80&w=800&auto=format&fit=crop",
                description = "Gucci's signature loafer featuring the iconic metal horsebit hardware. Masterfully crafted from rich full-grain leather in Florence, Italy, providing unparalleled timeless comfort and style.",
                isExclusive = true,
                isEarlyAccess = false,
                floor = 2,
                section = "Accessory & Footwear Salon",
                stockOnline = 24,
                stockInStore = 5,
                sizeOptions = "EU 41,EU 42,EU 43,EU 44"
            ),
            ProductEntity(
                id = "rolex_submariner_2",
                brand = "Rolex",
                name = "Submariner Date 41mm",
                category = "Watches",
                price = 10500.00,
                imageUrl = "https://images.unsplash.com/photo-1547996160-81dfa63595aa?q=80&w=800&auto=format&fit=crop",
                description = "The ultimate reference in driver watches. Crafted in iconic resilient Oystersteel, featuring a deep black dial with highly legible luminescent hour markers and a unidirectional rotatable Cerachrom bezel.",
                isExclusive = true,
                isEarlyAccess = true,
                floor = 3,
                section = "Fine Timepieces Gallery",
                stockOnline = 2,
                stockInStore = 1,
                sizeOptions = "41mm Case"
            ),
            ProductEntity(
                id = "chanel_no5_3",
                brand = "Chanel",
                name = "No. 5 L'Eau Eau de Parfum",
                category = "Beauty & Fragrance",
                price = 165.00,
                imageUrl = "https://images.unsplash.com/photo-1541643600914-78b084683601?q=80&w=800&auto=format&fit=crop",
                description = "The absolute epitome of femininity. A powder-soft floral aldehyde bouquet nested within an iconic faceted glass flacon, representing centuries of French haute perfumery.",
                isExclusive = false,
                isEarlyAccess = false,
                floor = 1,
                section = "Haute Perfumery",
                stockOnline = 50,
                stockInStore = 15,
                sizeOptions = "50 ml,100 ml"
            ),
            ProductEntity(
                id = "cartier_love_4",
                brand = "Cartier",
                name = "Love Bracelet 18K Yellow Gold",
                category = "Jewellery",
                price = 7350.00,
                imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?q=80&w=800&auto=format&fit=crop",
                description = "A universal symbol of love and commitment. Created in New York in 1969, the oval-shaped bracelet features secure functional screw details to signify everlasting bond.",
                isExclusive = true,
                isEarlyAccess = false,
                floor = 3,
                section = "Exquisite Gems Salon",
                stockOnline = 8,
                stockInStore = 2,
                sizeOptions = "16 cm,17 cm,18 cm"
            ),
            ProductEntity(
                id = "baccarat_decanter_5",
                brand = "Baccarat",
                name = "Harcourt 1841 Crystal Decanter",
                category = "Home & Lifestyle",
                price = 1250.00,
                imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?q=80&w=800&auto=format&fit=crop",
                description = "Created in 1841, the Harcourt clear-crystal decanter is the choice of historical sovereigns and popes. Showcases classical octagonal lines and a jewel-like geometric stopper.",
                isExclusive = false,
                isEarlyAccess = false,
                floor = 4,
                section = "Maison & Living",
                stockOnline = 4,
                stockInStore = 2,
                sizeOptions = "Standard"
            ),
            ProductEntity(
                id = "ysl_coat_6",
                brand = "Yves Saint Laurent",
                name = "Double-Breasted Wool Overcoat",
                category = "Fashion",
                price = 3800.00,
                imageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=800&auto=format&fit=crop",
                description = "An exquisite heavy overcoat tailored elegantly in luxury virgin wool. Features perfectly padded shoulders, six-button closure, and deep charcoal lining for contemporary comfort.",
                isExclusive = true,
                isEarlyAccess = false,
                floor = 2,
                section = "Men's Couture Collection",
                stockOnline = 6,
                stockInStore = 1,
                sizeOptions = "IT 48,IT 50,IT 52"
            ),
            ProductEntity(
                id = "fm_tea_7",
                brand = "Fortnum & Mason",
                name = "Golden Dragon Oolong Caddy",
                category = "Food & Gifts",
                price = 75.00,
                imageUrl = "https://images.unsplash.com/photo-1597481499750-3e6b22637e12?q=80&w=800&auto=format&fit=crop",
                description = "Extremely rare and meticulously hand-rolled loose leaf tea sourced from pristine high-altitude organic farms. Beautifully packaged inside the iconic signature Fortnum Eau-de-Nil tin.",
                isExclusive = false,
                isEarlyAccess = false,
                floor = 1,
                section = "Fine Tea and Confectionery",
                stockOnline = 120,
                stockInStore = 42,
                sizeOptions = "250g Loose Tea"
            ),
            ProductEntity(
                id = "hermes_scarf_8",
                brand = "Hermès",
                name = "Grand Apparat Silk Twill Scarf",
                category = "Jewellery & Accessories",
                price = 495.00,
                imageUrl = "https://images.unsplash.com/photo-1583209814683-c023dd293cc6?q=80&w=800&auto=format&fit=crop",
                description = "Crafted of premium 100% silk twill with artisan hand-rolled hems. Featuring historical equestrian illustrations with detailed gold ornaments and classic navy borders.",
                isExclusive = false,
                isEarlyAccess = false,
                floor = 2,
                section = "Silk Gallery",
                stockOnline = 15,
                stockInStore = 4,
                sizeOptions = "90cm x 90cm"
            )
        )
    }
}

package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuraRepository
import com.example.data.local.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class EditorialStory(
    val id: String,
    val title: String,
    val subtitle: String,
    val intro: String,
    val imageUrl: String,
    val accentColorHex: String,
    val featuredProductIds: List<String>
)

data class CollectionTab(val title: String, val category: String)

sealed interface ConciergeChatState {
    object Idle : ConciergeChatState
    object Loading : ConciergeChatState
    data class Success(val response: String) : ConciergeChatState
    data class Error(val message: String) : ConciergeChatState
}

class AuraViewModel(private val repository: AuraRepository) : ViewModel() {

    // Initialize database defaults on startup
    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
        }
    }

    // Filter and search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Combined filtered products
    val products: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory
    ) { all, query, cat ->
        var list = all
        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.brand.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
        if (cat != "All") {
            list = list.filter {
                it.category.contains(cat, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Direct access flows
    val reservations: StateFlow<List<ReservationEntity>> = repository.allReservations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vipProfile: StateFlow<VipProfileEntity?> = repository.vipProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat State
    private val _chatState = MutableStateFlow<ConciergeChatState>(ConciergeChatState.Idle)
    val chatState: StateFlow<ConciergeChatState> = _chatState.asStateFlow()

    // Detailed Product Overlays
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // Reservation Management
    fun reserveItem(
        productId: String,
        size: String,
        quantity: Int,
        storeName: String,
        pickupTime: String,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.makeReservation(productId, size, quantity, storeName, pickupTime, notes)
            if (success) {
                repository.updateVipPoints(200)
            }
            onComplete(success)
        }
    }

    fun cancelReservation(id: Int) {
        viewModelScope.launch {
            repository.cancelReservation(id)
        }
    }

    // Appointment Booking
    fun bookAppointment(date: String, time: String, category: String, notes: String) {
        viewModelScope.launch {
            repository.bookAppointment(date, time, category, notes)
        }
    }

    // Send Concierge Chat Message
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _chatState.value = ConciergeChatState.Loading
            try {
                repository.sendChatMessage(text)
                _chatState.value = ConciergeChatState.Idle
            } catch (e: Exception) {
                _chatState.value = ConciergeChatState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // High fashion curated magazine editorials
    val editorialStories = listOf(
        EditorialStory(
            id = "story_1",
            title = "A Study in Monochromatic Grace",
            subtitle = "Sartorial precision from Saint Laurent tailors our transition into winter nights.",
            intro = "In our latest editorial, we explore the stark architectural silhouettes crafted under the Saint Laurent label. Dominated by luxurious heavy virgin wool double-breasted overcoats paired with high-concept fragrances, the modern silhouette emerges confident, grounded, and unapologetically distinct.",
            imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=800&auto=format&fit=crop",
            accentColorHex = "3E3E3E",
            featuredProductIds = listOf("ysl_coat_6", "chanel_no5_3")
        ),
        EditorialStory(
            id = "story_2",
            title = "A Golden Standard",
            subtitle = "Uncovering the eternal bond represented by Cartier and Rolex.",
            intro = "Timelessness isn't a duration; it's a statement. This season, our jewelry editors pair the pure structural curves of the Cartier 18K yellow gold Love bracelet with the highly legibly illuminated dial of the Rolex Submariner. A testament to technical perfection and passionate commitment.",
            imageUrl = "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?q=80&w=800&auto=format&fit=crop",
            accentColorHex = "D4AF37",
            featuredProductIds = listOf("cartier_love_4", "rolex_submariner_2")
        ),
        EditorialStory(
            id = "story_3",
            title = "The Sanctum of Home",
            subtitle = "Crafting quiet grandeur in living quarters with Baccarat and Fortnum.",
            intro = "True luxury doesn't shouting from hangers; it whispers from the sideboards. We spotlight the clear, geometric light-reflecting crystal lines of the Harcourt 1841 decanter beside the earthy, high-altitude hand-crafted tea blends curated by Fortnum & Mason. Elevate daily rituals into royal receptions.",
            imageUrl = "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?q=80&w=800&auto=format&fit=crop",
            accentColorHex = "C5A387",
            featuredProductIds = listOf("baccarat_decanter_5", "fm_tea_7")
        )
    )

    // Store floors coordinate guide data
    val departmentDirectory = listOf(
        DepartmentGuide(1, "The Grand Atrium", "Haute Perfumery, Fine Tea Salon, Confectionery & Reception Desk"),
        DepartmentGuide(2, "The Fashion Atelier", "French & Italian Accessory Salons, Silk Gallery, Men's Couture Collection"),
        DepartmentGuide(3, "The Exquisite Pavilions", "Fine Timepieces Gallery (Rolex, Patek Philippe), Gems & Jewellery Court"),
        DepartmentGuide(4, "The Maison Sanctuary", "Luxury Crystal & Decanter Collections, Living Salons, Private Shopping Suites")
    )
}

data class DepartmentGuide(
    val floor: Int,
    val name: String,
    val descriptions: String
)

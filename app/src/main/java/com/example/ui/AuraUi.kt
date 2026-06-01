package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.AppointmentEntity
import com.example.data.local.ProductEntity
import com.example.data.local.ReservationEntity
import com.example.data.local.VipProfileEntity

// Luxury Aesthetic Colors - Artistic Flair Light-Warm Stone & Deep Charcoal Theme
val GoldAccent = Color(0xFFD97706)  // Rich Warm Amber Gold
val GoldenSun = Color(0xFFFBBF24)   // Bright Amber Glow
val OnyxBlack = Color(0xFFF9F8F6)   // Warm Light Stone base background (replaces pitch black)
val DarkGrey80 = Color(0xFFFFFFFF)  // Pristine white surface container backgrounds
val BorderGrey = Color(0xFFE7E5E4)  // Refined Stone-200 subtle border lines
val OffWhite = Color(0xFF57534E)    // Stone-600 elegant text for subtext and parameters
val PureWhite = Color(0xFF1C1B17)   // Deep Charcoal primary headers and bold texts (replaces pure white)
val MutedGold = Color(0xFF78716C)   // Stone-500 refined subheaders and inactive icons

// Premium UI Component representing the overall Aura Flagship Experience
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraAppContent(viewModel: AuraViewModel) {
    var currentTab by remember { mutableStateOf("Editorial") }
    val profile by viewModel.vipProfile.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()

    // Base Scaffold using the luxury dark themes
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "A U R A",
                            color = GoldAccent,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 6.sp,
                            modifier = Modifier.testTag("app_title")
                        )
                        Text(
                            text = "FLAGSHIP DIGITAL ATELIER",
                            color = MutedGold,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentTab = "VIP" },
                        modifier = Modifier.testTag("vip_profile_badge")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP Account Lounge",
                            tint = if (currentTab == "VIP") GoldAccent else OffWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = OnyxBlack,
                    titleContentColor = GoldAccent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = OnyxBlack,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, BorderGrey, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                val items = listOf("Editorial", "Salon", "Directory", "VIP")
                val icons = listOf(Icons.Default.Menu, Icons.Default.ShoppingCart, Icons.Default.LocationOn, Icons.Default.Person)
                val labels = listOf("Editorial", "Salon", "Directory", "VIP Salon")
                
                items.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = labels[index]
                            )
                        },
                        label = {
                            Text(
                                text = labels[index],
                                fontSize = 10.sp,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OnyxBlack,
                            selectedTextColor = GoldAccent,
                            indicatorColor = GoldAccent,
                            unselectedIconColor = OffWhite.copy(alpha = 0.6f),
                            unselectedTextColor = OffWhite.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        containerColor = OnyxBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Navigating routing Animation
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    "Editorial" -> EditorialScreen(viewModel) { tabName -> currentTab = tabName }
                    "Salon" -> SalonCatalogScreen(viewModel)
                    "Directory" -> DirectoryScreen(viewModel)
                    "VIP" -> VipLoungeScreen(viewModel) { tabName -> currentTab = tabName }
                    else -> EditorialScreen(viewModel) { tabName -> currentTab = tabName }
                }
            }

            // Detailed Overlay View when an item is tapped
            selectedProduct?.let { product ->
                ProductDetailOverlay(
                    product = product,
                    onDismiss = { viewModel.selectProduct(null) },
                    onReserve = { size, qty, store, time, notes, onResult ->
                        viewModel.reserveItem(product.id, size, qty, store, time, notes, onResult)
                    },
                    vipProfile = profile,
                    onNavigateToLocation = {
                        viewModel.selectProduct(null)
                        currentTab = "Directory"
                    }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// 1. EDITORIAL SHOPPING EXPERIENCE SCREEN
// -----------------------------------------------------------------------------------
@Composable
fun EditorialScreen(
    viewModel: AuraViewModel,
    onNavigateTab: (String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Curated Stories",
                        color = PureWhite,
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aesthetic inspirations & master-crafted edits",
                        color = MutedGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                
                // Active status label
                Box(
                    modifier = Modifier
                        .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, GoldAccent, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SUMMER 2026",
                        color = GoldAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Editorial Collection Stories
        items(viewModel.editorialStories) { story ->
            EditorialStoryCard(
                story = story,
                viewModel = viewModel,
                onExploreProducts = {
                    viewModel.selectCategory("All")
                    onNavigateTab("Salon")
                }
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(DarkGrey80, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .clickable { onNavigateTab("Directory") }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, GoldAccent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Map icon",
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Explore In-Store Layouts",
                            color = PureWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Locate fine timepieces, couture salons & private lounges.",
                            color = OffWhite.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Arrow right icon",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditorialStoryCard(
    story: EditorialStory,
    viewModel: AuraViewModel,
    onExploreProducts: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("editorial_card_${story.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkGrey80),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Large atmospheric lookbook imagery
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(story.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // High contrast aesthetic gradient overlay for fashion branding
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, OnyxBlack.copy(alpha = 0.9f)),
                                startY = 100f
                            )
                        )
                )

                // Story headlines
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = story.subtitle.uppercase(),
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.title,
                        color = PureWhite,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = story.intro,
                    color = OffWhite.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { expanded = !expanded },
                        colors = ButtonDefaults.textButtonColors(contentColor = GoldAccent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = if (expanded) "Read Less" else "Continue Reading",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(GoldAccent, RoundedCornerShape(8.dp))
                            .clickable { onExploreProducts() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Shop Lookbook",
                            color = OnyxBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Expandable shoppable product grid
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 20.dp)) {
                        Text(
                            text = "FEATURED PIECES",
                            color = MutedGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Resolve products from the repository dynamically
                        val allProducts by viewModel.products.collectAsState()
                        val storyProducts = allProducts.filter { it.id in story.featuredProductIds }

                        if (storyProducts.isEmpty()) {
                            Text(
                                text = "Discover luxury items inside our full Ateliers catalog.",
                                color = OffWhite.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                storyProducts.forEach { product ->
                                    ShoppableStoryItemRow(product = product) {
                                        viewModel.selectProduct(product)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppableStoryItemRow(
    product: ProductEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OnyxBlack, RoundedCornerShape(12.dp))
            .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.brand.uppercase(),
                color = GoldAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = product.name,
                color = PureWhite,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "£${"%,.2f".format(product.price)}",
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View details",
                color = MutedGold,
                fontSize = 10.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------------
// 2. CURATED MULTI-BRAND PRODUCT CATALOGUE + CONCIERGE CHAT
// -----------------------------------------------------------------------------------
@Composable
fun SalonCatalogScreen(viewModel: AuraViewModel) {
    val products by viewModel.products.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    
    var viewMode by remember { mutableStateOf("Catalog") } // "Catalog" or "Private Chat"
    var typedMessage by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val categories = listOf("All", "Fashion", "Watches", "Beauty", "Jewellery", "Home", "Food")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
    ) {
        // Mode Selector: Browse vs digital personal concierge stylist
        TabRow(
            selectedTabIndex = if (viewMode == "Catalog") 0 else 1,
            containerColor = OnyxBlack,
            contentColor = GoldAccent,
            modifier = Modifier.fillMaxWidth().border(1.dp, BorderGrey)
        ) {
            Tab(
                selected = viewMode == "Catalog",
                onClick = { viewMode = "Catalog" },
                text = {
                    Text(
                        "BROWSE COLLECTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                modifier = Modifier.testTag("tab_catalog")
            )
            Tab(
                selected = viewMode == "Private Chat",
                onClick = { viewMode = "Private Chat" },
                text = {
                    Text(
                        "CONCIERGE STYLIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                modifier = Modifier.testTag("tab_concierge")
            )
        }

        if (viewMode == "Catalog") {
            // Elegant search card with Category Scrollers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Search brands (Gucci, Rolex, Cartier...)", color = OffWhite.copy(alpha = 0.5f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = OffWhite)
                            }
                        }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = DarkGrey80,
                        unfocusedContainerColor = DarkGrey80
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Category chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GoldAccent else BorderGrey,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.selectCategory(cat) }
                                .testTag("category_chip_$cat"),
                            color = if (isSelected) GoldAccent.copy(alpha = 0.12f) else DarkGrey80,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = cat.uppercase(),
                                color = if (isSelected) GoldAccent else OffWhite.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Products grid list
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Not found",
                            tint = MutedGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Exquisite Items Found",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please reach out to our Concierge for custom inquiries.",
                            color = OffWhite.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(products) { product ->
                        ProductCardRow(product = product) {
                            viewModel.selectProduct(product)
                        }
                    }
                }
            }
        } else {
            // Personal Concierge Chat View with Arthur Pendleton
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(OnyxBlack)
            ) {
                // Arthur Pendleton Bio Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGrey80)
                        .border(1.dp, BorderGrey)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Brush.radialGradient(listOf(GoldAccent, OnyxBlack)), RoundedCornerShape(21.dp))
                            .border(1.dp, GoldAccent, RoundedCornerShape(21.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AP",
                            color = OnyxBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Arthur Pendleton",
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(GoldAccent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("PRIVATE CLIENT LEAD", color = OnyxBlack, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Online • Ready to curate bespoke styles",
                            color = GoldAccent,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = { viewModel.clearChatHistory() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear History", tint = OffWhite.copy(alpha = 0.5f))
                    }
                }

                // Scrollable Chat Message bubbles
                val scrollState = rememberScrollState()
                LaunchedEffect(chatMessages.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    if (chatMessages.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aura Digital Concierge",
                                color = PureWhite,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Arthur Pendleton is ready to coordinates your wardrobes, booking in-house appointments & find limited items. Inquire freely below.",
                                color = OffWhite.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            chatMessages.forEach { msg ->
                                val isUser = msg.role == "user"
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Column(
                                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
                                        modifier = Modifier.widthIn(max = 290.dp)
                                    ) {
                                        Surface(
                                            color = if (isUser) GoldAccent else DarkGrey80,
                                            shape = RoundedCornerShape(
                                                topStart = 14.dp,
                                                topEnd = 14.dp,
                                                bottomStart = if (isUser) 14.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 14.dp
                                            ),
                                            border = if (isUser) null else BorderStroke(1.dp, BorderGrey)
                                        ) {
                                            Text(
                                                text = msg.content,
                                                color = if (isUser) Color.White else PureWhite,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isUser) "Thomas" else "Arthur",
                                            color = OffWhite.copy(alpha = 0.4f),
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (chatState is ConciergeChatState.Loading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = GoldAccent,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Arthur is typing response...",
                                        color = GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Assistant prompts
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestions = listOf(
                        "Recommend a structured wool overcoat",
                        "Is the Rolex Submariner available on floor 3?",
                        "Arrange an Haute Couture styling slot"
                    )
                    suggestions.forEach { suggestion ->
                        Surface(
                            modifier = Modifier
                                .clickable { typedMessage = suggestion }
                                .border(1.dp, BorderGrey, RoundedCornerShape(20.dp)),
                            color = DarkGrey80,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = suggestion,
                                color = GoldAccent,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Chat Input bar
                Surface(
                    color = DarkGrey80,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderGrey)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = typedMessage,
                            onValueChange = { typedMessage = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input"),
                            placeholder = { Text("Consult Arthur elegantly...", color = OffWhite.copy(alpha = 0.4f), fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (typedMessage.isNotBlank()) {
                                    viewModel.sendChatMessage(typedMessage)
                                    typedMessage = ""
                                    keyboardController?.hide()
                                }
                            }),
                            textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = OnyxBlack,
                                unfocusedContainerColor = OnyxBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (typedMessage.isNotBlank()) {
                                    viewModel.sendChatMessage(typedMessage)
                                    typedMessage = ""
                                    keyboardController?.hide()
                                }
                            },
                            enabled = typedMessage.isNotBlank(),
                            modifier = Modifier.testTag("send_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Message",
                                tint = if (typedMessage.isNotBlank()) GoldAccent else OffWhite.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardRow(
    product: ProductEntity,
    onDetailClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_row_${product.id}")
            .clickable { onDetailClick() },
        colors = CardDefaults.cardColors(containerColor = DarkGrey80),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Immersively framed picture
            Box(
                modifier = Modifier
                    .size(120.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                if (product.isExclusive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(GoldAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "EXCLUSIVE",
                            color = OnyxBlack,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Exquisite texts
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.brand.uppercase(),
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "FLOOR ${product.floor}",
                        color = MutedGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = product.name,
                    color = PureWhite,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.description,
                    color = OffWhite.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "£${"%,.2f".format(product.price)}",
                        color = PureWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Stock indicators
                    val storeStock = product.stockInStore
                    Text(
                        text = when {
                            storeStock <= 0 -> "Online Only"
                            storeStock < 3 -> "Limited In Flagship"
                            else -> "In Stock at Flagship"
                        },
                        color = if (storeStock > 0) Color(0xFF81C784) else GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// 3. PHYSICAL STORE DIGITAL NAVIGATION / DIRECTORY ENGINE
// -----------------------------------------------------------------------------------
@Composable
fun DirectoryScreen(viewModel: AuraViewModel) {
    var selectedFloor by remember { mutableStateOf(2) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Flagship Guide",
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "St. James’s Square Pavilion Floor Plans",
                    color = MutedGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Active Floor selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(4, 3, 2, 1).forEach { floor ->
                    val isSelected = selectedFloor == floor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .background(
                                color = if (isSelected) GoldAccent else DarkGrey80,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isSelected) GoldAccent else BorderGrey, RoundedCornerShape(8.dp))
                            .clickable { selectedFloor = floor }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "L${floor}",
                            color = if (isSelected) OnyxBlack else PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Map layout diagram placeholder drawing using beautiful Canvas styling
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(DarkGrey80, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw abstract floor blueprint lines in gold tone
                    val w = size.width
                    val h = size.height

                    // Outer border
                    drawRect(
                        color = GoldAccent.copy(alpha = 0.25f),
                        size = size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )

                    // Layout rooms
                    drawLine(
                        color = BorderGrey,
                        start = androidx.compose.ui.geometry.Offset(w * 0.4f, 0f),
                        end = androidx.compose.ui.geometry.Offset(w * 0.4f, h),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = BorderGrey,
                        start = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.5f),
                        end = androidx.compose.ui.geometry.Offset(w, h * 0.5f),
                        strokeWidth = 3f
                    )

                    // Draw Escalator symbol
                    drawLine(
                        color = GoldAccent.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.3f),
                        end = androidx.compose.ui.geometry.Offset(w * 0.3f, h * 0.7f),
                        strokeWidth = 4f
                    )
                }

                // Navigation texts pointing layout features
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("North Atrium", color = OffWhite.copy(alpha = 0.4f), fontSize = 10.sp)
                        Text("East Salon Loft", color = OffWhite.copy(alpha = 0.4f), fontSize = 10.sp)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(OnyxBlack.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                            .border(1.dp, GoldAccent, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (selectedFloor) {
                                1 -> "LEVEL 1: COSMETICS & FOYER"
                                2 -> "LEVEL 2: COUTURE & LEATHER"
                                3 -> "LEVEL 3: WATCHES & GEMS"
                                else -> "LEVEL 4: PRIVATE SALON & DECOR"
                            },
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Elevators", color = OffWhite.copy(alpha = 0.4f), fontSize = 10.sp)
                        Text("Private Lounge", color = OffWhite.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                }
            }
        }

        // Active Departments listings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkGrey80),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderGrey)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FLOOR DEPARTMENT DIRECTORY",
                        color = MutedGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    viewModel.departmentDirectory.forEach { dept ->
                        val isHighlighted = dept.floor == selectedFloor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isHighlighted) GoldAccent.copy(alpha = 0.1f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedFloor = dept.floor }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "L${dept.floor}",
                                color = if (isHighlighted) GoldAccent else PureWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = dept.name,
                                    color = if (isHighlighted) GoldAccent else PureWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dept.descriptions,
                                    color = if (isHighlighted) OffWhite else OffWhite.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            if (isHighlighted) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoldAccent)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Products hosted in selected floor
        item {
            val allProducts by viewModel.products.collectAsState()
            val floorProducts = allProducts.filter { it.floor == selectedFloor }

            Text(
                text = "PIECES IN THIS ATELIER",
                color = MutedGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            if (floorProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGrey80, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ask our Private Stylist to fetch coordinates.",
                        color = OffWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    floorProducts.forEach { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkGrey80, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectProduct(prod) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(prod.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = prod.name,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.brand.uppercase(),
                                    color = GoldAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = prod.name,
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Section: ${prod.section}",
                                    color = OffWhite.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "View maps",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// 4. PRIVATE VIP LOUNGE SCREEN (MEMBERSHIPS, RESERVATIONS, ENERGETIC CALENDAR)
// -----------------------------------------------------------------------------------
@Composable
fun VipLoungeScreen(
    viewModel: AuraViewModel,
    onNavigateTab: (String) -> Unit
) {
    val profile by viewModel.vipProfile.collectAsState()
    val reservations by viewModel.reservations.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    var showBookingDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OnyxBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // VIP Banner Title
        item {
            Column {
                Text(
                    text = "Aura Lounge",
                    color = PureWhite,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Exclusive digital suite for Private Clients",
                    color = MutedGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }

        // Stunning VIP Membership Platinum Card
        item {
            profile?.let { prof ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vip_card"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GoldAccent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1E1E1E),
                                        Color(0xFF0F0F0F),
                                        Color(0xFF2C2517)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "AURA PRIVATE CLIENT",
                                        color = GoldAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = prof.membershipTier.uppercase(),
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ELITE",
                                        color = Color(0xFF1C1B17), // Deep charcoal contrasting label
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "CLIENT IDENTIFIER",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = prof.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "LOYALTY RESERVE",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${"%,d".format(prof.points)} PTS",
                                        color = GoldAccent,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Shopper assignment
        item {
            profile?.let { prof ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkGrey80),
                    border = BorderStroke(1.dp, BorderGrey)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(GoldAccent.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                                .border(1.dp, GoldAccent, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DEFINED STYLIST",
                                color = MutedGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = prof.personalShopperName,
                                color = PureWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Style Affinity: ${prof.stylePreferences}",
                                color = OffWhite.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { onNavigateTab("Salon") },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text("Stylist Chat", color = OnyxBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Custom CTA - Schedule Personal Appointment
        item {
            Button(
                onClick = { showBookingDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("book_appointment_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = OnyxBlack)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BOOK PRIVATE CLIENT APPOINTMENT",
                    color = OnyxBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        // ACTIVE CLICK & COLLECT RESERVATIONS
        item {
            Text(
                text = "CLICK & COLLECT CORNER (RESERVATIONS)",
                color = MutedGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        if (reservations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGrey80, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items holding reserved status. Explore Ateliers to reserve pieces.",
                        color = OffWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(reservations) { res ->
                ReservationRowCard(res = res) {
                    viewModel.cancelReservation(res.id)
                }
            }
        }

        // BOOKED APPTS
        item {
            Text(
                text = "UPCOMING booked SALON appointments",
                color = MutedGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        if (appointments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkGrey80, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No private sessions booked. Schedule a personal stylist visit above.",
                        color = OffWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(appointments) { appt ->
                AppointmentRowCard(appt = appt)
            }
        }
    }

    // Appointment Booking Selector popup
    if (showBookingDialog) {
        AppointmentBookingPopup(
            onDismiss = { showBookingDialog = false },
            onConfirm = { date, time, category, notes ->
                viewModel.bookAppointment(date, time, category, notes)
                showBookingDialog = false
            }
        )
    }
}

@Composable
fun ReservationRowCard(
    res: ReservationEntity,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("reservation_card_${res.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkGrey80),
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = res.brand.uppercase(), color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(text = res.productName, color = PureWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF81C784).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF81C784), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = res.status.uppercase(), color = Color(0xFF81C784), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = BorderGrey, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "PARTICULARS", color = OffWhite.copy(alpha = 0.4f), fontSize = 8.sp)
                    Text(text = "Size: ${res.size} • Qty: ${res.quantity}", color = OffWhite, fontSize = 12.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "LOCATION & TIMELINE", color = OffWhite.copy(alpha = 0.4f), fontSize = 8.sp)
                    Text(text = "${res.storeName} • ${res.pickupTime}", color = OffWhite, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reserved holds is secure for 48 hours",
                    color = GoldAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                TextButton(onSubmit = onCancel) {
                    Text("Cancel Hold", color = Color.Red.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }
    }
}

// Inline TextButton with Ripple Support to enforce boundaries
@Composable
fun TextButton(onSubmit: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onSubmit() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        content()
    }
}

@Composable
fun AppointmentRowCard(appt: AppointmentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkGrey80),
        border = BorderStroke(1.dp, BorderGrey)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = GoldAccent)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appt.categoryPreference.uppercase(),
                    color = GoldAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Styling with ${appt.shopperName}",
                    color = PureWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Schedule: ${appt.date} @ ${appt.time}",
                    color = OffWhite.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                if (appt.notes.isNotBlank()) {
                    Text(
                        text = "Notes: \"${appt.notes}\"",
                        color = OffWhite.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "CONFIRMED", color = GoldAccent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// 5. APPOINTMENT BOOKING FORM POPUP
// -----------------------------------------------------------------------------------
@Composable
fun AppointmentBookingPopup(
    onDismiss: () -> Unit,
    onConfirm: (date: String, time: String, category: String, notes: String) -> Unit
) {
    var date by remember { mutableStateOf("June 5, 2026") }
    var time by remember { mutableStateOf("14:00 PM") }
    var category by remember { mutableStateOf("Classic Couture & Handbags Shop") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf(
        "Classic Couture & Handbags Shop",
        "Fine Timepieces & Chronometers Salon",
        "Jewellery Custom Settings Court",
        "Home Styling & Maison Consultation"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("appointment_booking_box"),
            shape = RoundedCornerShape(16.dp),
            color = DarkGrey80,
            border = BorderStroke(1.dp, GoldAccent)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AURA ATELIER BOOKING",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Bespoke Styling",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )

                // Select Service field
                Text(text = "SALON SERVICE PREFERENCE", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                categories.forEach { cat ->
                    val isSelected = category == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) GoldAccent.copy(alpha = 0.12f) else OnyxBlack,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isSelected) GoldAccent else BorderGrey, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = cat, color = if (isSelected) GoldAccent else OffWhite, fontSize = 12.sp)
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Date field
                Text(text = "DATE SELECT", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = OnyxBlack,
                        unfocusedContainerColor = OnyxBlack
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_date_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Time slot field
                Text(text = "TIME PREFERENCE", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = OnyxBlack,
                        unfocusedContainerColor = OnyxBlack
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_time_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                // Notes field
                Text(text = "CONCIERGE REQUIREMENTS (OPTIONAL)", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = OnyxBlack,
                        unfocusedContainerColor = OnyxBlack
                    ),
                    placeholder = { Text("E.g., Prefers Champagne, interested in specific Rolex Submariner references", color = OffWhite.copy(alpha = 0.4f), fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("add_notes_input"),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onSubmit = onDismiss) {
                        Text("Dismiss", color = OffWhite.copy(alpha = 0.6f))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onConfirm(date, time, category, notes) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier.testTag("booking_scheduler_submit")
                    ) {
                        Text("Confirm Secure", color = OnyxBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------
// 6. DETAILED PRODUCT OVERLAY BOTTOM DIALOG SHEETS
// -----------------------------------------------------------------------------------
@Composable
fun ProductDetailOverlay(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onReserve: (size: String, qty: Int, store: String, time: String, notes: String, onResult: (Boolean) -> Unit) -> Unit,
    vipProfile: VipProfileEntity?,
    onNavigateToLocation: () -> Unit
) {
    val sizeList = product.sizeOptions.split(",")
    var selectedSize by remember { mutableStateOf(sizeList.firstOrNull() ?: "One Size") }
    var selectedQty by remember { mutableStateOf(1) }
    var selectedTime by remember { mutableStateOf("Scheduled Collection: Tonight 18:00") }
    var selectedStore by remember { mutableStateOf("St. James's Square Flagship") }
    var notes by remember { mutableStateOf("") }

    var isBookedSuccessfully by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("product_detail_overlay"),
            shape = RoundedCornerShape(16.dp),
            contentColor = PureWhite,
            color = DarkGrey80,
            border = BorderStroke(1.dp, GoldAccent)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header brand banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.brand.uppercase(),
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_detail_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close overlay", tint = OffWhite)
                    }
                }

                // High-res Picture Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Product details
                Text(
                    text = product.name,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "£${"%,.2f".format(product.price)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )

                    Box(
                        modifier = Modifier
                            .background(OnyxBlack, RoundedCornerShape(8.dp))
                            .border(1.dp, BorderGrey, RoundedCornerShape(8.dp))
                            .clickable { onNavigateToLocation() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                            Text(text = "FLOOR ${product.floor} SECTION", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = BorderGrey)

                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = OffWhite.copy(alpha = 0.85f)
                )

                if (isBookedSuccessfully) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF81C784).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF81C784), RoundedCornerShape(10.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "HOLD SECURED SUCCESFULLY\nArthur and our Floor Stylists are wrapping your luxury reserve inside our private vaults. Review details in VIP Lounge.",
                            color = Color(0xFF81C784),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // SECURE RESERVATION OPTIONS
                    Text(
                        text = "SECURE COLLECT HOLD PARTICULARS",
                        color = MutedGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    // Size Selection chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "CHOSE SIZE", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            sizeList.forEach { sz ->
                                val isSelected = selectedSize == sz
                                Surface(
                                    modifier = Modifier
                                        .border(1.dp, if (isSelected) GoldAccent else BorderGrey, RoundedCornerShape(6.dp))
                                        .clickable { selectedSize = sz },
                                    color = if (isSelected) GoldAccent.copy(alpha = 0.15f) else OnyxBlack,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = sz,
                                        color = if (isSelected) GoldAccent else OffWhite,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Schedule / time input
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "COLLECTION SCHEDULE ESTIMATION", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                        OutlinedTextField(
                            value = selectedTime,
                            onValueChange = { selectedTime = it },
                            modifier = Modifier.fillMaxWidth().testTag("reservation_time_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = BorderGrey,
                                focusedContainerColor = OnyxBlack,
                                unfocusedContainerColor = OnyxBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Special instructions
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "CLIENT COMPLIMENTARY NOTES", color = OffWhite.copy(alpha = 0.6f), fontSize = 10.sp)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("E.g., Gift wrapping required, bespoke gift tag message", color = OffWhite.copy(alpha = 0.4f), fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("reservation_notes_input"),
                            textStyle = androidx.compose.ui.text.TextStyle(color = PureWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldAccent,
                                unfocusedBorderColor = BorderGrey,
                                focusedContainerColor = OnyxBlack,
                                unfocusedContainerColor = OnyxBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Reserve submission button
                    Button(
                        onClick = {
                            onReserve(selectedSize, selectedQty, selectedStore, selectedTime, notes) { success ->
                                if (success) {
                                    isBookedSuccessfully = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_hold_button")
                    ) {
                        Text(
                            text = "SECURE EXQUISITE COLLECT HOLD",
                            color = OnyxBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

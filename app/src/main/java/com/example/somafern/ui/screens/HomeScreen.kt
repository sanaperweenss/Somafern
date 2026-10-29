package com.example.somafern.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.somafern.R
import com.example.somafern.data.model.Coupon
import com.example.somafern.data.model.FoodItem
import com.example.somafern.data.repository.FirebaseRepository
import com.example.somafern.ui.components.FoodVegIndicator
import com.example.somafern.ui.components.NotificationCenterBottomSheet
import com.example.somafern.ui.components.SomafernFoodImage
import com.example.somafern.ui.components.SomafernLogo
import com.example.somafern.ui.components.SomafernSearchBar
import com.example.somafern.ui.navigation.Screen
import com.example.somafern.ui.theme.AccentGreen
import com.example.somafern.ui.theme.DangerRed
import com.example.somafern.ui.theme.GoldLight
import com.example.somafern.ui.theme.GoldPrimary
import com.example.somafern.ui.theme.NavyBackground
import com.example.somafern.ui.theme.NavyBorder
import com.example.somafern.ui.theme.NavyCard
import com.example.somafern.ui.theme.NavyInput
import com.example.somafern.ui.theme.StarYellow
import com.example.somafern.ui.theme.TextMuted
import com.example.somafern.ui.theme.TextPrimary
import com.example.somafern.ui.theme.TextSecondary
import com.example.somafern.viewmodel.SomafernViewModel

private data class CategoryChip(
    val name: String,
    val iconEmoji: String
)

private data class FeaturedBeverage(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val imageUrl: String = "",
    val drawableRes: Int? = null,
    val tag: String
)

@Composable
fun HomeScreen(
    viewModel: SomafernViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val fullCardWidth = (screenWidth - 32.dp).coerceAtLeast(280.dp)
    val dishCardWidth = 145.dp
    val beverageCardWidth = 142.dp

    val menuItems by viewModel.menuItems.collectAsState()
    val coupons by viewModel.coupons.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val activeHeroBanners = remember(banners) { banners.filter { it.isActive } }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val showNotificationCenter by viewModel.showNotificationCenter.collectAsState()
    val unreadNotifCount = viewModel.getUnreadNotificationCount()
    val favoriteItemIds by viewModel.favoriteItemIds.collectAsState()
    val restaurantSettings by viewModel.restaurantSettings.collectAsState()
    val cartCount = viewModel.getCartCount()

    val popularCombos = menuItems.filter { it.isPopular }
    val displayDishes = if (popularCombos.isNotEmpty()) popularCombos else menuItems

    val scrollState = rememberScrollState()
    var copiedCouponCode by remember { mutableStateOf<String?>(null) }
    val activeCoupons = if (coupons.any { it.isActive }) coupons.filter { it.isActive } else FirebaseRepository.defaultCoupons

    val categoryList = listOf(
        CategoryChip("All Dishes", "⭐"),
        CategoryChip("Pasta", "🍝"),
        CategoryChip("Momos", "🥟"),
        CategoryChip("Noodles", "🍜"),
        CategoryChip("Ramen", "🍲"),
        CategoryChip("Thupka", "🥘"),
        CategoryChip("Sandwiches", "🥪"),
        CategoryChip("Maggie", "🥢"),
        CategoryChip("Drinks", "☕"),
        CategoryChip("Somafern Special", "👑")
    )

    // Curated Cafe Coolers & Shakes with guaranteed local drawables
    val featuredDrinks = listOf(
        FeaturedBeverage(
            id = "drink_hazelnut_frappe",
            name = "Iced Hazelnut Frappe",
            category = "Drinks",
            price = 130.0,
            rating = 4.9,
            drawableRes = R.drawable.drink_frappe,
            tag = "Bestseller 🔥"
        ),
        FeaturedBeverage(
            id = "drink_mint_mojito",
            name = "Virgin Mint Mojito",
            category = "Drinks",
            price = 110.0,
            rating = 4.8,
            drawableRes = R.drawable.drink_mojito,
            tag = "Refreshing 🌿"
        ),
        FeaturedBeverage(
            id = "drink_caramel_coffee",
            name = "Caramel Cold Brew",
            category = "Drinks",
            price = 120.0,
            rating = 4.9,
            drawableRes = R.drawable.drink_frappe,
            tag = "Signature ☕"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Clean Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(NavyBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo & Title (Flexible weight so it never squishes action buttons)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SomafernLogo(size = 30.dp, logoUrl = restaurantSettings.logoUrl)
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "THE SOMAFERN",
                        color = GoldPrimary,
                        fontSize = 14.5.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Cafe & Comfort Bites ☕",
                        color = GoldLight.copy(alpha = 0.8f),
                        fontSize = 9.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Action Buttons (Optimized 34.dp size with unclipped badge padding)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Call Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(NavyCard)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.7f), CircleShape)
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+917321962452"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Cafe",
                        tint = GoldPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // WhatsApp Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(NavyCard)
                        .border(1.dp, AccentGreen.copy(alpha = 0.8f), CircleShape)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/917321962452?text=Hello%20Somafern%20Cafe!"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp Cafe",
                        tint = AccentGreen,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Notification Bell Button with Unclipped Overlay Badge
                Box(
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(NavyCard)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.7f), CircleShape)
                            .clickable { viewModel.openNotificationCenter() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = TextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (unreadNotifCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-4.dp))
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                                .border(1.5.dp, NavyBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadNotifCount.toString(),
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Cart Button with Unclipped Overlay Badge
                Box(
                    modifier = Modifier.padding(top = 4.dp, end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(NavyCard)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.7f), CircleShape)
                            .clickable { onNavigate(Screen.Cart.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = TextPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-4.dp))
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                                .border(1.5.dp, NavyBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Main Scrollable Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = 28.dp)
        ) {
            // Warm Welcoming Sub-Header with Dynamic Font Scaling & Responsive Device-Adaptive Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val titleFontSize = if (screenWidth < 360.dp) 15.sp else if (screenWidth < 400.dp) 16.sp else 18.sp
                val subtitleFontSize = if (screenWidth < 360.dp) 10.sp else 11.sp

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Welcome to SOMAFERN ✨",
                        color = GoldLight,
                        fontSize = titleFontSize,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Handcrafted Comfort Food & Fresh Brews",
                        color = TextSecondary,
                        fontSize = subtitleFontSize,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                val isOpen = restaurantSettings.isOnlineOrderingOpen
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isOpen) AccentGreen.copy(alpha = 0.15f) else DangerRed.copy(alpha = 0.15f))
                        .border(1.dp, if (isOpen) AccentGreen.copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOpen) AccentGreen else DangerRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOpen) "Open Now" else "Closed",
                            color = if (isOpen) AccentGreen else DangerRed,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Modern Search Bar
            SomafernSearchBar(
                query = searchQuery,
                onQueryChange = {
                    viewModel.setSearchQuery(it)
                    if (it.isNotBlank()) {
                        onNavigate(Screen.Menu.route)
                    }
                },
                placeholderText = "Search dishes, coffee, pasta...",
                onFilterClick = { onNavigate(Screen.Menu.route) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // FEATURE 4: QUICK SEARCH SUGGESTION TAG CHIPS
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf("White Sauce Pasta", "Steam Momos", "Hazelnut Frappe", "Thupka", "Cheese Sandwich")
                items(suggestions) { suggestion ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyCard)
                            .border(1.dp, NavyBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setSearchQuery(suggestion)
                                onNavigate(Screen.Menu.route)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = suggestion,
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

Spacer(modifier = Modifier.height(16.dp))

            // Categories Section ("What are you craving? 🍽️")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "What are you craving? 🍽️",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Full Menu >",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(Screen.Menu.route) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryList) { cat ->
                    val isSelected = selectedCategory.equals(cat.name, ignoreCase = true) ||
                            (cat.name == "All Dishes" && (selectedCategory.isBlank() || selectedCategory == "All Dishes"))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GoldPrimary else NavyCard)
                            .border(1.dp, if (isSelected) GoldPrimary else NavyBorder, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.setCategory(cat.name)
                                onNavigate(Screen.Menu.route)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = cat.iconEmoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            color = if (isSelected) NavyBackground else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic "Chef's Special" Hero Carousel (Matches Reference Aesthetic)
            val chefSpecialItems = menuItems.filter { it.isTodaySpecial }.ifEmpty { menuItems.filter { it.isPopular }.ifEmpty { menuItems.take(4) } }
            val chefSpecialListState = rememberLazyListState()
            val chefSpecialSnapFlingBehavior = rememberSnapFlingBehavior(lazyListState = chefSpecialListState)

            Column {
                LazyRow(
                    state = chefSpecialListState,
                    flingBehavior = chefSpecialSnapFlingBehavior,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(chefSpecialItems, key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .width(fullCardWidth)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF241C14),
                                            Color(0xFF16213A),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                )
                                .border(1.2.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.showDishDetails(item) }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(GoldPrimary.copy(alpha = 0.2f))
                                            .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "CHEF'S SPECIAL ⭐",
                                            color = GoldPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.name,
                                        color = GoldLight,
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 22.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${item.category} • ₹${item.price.toInt()} ${if (item.offerTag.isNotBlank()) "(${item.offerTag})" else ""}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Order Now pill button
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(GoldPrimary)
                                            .clickable {
                                                viewModel.addToCart(item)
                                                onNavigate(Screen.Cart.route)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Order Now • ₹${item.price.toInt()}",
                                            color = NavyBackground,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = NavyBackground,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Floating Dish Photo with Gold Rim
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, GoldPrimary, CircleShape)
                                        .background(NavyCard)
                                ) {
                                    SomafernFoodImage(
                                        model = item.imageUrl,
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        drawableRes = item.drawableRes
                                    )
                                }
                            }
                        }
                    }
                }

                if (chefSpecialItems.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chefSpecialItems.forEachIndexed { index, _ ->
                            val isCurrentPage = chefSpecialListState.firstVisibleItemIndex == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isCurrentPage) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentPage) GoldPrimary else NavyBorder)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MULTI-COUPON CAROUSEL (Active Cafe Coupons)
            val couponListState = rememberLazyListState()
            val couponSnapFlingBehavior = rememberSnapFlingBehavior(lazyListState = couponListState)

            Column {
                LazyRow(
                    state = couponListState,
                    flingBehavior = couponSnapFlingBehavior,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activeCoupons) { coupon ->
                        val isThisCopied = copiedCouponCode == coupon.code
                        Box(
                            modifier = Modifier
                                .width(fullCardWidth)
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFBBF24),
                                            Color(0xFFF59E0B),
                                            Color(0xFFD97706)
                                        )
                                    )
                                )
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Coupon", coupon.code)
                                    clipboard.setPrimaryClip(clip)
                                    copiedCouponCode = coupon.code
                                    Toast.makeText(context, "Code ${coupon.code} copied! 🎉", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = Color(0xFF451A03),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = coupon.title,
                                            color = Color(0xFF451A03),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Flat ${coupon.discountPercent}% OFF On Orders Above ₹${coupon.minOrderValue.toInt()}",
                                        color = Color(0xFF1E1003),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1
                                    )

                                    Text(
                                        text = coupon.description,
                                        color = Color(0xFF78350F),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Dark Pill with Code & Copy Button
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, GoldPrimary, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = coupon.code,
                                        color = GoldLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isThisCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = if (isThisCopied) AccentGreen else GoldPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (activeCoupons.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeCoupons.forEachIndexed { index, _ ->
                            val isCurrentPage = couponListState.firstVisibleItemIndex == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isCurrentPage) 8.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentPage) GoldPrimary else NavyBorder)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trending Cafe Dishes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Dishes 🔥",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "See All >",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigate(Screen.Menu.route) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (displayDishes.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayDishes) { dish ->
                        HomeDishCard(
                            item = dish,
                            cardWidth = dishCardWidth,
                            isFavorite = favoriteItemIds.contains(dish.id),
                            onFavoriteToggle = { viewModel.toggleFavorite(dish.id) },
                            onAdd = { viewModel.addToCart(dish) },
                            onItemClick = { viewModel.showDishDetails(dish) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chilled Sips & Shakes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chilled Sips & Shakes 🍹",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Thick frappes, coolers & artisan coffee",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Explore Drinks >",
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        viewModel.setCategory("Drinks")
                        onNavigate(Screen.Menu.route)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(featuredDrinks) { drink ->
                    BeverageCard(
                        drink = drink,
                        cardWidth = beverageCardWidth,
                        isFavorite = favoriteItemIds.contains(drink.id),
                        onFavoriteToggle = { viewModel.toggleFavorite(drink.id) },
                        onAdd = {
                            viewModel.addToCart(
                                FoodItem(
                                    id = drink.id,
                                    name = drink.name,
                                    description = drink.tag,
                                    price = drink.price,
                                    rating = drink.rating,
                                    category = drink.category,
                                    imageUrl = drink.imageUrl,
                                    drawableRes = drink.drawableRes
                                )
                            )
                        },
                        onItemClick = {
                            viewModel.showDishDetails(
                                FoodItem(
                                    id = drink.id,
                                    name = drink.name,
                                    description = "${drink.tag}. Crafted fresh to order with premium artisan ingredients, chilled to perfection.",
                                    price = drink.price,
                                    rating = drink.rating,
                                    category = drink.category,
                                    imageUrl = drink.imageUrl,
                                    drawableRes = drink.drawableRes
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GUARANTEED HIGH-RESOLUTION COMBOS (No missing/broken images!)
            Text(
                text = "Must-Try Cafe Combos 🍱",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Pair your favorite dishes & save more",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Combo 1: Italian Delight Combo (Using verified local combo_pasta drawable)
            CafeComboCard(
                title = "Italian Delight Combo",
                description = "Creamy White Sauce Pasta + Iced Hazelnut Frappe",
                originalPrice = 490.0,
                comboPrice = 399.0,
                saveAmount = "SAVE ₹91",
                drawableRes = R.drawable.combo_pasta,
                isFavorite = favoriteItemIds.contains("combo_italian_delight"),
                onFavoriteToggle = { viewModel.toggleFavorite("combo_italian_delight") },
                onAdd = {
                    viewModel.addToCart(
                        FoodItem(
                            id = "combo_italian_delight",
                            name = "Italian Delight Combo (Pasta + Frappe)",
                            description = "Creamy White Sauce Pasta + Iced Hazelnut Frappe. Complete Italian cafe comfort combo.",
                            price = 399.0,
                            rating = 4.9,
                            category = "Combos",
                            drawableRes = R.drawable.combo_pasta
                        )
                    )
                },
                onItemClick = {
                    viewModel.showDishDetails(
                        FoodItem(
                            id = "combo_italian_delight",
                            name = "Italian Delight Combo (Pasta + Frappe)",
                            description = "Creamy White Sauce Pasta + Iced Hazelnut Frappe. Complete Italian cafe comfort combo.",
                            price = 399.0,
                            rating = 4.9,
                            category = "Combos",
                            drawableRes = R.drawable.combo_pasta
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Combo 2: Cafe Evening Snack Feast (Using verified local combo_sandwich drawable)
            CafeComboCard(
                title = "Cafe Evening Snack Feast",
                description = "Grilled Cheese Club Sandwich + Classic Maggie",
                originalPrice = 320.0,
                comboPrice = 249.0,
                saveAmount = "SAVE ₹71",
                drawableRes = R.drawable.combo_sandwich,
                isFavorite = favoriteItemIds.contains("combo_snack_feast"),
                onFavoriteToggle = { viewModel.toggleFavorite("combo_snack_feast") },
                onAdd = {
                    viewModel.addToCart(
                        FoodItem(
                            id = "combo_snack_feast",
                            name = "Evening Snack Feast (Sandwich + Maggie)",
                            description = "Grilled Cheese Club Sandwich + Classic Maggie. Perfect evening snack combination.",
                            price = 249.0,
                            rating = 4.8,
                            category = "Combos",
                            drawableRes = R.drawable.combo_sandwich
                        )
                    )
                },
                onItemClick = {
                    viewModel.showDishDetails(
                        FoodItem(
                            id = "combo_snack_feast",
                            name = "Evening Snack Feast (Sandwich + Maggie)",
                            description = "Grilled Cheese Club Sandwich + Classic Maggie. Perfect evening snack combination.",
                            price = 249.0,
                            rating = 4.8,
                            category = "Combos",
                            drawableRes = R.drawable.combo_sandwich
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Combo 3: Royal Momo & Mojito Feast
            CafeComboCard(
                title = "Royal Momo & Brew Feast",
                description = "Steamed Chicken Momos + Virgin Mint Mojito",
                originalPrice = 370.0,
                comboPrice = 289.0,
                saveAmount = "SAVE ₹81",
                drawableRes = R.drawable.drink_mojito,
                isFavorite = favoriteItemIds.contains("combo_momo_brew"),
                onFavoriteToggle = { viewModel.toggleFavorite("combo_momo_brew") },
                onAdd = {
                    viewModel.addToCart(
                        FoodItem(
                            id = "combo_momo_brew",
                            name = "Royal Momo & Brew Feast (Momos + Mojito)",
                            description = "Steamed Chicken Momos + Refreshing Virgin Mint Mojito. Royal street-style combo.",
                            price = 289.0,
                            rating = 4.9,
                            category = "Combos",
                            drawableRes = R.drawable.drink_mojito
                        )
                    )
                },
                onItemClick = {
                    viewModel.showDishDetails(
                        FoodItem(
                            id = "combo_momo_brew",
                            name = "Royal Momo & Brew Feast (Momos + Mojito)",
                            description = "Steamed Chicken Momos + Refreshing Virgin Mint Mojito. Royal street-style combo.",
                            price = 289.0,
                            rating = 4.9,
                            category = "Combos",
                            drawableRes = R.drawable.drink_mojito
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // REPLACED LAST PART: Premium Cafe Promise & Brand Experience Card
            // 3 Trust Feature Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CafeTrustPill(
                    icon = "☕",
                    title = "Fresh Brews",
                    subtitle = "100% Handcrafted",
                    modifier = Modifier.weight(1f)
                )
                CafeTrustPill(
                    icon = "🧀",
                    title = "Cooked Fresh",
                    subtitle = "Zero Pre-Cooking",
                    modifier = Modifier.weight(1f)
                )
                CafeTrustPill(
                    icon = "⚡",
                    title = "Hot Delivery",
                    subtitle = "Kitchen Direct",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Official SOMAFERN Visit & Contact Experience Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF161E34),
                                Color(0xFF0C1322)
                            )
                        )
                    )
                    .border(1.2.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Official Gold Circular Logo
                    SomafernLogo(size = 76.dp, logoUrl = restaurantSettings.logoUrl)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "THE SOMAFERN",
                        color = GoldLight,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Home Crafted Comfort Food & Cafe",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "7M2W+V7P, Law Gate Rd, Phagwara, Punjab",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Timings Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AccentGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Open Everyday • 10:00 AM - 11:30 PM",
                            color = AccentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct Contact Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Call Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavyCard)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+917321962452"))
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call Us", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp Pill
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavyCard)
                                .border(1.dp, AccentGreen.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/917321962452?text=Hello%20Somafern%20Cafe!"))
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", color = AccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Signature
            Text(
                text = "Crafted with ❤️ for Food Lovers • SOMAFERN",
                color = TextMuted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showNotificationCenter) {
        NotificationCenterBottomSheet(
            notifications = notifications,
            onDismiss = { viewModel.closeNotificationCenter() },
            onMarkAllRead = { viewModel.markAllNotificationsAsRead() }
        )
    }
}

/**
 * Modern Dish Card for Home Screen Carousel
 */
@Composable
private fun HomeDishCard(
    item: FoodItem,
    cardWidth: Dp = 145.dp,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onAdd: () -> Unit,
    onItemClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .clickable { onItemClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(98.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyBackground)
        ) {
            SomafernFoodImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Rating Badge (Bottom Left)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(NavyBackground.copy(alpha = 0.85f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = StarYellow,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = item.rating.toString(),
                    color = TextPrimary,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Favorite Heart Icon (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(NavyBackground.copy(alpha = 0.75f))
                    .clickable { onFavoriteToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) DangerRed else Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            FoodVegIndicator(dish = item, size = 15.dp, dotSize = 5.dp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = item.name,
                color = TextPrimary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = item.category,
            color = TextMuted,
            fontSize = 10.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹${item.price.toInt()}",
                color = GoldPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = NavyBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Colorful Beverage & Cooler Card
 */
@Composable
private fun BeverageCard(
    drink: FeaturedBeverage,
    cardWidth: Dp = 142.dp,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onAdd: () -> Unit,
    onItemClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(cardWidth)
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .clickable { onItemClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(98.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NavyBackground)
        ) {
            if (drink.drawableRes != null) {
                Image(
                    painter = painterResource(id = drink.drawableRes),
                    contentDescription = drink.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = drink.imageUrl,
                    contentDescription = drink.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Rating Badge (Bottom Left)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(NavyBackground.copy(alpha = 0.85f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = StarYellow,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = drink.rating.toString(),
                    color = TextPrimary,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Favorite Heart Icon (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(NavyBackground.copy(alpha = 0.75f))
                    .clickable { onFavoriteToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) DangerRed else Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = drink.name,
            color = TextPrimary,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = drink.tag,
            color = GoldLight,
            fontSize = 10.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "₹${drink.price.toInt()}",
                color = GoldPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = NavyBackground,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * High-Converting Cafe Combo Card with Guaranteed Image
 */
@Composable
private fun CafeComboCard(
    title: String,
    description: String,
    originalPrice: Double,
    comboPrice: Double,
    saveAmount: String,
    drawableRes: Int? = null,
    imageUrl: String = "",
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onAdd: () -> Unit,
    onItemClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(18.dp))
            .clickable { onItemClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Image with Green Savings Badge & Favorite Heart
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyBackground)
            ) {
                if (drawableRes != null) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .clip(RoundedCornerShape(bottomEnd = 8.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = saveAmount,
                        color = NavyBackground,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Favorite Heart Icon (Bottom Right of Image)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(NavyBackground.copy(alpha = 0.85f))
                        .clickable { onFavoriteToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) DangerRed else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${comboPrice.toInt()}",
                            color = GoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${originalPrice.toInt()}",
                            color = TextMuted,
                            fontSize = 11.5.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    // Sleek Circular Gold '+' Action Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                            .clickable { onAdd() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Combo",
                            tint = NavyBackground,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Trust Feature Pill for Bottom Section with Animated Pulse & Refined Fonts
 */
@Composable
private fun CafeTrustPill(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = icon,
                fontSize = 22.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = GoldLight,
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

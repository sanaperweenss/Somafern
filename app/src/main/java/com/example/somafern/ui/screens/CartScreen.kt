package com.example.somafern.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.TakeoutDining
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somafern.data.model.DeliveryZone
import com.example.somafern.data.repository.FirebaseRepository
import com.example.somafern.ui.components.SomafernFoodImage
import com.example.somafern.ui.components.StandardTopBar
import com.example.somafern.ui.navigation.Screen
import com.example.somafern.ui.theme.AccentGreen
import com.example.somafern.ui.theme.DangerRed
import com.example.somafern.ui.theme.DividerColor
import com.example.somafern.ui.theme.GoldLight
import com.example.somafern.ui.theme.GoldPrimary
import com.example.somafern.ui.theme.NavyBackground
import com.example.somafern.ui.theme.NavyBorder
import com.example.somafern.ui.theme.NavyCard
import com.example.somafern.ui.theme.NavyInput
import com.example.somafern.ui.theme.TextMuted
import com.example.somafern.ui.theme.TextPrimary
import com.example.somafern.ui.theme.TextSecondary
import com.example.somafern.utils.OrderUtils
import com.example.somafern.viewmodel.SomafernViewModel

@Composable
fun CartScreen(
    viewModel: SomafernViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val addCutlery by viewModel.addCutlery.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val coupons by viewModel.coupons.collectAsState()
    val activeCoupons = if (coupons.any { it.isActive }) coupons.filter { it.isActive } else FirebaseRepository.defaultCoupons
    val isLoading by viewModel.isLoading.collectAsState()

    val menuItems by viewModel.menuItems.collectAsState()
    val restaurantSettings by viewModel.restaurantSettings.collectAsState()
    val deliveryZones by viewModel.deliveryZones.collectAsState()
    val activeZones = remember(deliveryZones) { deliveryZones.filter { it.isActive } }
    var selectedZone by remember(activeZones) {
        mutableStateOf(activeZones.firstOrNull() ?: DeliveryZone("zone_default", "Zone A (0 - 5 km)", 20.0, 199.0, "30-40 mins", true))
    }

    val unavailableItems = remember(cartItems, menuItems) {
        cartItems.filter { cartItem ->
            val menuItem = menuItems.find { it.id == cartItem.foodItem.id }
            menuItem?.isAvailable == false
        }
    }
    val hasUnavailableItems = unavailableItems.isNotEmpty()
    val isStoreOpen = restaurantSettings.isOnlineOrderingOpen

    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()

    var customerName by remember { mutableStateOf("SOMAFERN Lover") }
    var customerPhone by remember { mutableStateOf("7321962452") }
    var deliveryAddress by remember { mutableStateOf(selectedAddress) }
    var isEditingDetails by remember { mutableStateOf(false) }
    var orderType by remember { mutableStateOf("Delivery") } // "Delivery", "Dine-In", "Takeaway"
    var tableNumber by remember { mutableStateOf("Table 1") }
    var selectedPaymentMethod by remember { mutableStateOf("Counter / COD") }

    var couponInput by remember { mutableStateOf("") }
    var couponFeedback by remember { mutableStateOf<String?>(null) }
    var couponIsError by remember { mutableStateOf(false) }

    val subtotal = remember(cartItems, appliedCoupon) { viewModel.getSubtotal() }
    val discount = remember(cartItems, appliedCoupon, coupons) { viewModel.getDiscount() }
    val tax = remember(cartItems, appliedCoupon, coupons) { viewModel.getTax() }
    val baseTotal = remember(cartItems, appliedCoupon, coupons) { viewModel.getTotal() }
    val deliveryFee = if (orderType == "Delivery") {
        if (selectedZone.minOrder > 0 && subtotal >= selectedZone.minOrder) 0.0 else selectedZone.fee
    } else {
        0.0
    }
    val total = baseTotal + deliveryFee
    val cartCount = viewModel.getCartCount()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Top Navigation Bar
        StandardTopBar(
            title = if (cartCount > 0) "Your Cart ($cartCount)" else "Your Cart",
            onBackClick = { onNavigate(Screen.Home.route) },
            onDeleteClick = {
                viewModel.clearCart()
                viewModel.removeCoupon()
            }
        )

        if (cartItems.isEmpty()) {
            val infiniteTransition = rememberInfiniteTransition(label = "cartPulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            // Elegant Animated Empty Cart View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                            .border(1.2.dp, GoldPrimary.copy(alpha = 0.5f), CircleShape)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Empty Cart",
                            tint = GoldPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Your Cart is Empty",
                        color = GoldLight,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Your cart is currently empty. Explore our handcrafted comfort food & fresh brews to start your order!",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Quick Browse Recommendation Chips
                    Text(
                        text = "Popular Right Now 🔥",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val popularQuickPills = listOf(
                            "🍝 Pasta",
                            "🥟 Momos",
                            "☕ Frappe"
                        )
                        popularQuickPills.forEach { pill ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NavyCard)
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .clickable { onNavigate(Screen.Menu.route) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = pill,
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Active Cart Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isStoreOpen) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DangerRed.copy(alpha = 0.15f))
                                    .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("☕", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Café is currently paused for online orders (${restaurantSettings.openingHours}).",
                                        color = DangerRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    if (hasUnavailableItems) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DangerRed.copy(alpha = 0.15f))
                                    .border(1.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚠️", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Some items in your cart are currently out of stock. Please remove them before placing your order.",
                                        color = DangerRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Selected Items",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Cart Items
                    items(cartItems, key = { it.cartItemId.ifBlank { it.foodItem.id } }) { item ->
                        val isItemUnavailable = unavailableItems.any { it.cartItemId == item.cartItemId }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(
                                    1.dp,
                                    if (isItemUnavailable) DangerRed.copy(alpha = 0.8f) else NavyBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Food Photo
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NavyBackground)
                                ) {
                                    SomafernFoodImage(
                                        model = item.foodItem.imageUrl,
                                        contentDescription = item.foodItem.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        drawableRes = item.foodItem.drawableRes
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.foodItem.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (isItemUnavailable) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(DangerRed.copy(alpha = 0.15f))
                                                .border(0.8.dp, DangerRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Out of Stock 🔴 Please remove",
                                                color = DangerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (item.selectedAddOns.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+ " + item.selectedAddOns.joinToString(", ") { "${it.name} (₹${it.price.toInt()})" },
                                            color = GoldLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (item.specialInstructions.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Note: ${item.specialInstructions}",
                                            color = TextMuted,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Text(
                                        text = "₹${item.unitPrice.toInt()}",
                                        color = GoldPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Modern Stepper Pill
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(NavyBackground)
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .clickable { viewModel.decrementQuantity(item.cartItemId) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    Text(
                                        text = item.quantity.toString(),
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .clickable { viewModel.incrementQuantity(item.cartItemId) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Increase",
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { viewModel.removeFromCart(item.cartItemId) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = TextMuted,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Cutlery Switch
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(NavyCard)
                                .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Add cutlery (optional)",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Help us reduce disposable waste 🌿",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Switch(
                                checked = addCutlery,
                                onCheckedChange = { viewModel.toggleCutlery(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldPrimary,
                                    checkedTrackColor = NavyBackground,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = NavyBackground
                                )
                            )
                        }
                    }

                    // INTERACTIVE COUPON & PROMO CODE SECTION
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(
                                    1.dp,
                                    if (appliedCoupon != null) AccentGreen.copy(alpha = 0.6f) else NavyBorder,
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = if (appliedCoupon != null) AccentGreen else GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Apply Cafe Coupon",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (appliedCoupon != null) {
                                        Text(
                                            text = "Remove",
                                            color = DangerRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                viewModel.removeCoupon()
                                                couponInput = ""
                                                couponFeedback = null
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (appliedCoupon != null) {
                                    // Applied State
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AccentGreen.copy(alpha = 0.15f))
                                            .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = AccentGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "'$appliedCoupon' Applied • You saved ₹${discount.toInt()}! 🎉",
                                            color = AccentGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    // Coupon Input Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = couponInput,
                                            onValueChange = {
                                                couponInput = it.uppercase()
                                                couponFeedback = null
                                            },
                                            placeholder = {
                                                Text("Enter coupon (e.g. SOMA15)", color = TextMuted, fontSize = 12.sp)
                                            },
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = NavyInput,
                                                unfocusedContainerColor = NavyInput,
                                                focusedBorderColor = GoldPrimary,
                                                unfocusedBorderColor = NavyBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .height(50.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (couponInput.isNotBlank()) GoldPrimary else NavyBorder)
                                                .clickable(enabled = couponInput.isNotBlank()) {
                                                    val res = viewModel.applyCoupon(couponInput)
                                                    couponFeedback = res.second
                                                    couponIsError = !res.first
                                                    if (res.first) {
                                                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "APPLY",
                                                color = if (couponInput.isNotBlank()) NavyBackground else TextMuted,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    // Quick One-Tap Active Coupons Suggestion Row
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(activeCoupons) { cp ->
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(GoldPrimary.copy(alpha = 0.12f))
                                                    .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        couponInput = cp.code
                                                        val res = viewModel.applyCoupon(cp.code)
                                                        couponFeedback = res.second
                                                        couponIsError = !res.first
                                                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "⚡", fontSize = 11.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${cp.code} (${cp.discountPercent}% OFF)",
                                                    color = GoldLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    if (couponFeedback != null && couponIsError) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = couponFeedback ?: "",
                                            color = DangerRed,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Delivery & Contact Info Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Delivery & Contact Info",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = if (isEditingDetails) "Done ✓" else "Edit Details",
                                        color = GoldPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { isEditingDetails = !isEditingDetails }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // 1-Tap Saved Address Chips
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(savedAddresses) { addr ->
                                        val isChosen = deliveryAddress == addr.fullAddress
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isChosen) GoldPrimary else NavyInput)
                                                .border(1.dp, if (isChosen) GoldPrimary else NavyBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    deliveryAddress = addr.fullAddress
                                                    viewModel.selectAddress(addr.fullAddress)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = addr.label,
                                                color = if (isChosen) NavyBackground else GoldLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (orderType == "Delivery" && activeZones.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Select Delivery Zone",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(activeZones) { zone ->
                                            val isZoneSelected = zone.id == selectedZone.id
                                            val feeText = if (zone.fee > 0.0) "₹${zone.fee.toInt()}" else "Free"
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isZoneSelected) GoldPrimary else NavyInput)
                                                    .border(1.dp, if (isZoneSelected) GoldPrimary else NavyBorder, RoundedCornerShape(8.dp))
                                                    .clickable { selectedZone = zone }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🛵 ${zone.name} ($feeText • ${zone.estimatedTime})",
                                                    color = if (isZoneSelected) NavyBackground else GoldLight,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (isEditingDetails) {
                                    OutlinedTextField(
                                        value = customerName,
                                        onValueChange = { customerName = it },
                                        label = { Text("Your Name", color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = NavyInput,
                                            unfocusedContainerColor = NavyInput,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = customerPhone,
                                        onValueChange = { customerPhone = it },
                                        label = { Text("Phone Number", color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = NavyInput,
                                            unfocusedContainerColor = NavyInput,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = deliveryAddress,
                                        onValueChange = { deliveryAddress = it },
                                        label = { Text("Delivery Address", color = TextMuted) },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted) },
                                        maxLines = 2,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = NavyInput,
                                            unfocusedContainerColor = NavyInput,
                                            focusedBorderColor = GoldPrimary,
                                            unfocusedBorderColor = NavyBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        )
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "👤 $customerName",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "• 📞 $customerPhone",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📍 $deliveryAddress",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    // Payment Method Card (Play Store Compliant - Physical Goods)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payment Settlement",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoldPrimary.copy(alpha = 0.12f))
                                        .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = NavyBackground,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (orderType == "Dine-In") "Pay at Table / Counter" else if (orderType == "Takeaway") "Pay at Counter on Pickup" else "Cash / Pay on Delivery",
                                            color = TextPrimary,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Pay in cash, card, or cafe QR once your food is ready",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text("Direct", color = AccentGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Bill Summary Card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Bill Summary",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Subtotal", color = TextSecondary, fontSize = 13.sp)
                                    Text("₹${subtotal.toInt()}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (discount > 0.0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Coupon Discount (${appliedCoupon})", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text("-₹${discount.toInt()}", color = AccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Delivery Partner Fee (${if (orderType == "Delivery") selectedZone.name else orderType})",
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                    if (deliveryFee == 0.0) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (orderType == "Delivery" && selectedZone.fee > 0.0) {
                                                Text(
                                                    text = "₹${selectedZone.fee.toInt()}",
                                                    color = TextMuted,
                                                    fontSize = 11.sp,
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = "FREE",
                                                color = AccentGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "₹${deliveryFee.toInt()}",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Taxes & Charges (GST 5%)", color = TextSecondary, fontSize = 13.sp)
                                    Text("₹${tax.toInt()}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(color = DividerColor)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Grand Total", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        if (discount > 0.0) {
                                            Text(
                                                text = "Original: ₹${(subtotal + kotlin.math.round(subtotal * 0.05)).toInt()}",
                                                color = TextMuted,
                                                fontSize = 11.sp,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                        }
                                    }
                                    Text(
                                        text = "₹${total.toInt()}",
                                        color = GoldPrimary,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                if (discount > 0.0) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AccentGreen.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🎉", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "You are saving ₹${discount.toInt()} on this order!",
                                            color = AccentGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Sticky Bottom Action Button (Play Store Compliant)
                val effectiveAddress = when (orderType) {
                    "Dine-In" -> "🍽️ Dine-in: $tableNumber (The Somafern Cafe)"
                    "Takeaway" -> "🛍️ Takeaway / Counter Pickup (The Somafern Cafe)"
                    else -> deliveryAddress
                }

                val canProceed = !hasUnavailableItems && isStoreOpen && !isLoading
                val buttonBackground = if (!canProceed) Color(0xFF2D3748) else GoldPrimary
                val buttonTextColor = if (!canProceed) TextMuted else NavyBackground
                val buttonText = when {
                    !isStoreOpen -> "Café is Currently Offline / Paused ☕"
                    hasUnavailableItems -> "Remove Out-of-Stock Items to Order ⚠️"
                    isLoading -> "Placing Order..."
                    else -> "Confirm & Place Order • ₹${total.toInt()}"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(buttonBackground)
                        .clickable(enabled = canProceed) {
                            val method = if (orderType == "Dine-In") "Pay at Table / Counter" else if (orderType == "Takeaway") "Pay at Counter on Pickup" else "Cash / Pay on Delivery"
                            viewModel.placeOrder(
                                customerName = customerName,
                                customerPhone = customerPhone,
                                deliveryAddress = effectiveAddress,
                                paymentMethod = method,
                                paymentStatus = "Pay on Delivery",
                                orderType = orderType,
                                deliveryZone = selectedZone.name
                            ) { order ->
                                OrderUtils.playOrderConfirmationTone(context)
                                OrderUtils.sendOrderToWhatsApp(context, order)
                                onNavigate(Screen.OrderTracking.route)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = NavyBackground,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (canProceed) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = buttonTextColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = buttonText,
                                color = buttonTextColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

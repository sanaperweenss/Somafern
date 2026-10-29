package com.example.somafern.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somafern.data.model.CartItem
import com.example.somafern.data.model.CustomerReview
import com.example.somafern.data.model.Order
import com.example.somafern.data.repository.FirebaseRepository
import com.example.somafern.ui.components.SomafernFoodImage
import com.example.somafern.ui.navigation.Screen
import com.example.somafern.ui.theme.AccentGreen
import com.example.somafern.ui.theme.DangerRed
import com.example.somafern.ui.theme.GoldLight
import com.example.somafern.ui.theme.GoldPrimary
import com.example.somafern.ui.theme.NavyBackground
import com.example.somafern.ui.theme.NavyBorder
import com.example.somafern.ui.theme.NavyCard
import com.example.somafern.ui.theme.StarYellow
import com.example.somafern.ui.theme.TextMuted
import com.example.somafern.ui.theme.TextPrimary
import com.example.somafern.ui.theme.TextSecondary
import com.example.somafern.viewmodel.SomafernViewModel
import kotlinx.coroutines.flow.collect

@Composable
fun OrderTrackingScreen(
    viewModel: SomafernViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val currentOrder by viewModel.currentOrder.collectAsState()
    val scrollState = rememberScrollState()

    var userRating by remember { mutableIntStateOf(5) }
    var hasSubmittedRating by remember { mutableStateOf(false) }

    val liveOrder by produceState<Order?>(initialValue = currentOrder, key1 = currentOrder?.id) {
        val oId = currentOrder?.id
        if (!oId.isNullOrBlank()) {
            viewModel.listenToOrder(oId).collect { updated: Order? ->
                if (updated != null) {
                    value = updated
                }
            }
        }
    }

    val order = liveOrder ?: currentOrder
    val currentStatus = order?.status ?: "Order Placed"
    val isCancelled = currentStatus.equals("Cancelled", ignoreCase = true) || currentStatus.equals("Rejected", ignoreCase = true)

    val activeStepIndex = when {
        isCancelled -> -1
        currentStatus == "Order Placed" -> 0
        currentStatus == "Preparing" -> 1
        currentStatus in listOf("On the Way", "Ready for Pickup", "Out for Delivery") -> 2
        currentStatus in listOf("Delivered", "Completed") -> 3
        else -> 1
    }

    val currentStatusText = when {
        isCancelled -> "Order Declined 🛑"
        activeStepIndex == 0 -> "Order Placed"
        activeStepIndex == 1 -> "Preparing in Kitchen 🔥"
        activeStepIndex == 2 -> if (order?.orderType == "Dine-In" || order?.orderType == "Takeaway") "Ready at Counter 🍽️" else "Out for Delivery 🛵"
        activeStepIndex == 3 -> if (order?.orderType == "Dine-In") "Served & Enjoy 🎉" else "Delivered 🎉"
        else -> "Processing"
    }

    val orderId = order?.id ?: "778450"
    val orderDate = order?.formattedDate?.ifBlank { "Today" } ?: "Today"
    val defaultCartItems = listOfNotNull(
        FirebaseRepository.defaultFoodItems.getOrNull(0)?.let { CartItem(it, 1) },
        FirebaseRepository.defaultFoodItems.getOrNull(2)?.let { CartItem(it, 1) }
    )
    val items = order?.items?.ifEmpty { defaultCartItems } ?: defaultCartItems

    val step2Title = if (order?.orderType == "Dine-In" || order?.orderType == "Takeaway") "Ready" else "On the Way"
    val step3Title = if (order?.orderType == "Dine-In") "Served" else "Delivered"

    val steps = listOf(
        TrackingStep("Order Placed", Icons.Default.Receipt, isCompleted = activeStepIndex > 0, isCurrent = activeStepIndex == 0),
        TrackingStep("Preparing", Icons.Default.OutdoorGrill, isCompleted = activeStepIndex > 1, isCurrent = activeStepIndex == 1),
        TrackingStep(step2Title, Icons.Default.DeliveryDining, isCompleted = activeStepIndex > 2, isCurrent = activeStepIndex == 2),
        TrackingStep(step3Title, Icons.Default.Done, isCompleted = activeStepIndex == 3, isCurrent = activeStepIndex == 3)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onNavigate(Screen.Home.route) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Order #$orderId",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = orderDate,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isCancelled) DangerRed else if (activeStepIndex == 3) AccentGreen else GoldPrimary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentStatusText,
                    color = if (isCancelled) Color.White else NavyBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (isCancelled) {
                // Dedicated Cancelled / Rejected Banner Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DangerRed.copy(alpha = 0.12f))
                        .border(1.2.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛑", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Order Declined by Café",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "The kitchen cannot fulfill this order at this time.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Reason: Raw ingredient shortage or peak-hour kitchen rush. No payment has been charged. You can call the café directly to ask for freshly available alternatives or browse the menu.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            } else {
                // Visual Stepper Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 20.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, step ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (step.isCurrent) GoldPrimary else if (step.isCompleted) NavyCard else NavyBackground)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (step.isCurrent || step.isCompleted) GoldPrimary else NavyBorder,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = step.icon,
                                        contentDescription = step.title,
                                        tint = if (step.isCurrent) NavyBackground else if (step.isCompleted) GoldPrimary else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = step.title,
                                    color = if (step.isCurrent) GoldPrimary else if (step.isCompleted) TextPrimary else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.6f)
                                        .height(2.dp)
                                        .background(if (step.isCompleted) GoldPrimary else NavyBorder)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order Items Summary
            Text(
                text = "Order Details",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items.forEach { cartItem ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                SomafernFoodImage(
                                    model = cartItem.foodItem.imageUrl,
                                    contentDescription = cartItem.foodItem.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    drawableRes = cartItem.foodItem.drawableRes
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cartItem.foodItem.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (cartItem.selectedAddOns.isNotEmpty()) {
                                    Text(
                                        text = "+ " + cartItem.selectedAddOns.joinToString(", ") { it.name },
                                        color = GoldLight,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }

                            Text(
                                text = "×${cartItem.quantity}",
                                color = TextMuted,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text(
                                text = "₹${cartItem.totalPrice.toInt()}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Delivery Address Card
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delivery Address",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "View Map",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                val gmmIntentUri = Uri.parse("geo:31.2240,75.7708?q=Law+Gate+Rd,+Phagwara,+Punjab")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {}
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = order?.deliveryAddress?.ifBlank { "Law Gate Rd, Phagwara, Punjab" } ?: "Law Gate Rd, Phagwara, Punjab",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(start = 28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rate Meal & Experience Card (Only if not cancelled)
            if (!isCancelled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyCard)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rate Your Meal & Experience 🌟",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your feedback helps us keep food ratings fresh & accurate!",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (hasSubmittedRating) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AccentGreen.copy(alpha = 0.15f))
                                    .border(1.dp, AccentGreen, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Thank you for rating! ⭐ ($userRating Stars)",
                                    color = AccentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarOutline,
                                        contentDescription = "Star $star",
                                        tint = if (star <= userRating) StarYellow else TextMuted,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clickable { userRating = star }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GoldPrimary)
                                    .clickable {
                                        hasSubmittedRating = true
                                        items.forEach { cartItem ->
                                            if (cartItem.foodItem.id.isNotBlank()) {
                                                viewModel.rateItem(cartItem.foodItem.id, userRating.toDouble())
                                            }
                                        }
                                        val reviewComment = when (userRating) {
                                            5 -> "Amazing food and super fast service! Loved it! ⭐⭐⭐⭐⭐"
                                            4 -> "Tasty food, well prepared and good experience! ⭐⭐⭐⭐"
                                            3 -> "Average taste, expected slightly better."
                                            2 -> "Need improvement in taste and packing."
                                            else -> "Disappointed with the order."
                                        }
                                        val newReview = CustomerReview(
                                            id = "rev_${System.currentTimeMillis()}",
                                            orderId = orderId,
                                            customerName = order?.customerName?.ifBlank { "Valued Guest" } ?: "Valued Guest",
                                            customerPhone = order?.customerPhone ?: "+91 7321962452",
                                            rating = userRating,
                                            reviewText = reviewComment,
                                            timeAgo = "Just now"
                                        )
                                        viewModel.submitCustomerReview(newReview)
                                        Toast.makeText(context, "Review & rating submitted to café! Thank you 🎉", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Submit Rating",
                                    color = NavyBackground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Call Restaurant Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.2.dp, GoldPrimary, RoundedCornerShape(14.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+917321962452"))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Call Restaurant (+91 7321962452)",
                        color = GoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Back to Home Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldPrimary)
                    .clickable { onNavigate(Screen.Home.route) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isCancelled) "Browse Fresh Menu" else "Back to Home",
                    color = NavyBackground,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class TrackingStep(
    val title: String,
    val icon: ImageVector,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

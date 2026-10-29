package com.example.somafern.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somafern.data.model.CartAddOn
import com.example.somafern.data.model.FoodItem
import com.example.somafern.ui.theme.AccentGreen
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

/**
 * Returns dynamic, tailored cafe add-ons based on the dish type or category
 */
fun getAddOnsForDish(dish: FoodItem): List<CartAddOn> {
    val category = dish.category.lowercase()
    val name = dish.name.lowercase()

    return when {
        category.contains("drink") || category.contains("sip") || category.contains("shake") ||
        category.contains("coffee") || category.contains("cooler") || category.contains("frappe") ||
        name.contains("frappe") || name.contains("mojito") || name.contains("coffee") || name.contains("shake") -> {
            listOf(
                CartAddOn(name = "Extra Whipped Cream", price = 30.0),
                CartAddOn(name = "Caramel Drizzle", price = 25.0),
                CartAddOn(name = "Chocolate Syrup & Choco Chips", price = 25.0),
                CartAddOn(name = "Extra Espresso Shot", price = 40.0),
                CartAddOn(name = "Vanilla Ice Cream Scoop", price = 40.0)
            )
        }
        category.contains("combo") || category.contains("special") || name.contains("combo") || name.contains("feast") -> {
            listOf(
                CartAddOn(name = "Crispy French Fries (Peri Peri)", price = 50.0),
                CartAddOn(name = "Upgrade Drink to Large Frappe", price = 45.0),
                CartAddOn(name = "Extra Cheesy Dip", price = 30.0),
                CartAddOn(name = "Warm Choco Lava Cake", price = 79.0)
            )
        }
        category.contains("momo") || name.contains("momo") -> {
            listOf(
                CartAddOn(name = "Extra Spicy Schezwan Dip", price = 25.0),
                CartAddOn(name = "Creamy Garlic Mayo", price = 25.0),
                CartAddOn(name = "Melted Cheese Topping", price = 35.0),
                CartAddOn(name = "Crispy Fried Upgrade", price = 30.0)
            )
        }
        category.contains("noodle") || category.contains("ramen") || category.contains("thupka") || name.contains("ramen") || name.contains("noodle") -> {
            listOf(
                CartAddOn(name = "Grilled Paneer Cubes", price = 45.0),
                CartAddOn(name = "Exotic Sautéed Veggies", price = 35.0),
                CartAddOn(name = "Chilli Garlic Crispy Crunch", price = 20.0),
                CartAddOn(name = "Spicy Sesame Chili Oil", price = 20.0)
            )
        }
        else -> {
            // Pasta / Pizza / Burgers / Sandwiches / General
            listOf(
                CartAddOn(name = "Extra Mozzarella Cheese", price = 40.0),
                CartAddOn(name = "Toasted Garlic Bread (2 pcs)", price = 55.0),
                CartAddOn(name = "Sautéed Mushroom & Olives", price = 35.0),
                CartAddOn(name = "Spicy Jalapeno Dip", price = 25.0),
                CartAddOn(name = "Grilled Herb Paneer", price = 45.0)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DishDetailsBottomSheet(
    dish: FoodItem,
    onDismiss: () -> Unit,
    onAddToCart: (item: FoodItem, quantity: Int, selectedAddOns: List<CartAddOn>, instructions: String) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var quantity by remember { mutableIntStateOf(1) }
    val availableAddOns = remember(dish) { getAddOnsForDish(dish) }
    val selectedAddOns = remember { mutableStateListOf<CartAddOn>() }

    val isDrink = remember(dish) {
        val cat = dish.category.lowercase()
        val n = dish.name.lowercase()
        cat.contains("drink") || cat.contains("sip") || cat.contains("shake") ||
        cat.contains("coffee") || cat.contains("cooler") || cat.contains("frappe") ||
        n.contains("frappe") || n.contains("mojito") || n.contains("coffee") || n.contains("shake")
    }

    var selectedPreference by remember {
        mutableStateOf(if (isDrink) "Normal Ice ❄️" else "Medium Spice 🌶️")
    }

    var specialInstructions by remember { mutableStateOf("") }

    val basePrice = dish.price
    val addOnsTotal = selectedAddOns.sumOf { it.price }
    val unitPrice = basePrice + addOnsTotal
    val totalPrice = unitPrice * quantity

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavyCard,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoldPrimary.copy(alpha = 0.45f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Header Row (Category pill + Close Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Category Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldPrimary.copy(alpha = 0.12f))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = dish.category.uppercase(),
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            )
                        }

                        if (dish.isPopular || dish.isTodaySpecial) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AccentGreen.copy(alpha = 0.15f))
                                    .border(1.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (dish.isTodaySpecial) "CHEF'S SPECIAL ⭐" else "POPULAR 🔥",
                                    color = AccentGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Circular Close Button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NavyBackground)
                            .border(1.dp, NavyBorder, CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Image with Rating & Veg badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavyBackground)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                ) {
                    SomafernFoodImage(
                        model = dish.imageUrl,
                        contentDescription = dish.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(210.dp),
                        drawableRes = dish.drawableRes
                    )

                    // Floating Star Rating Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NavyBackground.copy(alpha = 0.85f))
                            .border(1.dp, StarYellow.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", dish.rating),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Veg / Non-Veg Indicator Badge (Top Left)
                    FoodVegIndicator(
                        dish = dish,
                        size = 24.dp,
                        dotSize = 9.dp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dish Title & Base Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dish.name,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        )

                        if (dish.offerTag.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dish.offerTag,
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "₹${dish.price.toInt()}",
                        color = GoldPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Full Dish Description
                Text(
                    text = dish.description.ifBlank {
                        "Prepared fresh to order in the Somafern kitchen using artisan ingredients and traditional recipes."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = NavyBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // PREFERENCE CUSTOMIZATION CHIPS (Ice or Spice)
                Text(
                    text = if (isDrink) "Temperature & Ice Preference" else "Spice Level Preference",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val preferences = if (isDrink) {
                    listOf("Normal Ice ❄️", "Less Ice", "No Ice ☕")
                } else {
                    listOf("Mild 🍃", "Medium Spice 🌶️", "Extra Spicy 🔥")
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    preferences.forEach { pref ->
                        val isSelected = selectedPreference == pref
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else NavyBackground)
                                .border(
                                    1.dp,
                                    if (isSelected) GoldPrimary else NavyBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedPreference = pref }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = pref,
                                color = if (isSelected) GoldLight else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = NavyBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // ADD-ONS SECTION HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Customize with Add-ons",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(Optional)",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Select any delicious extras you'd like to add to this item",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List of Add-ons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableAddOns.forEach { addOn ->
                        val isChecked = selectedAddOns.any { it.name == addOn.name }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isChecked) NavyBackground else NavyBackground.copy(alpha = 0.6f))
                                .border(
                                    1.dp,
                                    if (isChecked) GoldPrimary.copy(alpha = 0.8f) else NavyBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (isChecked) {
                                        selectedAddOns.removeAll { it.name == addOn.name }
                                    } else {
                                        selectedAddOns.add(addOn)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Custom Checkbox Box
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isChecked) GoldPrimary else NavyCard)
                                        .border(
                                            1.dp,
                                            if (isChecked) GoldPrimary else NavyBorder,
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = NavyBackground,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = addOn.name,
                                    color = if (isChecked) TextPrimary else TextSecondary,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            Text(
                                text = "+₹${addOn.price.toInt()}",
                                color = GoldPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // SPECIAL INSTRUCTIONS TEXT FIELD
                Text(
                    text = "Special Kitchen Instructions",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it.take(120) },
                    placeholder = {
                        Text("e.g. Less spicy, make it extra crispy, serve hot...", color = TextMuted, fontSize = 12.sp)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NavyInput,
                        unfocusedContainerColor = NavyInput,
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = NavyBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // STICKY BOTTOM ACTION BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBackground)
                    .border(1.dp, NavyBorder)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (dish.isAvailable) {
                        // Modern Stepper [- 1 +]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { if (quantity > 1) quantity-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = if (quantity > 1) GoldPrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = quantity.toString(),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable { if (quantity < 20) quantity++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Big Gold "Add to Cart • ₹XXX" Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
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
                                    val fullInstructions = if (specialInstructions.isNotBlank()) {
                                        "${selectedPreference.trim()} • ${specialInstructions.trim()}"
                                    } else {
                                        selectedPreference.trim()
                                    }

                                    onAddToCart(
                                        dish,
                                        quantity,
                                        selectedAddOns.toList(),
                                        fullInstructions
                                    )

                                    val addOnsNote = if (selectedAddOns.isNotEmpty()) " with ${selectedAddOns.size} add-on(s)" else ""
                                    Toast.makeText(
                                        context,
                                        "Added ${quantity}x ${dish.name}$addOnsNote to cart! 🛍️",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    onDismiss()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color(0xFF1E1003),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Add to Cart • ₹${totalPrice.toInt()}",
                                    color = Color(0xFF1E1003),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Out of Stock Status Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NavyCard)
                                .border(1.2.dp, com.example.somafern.ui.theme.DangerRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Currently Out of Stock 🔴",
                                color = com.example.somafern.ui.theme.DangerRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

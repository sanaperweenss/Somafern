package com.example.somafern.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somafern.ui.components.FoodCard
import com.example.somafern.ui.components.SomafernSearchBar
import com.example.somafern.ui.components.StandardTopBar
import com.example.somafern.ui.navigation.Screen
import com.example.somafern.ui.theme.GoldPrimary
import com.example.somafern.ui.theme.NavyBackground
import com.example.somafern.ui.theme.NavyBorder
import com.example.somafern.ui.theme.NavyCard
import com.example.somafern.ui.theme.NavyInput
import com.example.somafern.ui.theme.TextMuted
import com.example.somafern.ui.theme.TextPrimary
import com.example.somafern.ui.theme.TextSecondary
import com.example.somafern.viewmodel.SomafernViewModel

@Composable
fun MenuScreen(
    viewModel: SomafernViewModel,
    onNavigate: (String) -> Unit
) {
    val menuItems by viewModel.menuItems.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favoriteItemIds by viewModel.favoriteItemIds.collectAsState()
    val cartCount = viewModel.getCartCount()

    val categories = listOf(
        "All Dishes", "❤️ Favorites", "Pasta", "Momos", "Noodles", "Ramen",
        "Thupka", "Sandwiches", "Maggie", "Drinks", "Somafern Special",
        "Fried Rice", "Salad"
    )

    val filteredItems = menuItems.filter { item ->
        val matchesCategory = when (selectedCategory) {
            "All Dishes" -> true
            "❤️ Favorites" -> favoriteItemIds.contains(item.id)
            else -> item.category.equals(selectedCategory, ignoreCase = true)
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            item.name.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        StandardTopBar(
            title = "Menu",
            onBackClick = { onNavigate(Screen.Home.route) },
            cartCount = cartCount,
            onCartClick = { onNavigate(Screen.Cart.route) }
        )

        // Search Bar
        SomafernSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            placeholderText = "Search dishes, coffee, pasta...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                val count = when (category) {
                    "All Dishes" -> menuItems.size
                    "❤️ Favorites" -> favoriteItemIds.size
                    else -> menuItems.count { it.category.equals(category, ignoreCase = true) }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) GoldPrimary else NavyCard)
                        .border(1.dp, if (isSelected) GoldPrimary else NavyBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.setCategory(category) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) NavyBackground else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) NavyBackground.copy(alpha = 0.85f) else NavyBorder.copy(alpha = 0.6f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = count.toString(),
                            color = if (isSelected) GoldPrimary else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredItems.isEmpty()) {
            val emptyTitle = when {
                selectedCategory == "❤️ Favorites" -> "No Favorite Dishes Yet ❤️"
                searchQuery.isNotBlank() -> "No Matching Dishes Found"
                selectedCategory != "All Dishes" -> "No Dishes in '$selectedCategory'"
                else -> "Menu Updating Soon ☕"
            }

            val emptyMessage = when {
                selectedCategory == "❤️ Favorites" -> "Tap the heart icon on any dish in the menu to save it to your favorites list!"
                searchQuery.isNotBlank() -> "No dishes match '$searchQuery'. Try searching for another term!"
                selectedCategory != "All Dishes" -> "No dishes available under '$selectedCategory'. Try browsing 'All Dishes'!"
                else -> "Our chefs are updating the menu with fresh handcrafted dishes. Please check back shortly!"
            }

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
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NavyCard)
                            .border(1.dp, NavyBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedCategory == "❤️ Favorites") "❤️" else "☕",
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = emptyTitle,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = emptyMessage,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary)
                            .clickable {
                                viewModel.setCategory("All Dishes")
                                viewModel.setSearchQuery("")
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Explore All Dishes 🍽️",
                            color = NavyBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Food Items List
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val qtyInCart = cartItems.find { it.foodItem.id == item.id }?.quantity ?: 0

                    FoodCard(
                        item = item,
                        quantityInCart = qtyInCart,
                        isFavorite = favoriteItemIds.contains(item.id),
                        onFavoriteToggle = { viewModel.toggleFavorite(item.id) },
                        onAddClick = { viewModel.addToCart(item) },
                        onIncrement = { viewModel.incrementQuantity(item.id) },
                        onDecrement = { viewModel.decrementQuantity(item.id) },
                        onItemClick = { viewModel.showDishDetails(item) }
                    )
                }
            }
        }
    }
}

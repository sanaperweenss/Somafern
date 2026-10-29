package com.example.somafern.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Menu : Screen("menu")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object OrderTracking : Screen("order_tracking")
    object AdminAddItem : Screen("admin_add_item")
}

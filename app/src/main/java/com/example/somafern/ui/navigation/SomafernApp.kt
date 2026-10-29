package com.example.somafern.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somafern.ui.theme.GoldLight
import com.example.somafern.ui.theme.GoldPrimary
import com.example.somafern.ui.theme.NavyBackground
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.somafern.ui.components.CartBottomBar
import com.example.somafern.ui.components.DishDetailsBottomSheet
import com.example.somafern.ui.components.SomafernBottomNav
import com.example.somafern.ui.screens.AdminAddItemScreen
import com.example.somafern.ui.screens.AuthScreen
import com.example.somafern.ui.screens.CartScreen
import com.example.somafern.ui.screens.HomeScreen
import com.example.somafern.ui.screens.MenuScreen
import com.example.somafern.ui.screens.OrderTrackingScreen
import com.example.somafern.ui.screens.ProfileScreen
import com.example.somafern.ui.screens.SplashScreen
import com.example.somafern.ui.theme.NavyBackground
import com.example.somafern.viewmodel.SomafernViewModel

@Composable
fun SomafernApp(
    viewModel: SomafernViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.example.somafern.data.repository.FirebaseRepository.restoreGoogleUserData(context)
    }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val cartItems by viewModel.cartItems.collectAsState()
    val cartCount = viewModel.getCartCount()
    val selectedDishForDetails by viewModel.selectedDishForDetails.collectAsState()
    val lastAddedItemToast by viewModel.lastAddedItemToast.collectAsState()

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Menu.route,
        Screen.Cart.route,
        Screen.Profile.route
    )

    Scaffold(
        containerColor = NavyBackground,
        bottomBar = {
            if (showBottomBar) {
                Column {
                    if ((currentRoute == Screen.Home.route || currentRoute == Screen.Menu.route) && cartCount > 0) {
                        CartBottomBar(
                            cartCount = cartCount,
                            totalPrice = viewModel.getTotal(),
                            onViewCartClick = {
                                navController.navigate(Screen.Cart.route)
                            }
                        )
                    }

                    SomafernBottomNav(
                        currentRoute = currentRoute,
                        cartCount = cartCount,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onNavigateNext = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Auth.route) {
                    AuthScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Auth.route) { inclusive = true }
                            }
                        },
                        onOpenAdmin = {
                            navController.navigate(Screen.AdminAddItem.route)
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.Menu.route) {
                    MenuScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.Cart.route) {
                    CartScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }

                composable(Screen.OrderTracking.route) {
                    OrderTrackingScreen(
                        viewModel = viewModel,
                        onNavigate = { route ->
                            if (route == Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = false }
                                }
                            } else {
                                navController.navigate(route)
                            }
                        }
                    )
                }

                composable(Screen.AdminAddItem.route) {
                    AdminAddItemScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Global Dish Details Bottom Sheet with Add-ons
            selectedDishForDetails?.let { dish ->
                DishDetailsBottomSheet(
                    dish = dish,
                    onDismiss = { viewModel.dismissDishDetails() },
                    onAddToCart = { item, quantity, selectedAddOns, instructions ->
                        viewModel.addCustomizedToCart(item, quantity, selectedAddOns, instructions)
                    }
                )
            }

            // Global Add To Cart Toast Banner (Auto-dismiss & Only on Home/Menu)
            LaunchedEffect(lastAddedItemToast) {
                if (lastAddedItemToast != null) {
                    kotlinx.coroutines.delay(3500)
                    viewModel.clearAddToCartToast()
                }
            }

            AnimatedVisibility(
                visible = (currentRoute == Screen.Home.route || currentRoute == Screen.Menu.route) && lastAddedItemToast != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF090D16)
                                )
                            )
                        )
                        .border(1.2.dp, GoldPrimary, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, GoldPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🛒", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lastAddedItemToast ?: "",
                                color = GoldLight,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Professional View Cart Pill Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldPrimary)
                                .clickable {
                                    viewModel.clearAddToCartToast()
                                    navController.navigate(Screen.Cart.route)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "View Cart →",
                                color = NavyBackground,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

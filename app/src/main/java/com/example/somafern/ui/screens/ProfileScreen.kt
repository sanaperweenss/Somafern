package com.example.somafern.ui.screens

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.somafern.data.repository.FirebaseRepository
import com.example.somafern.ui.components.StandardTopBar
import com.example.somafern.ui.navigation.Screen
import com.example.somafern.ui.theme.AccentGreen
import com.example.somafern.ui.theme.DangerRed
import com.example.somafern.ui.theme.GoldLight
import com.example.somafern.ui.theme.GoldPrimary
import com.example.somafern.ui.theme.NavyBackground
import com.example.somafern.ui.theme.NavyBorder
import com.example.somafern.ui.theme.NavyCard
import com.example.somafern.ui.theme.TextMuted
import com.example.somafern.ui.theme.TextPrimary
import com.example.somafern.ui.theme.TextSecondary
import com.example.somafern.viewmodel.SomafernViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SomafernViewModel,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val userEmail = FirebaseRepository.getCurrentUserEmail() ?: "guest@somafern.com"
    val userName = remember(userEmail) {
        val customName = FirebaseRepository.getCurrentUserName()
        if (!customName.isNullOrBlank()) customName
        else {
            val base = userEmail.substringBefore("@")
            if (base.equals("guest", ignoreCase = true)) "SOMAFERN Guest"
            else base.replaceFirstChar { it.uppercase() } + " Member"
        }
    }
    val userPhoto = FirebaseRepository.getCurrentUserPhoto()
    val isGoogleUser = FirebaseRepository.isGoogleUser()
    val scrollState = rememberScrollState()

    val userOrders by viewModel.userOrders.collectAsState()
    val savedAddresses by viewModel.savedAddresses.collectAsState()
    val selectedAddress by viewModel.selectedAddress.collectAsState()
    val loyaltyStamps by viewModel.loyaltyStamps.collectAsState()
    val cafeCoins by viewModel.cafeCoins.collectAsState()
    val selectedAvatarEmoji by viewModel.selectedAvatarEmoji.collectAsState()

    var showAddressManager by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }

    val avatarOptions = listOf(
        Pair("👑", "Royal Chef • Master of Gourmet Comfort"),
        Pair("☕", "Coffee Master • Artisan Brew Connoisseur"),
        Pair("🍝", "Pasta Connoisseur • White & Red Sauce Expert"),
        Pair("🥟", "Momo Lover • Steamed & Fried Delights"),
        Pair("🍕", "Pizza Gourmet • Cheesy Crust Enthusiast"),
        Pair("🍹", "Shake Artisan • Coolers & Thick Shakes")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
    ) {
        StandardTopBar(
            title = "My Profile"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // PRODUCTION-GRADE HERO USER IDENTITY CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF241C14),
                                Color(0xFF16213A),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(1.2.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Container with Edit Pencil Badge
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(NavyCard)
                            .border(2.dp, GoldPrimary, CircleShape)
                            .clickable { showAvatarSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userPhoto.isNullOrBlank()) {
                            AsyncImage(
                                model = userPhoto,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = selectedAvatarEmoji,
                                fontSize = 38.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✏️", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = userName,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isGoogleUser) Color(0xFF4285F4) else GoldPrimary)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isGoogleUser) "GOOGLE VERIFIED ✔" else "VIP GOLD",
                                    color = if (isGoogleUser) Color.White else NavyBackground,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = userEmail,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .clickable { showAvatarSheet = true }
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Change Persona ✨", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PRODUCTION QUICK METRICS SUMMARY GRID
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(NavyCard)
                    .border(1.dp, NavyBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${userOrders.size}", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(text = "Orders 📦", color = TextMuted, fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(NavyBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$cafeCoins", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(text = "Coins 🪙", color = TextMuted, fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(NavyBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$loyaltyStamps/5", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(text = "Stamps ☕", color = TextMuted, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SOMAFERN LOYALTY STAMP CARD & CAFE COINS REWARDS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF322417),
                                Color(0xFF1B2A4A),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .border(1.2.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "$cafeCoins Cafe Coins",
                                    color = GoldLight,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Redeem on checkout for instant savings",
                                    color = TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(GoldPrimary)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "STAMP CARD ☕",
                                color = NavyBackground,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Collect 5 stamps to earn a FREE Hazelnut Frappe! 🎉",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stamp Grid (5 Stamps)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..5).forEach { stampNum ->
                            val isCollected = stampNum <= loyaltyStamps
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isCollected) GoldPrimary else NavyCard)
                                    .border(1.2.dp, if (isCollected) GoldPrimary else NavyBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isCollected) "☕" else "$stampNum",
                                    color = if (isCollected) NavyBackground else TextMuted,
                                    fontSize = if (isCollected) 18.sp else 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RECENT ORDERS WITH 1-TAP RE-ORDER
            if (userOrders.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Orders 🕒",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View Active >",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigate(Screen.OrderTracking.route) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                userOrders.take(3).forEach { pastOrder ->
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
                                Text(
                                    text = "Order #${pastOrder.id}",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentGreen.copy(alpha = 0.15f))
                                        .border(1.dp, AccentGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Delivered ✓",
                                        color = AccentGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = pastOrder.items.joinToString(", ") { "${it.foodItem.name} ×${it.quantity}" },
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total: ₹${pastOrder.total.toInt()}",
                                    color = GoldPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(GoldPrimary)
                                        .clickable {
                                            viewModel.repeatOrder(pastOrder)
                                            onNavigate(Screen.Cart.route)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Repeat Order 🔄",
                                            color = NavyBackground,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // PRODUCTION MENU OPTIONS
            ProfileOptionItem(
                icon = Icons.Default.LocationOn,
                title = "Saved Addresses (${savedAddresses.size})",
                subtitle = selectedAddress,
                onClick = { showAddressManager = !showAddressManager }
            )

            if (showAddressManager) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    savedAddresses.forEach { addr ->
                        val isSelected = addr.fullAddress == selectedAddress
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) GoldPrimary.copy(alpha = 0.15f) else NavyCard)
                                .border(1.dp, if (isSelected) GoldPrimary else NavyBorder, RoundedCornerShape(14.dp))
                                .clickable { viewModel.selectAddress(addr.fullAddress) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = addr.label, color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = addr.fullAddress,
                                color = TextSecondary,
                                fontSize = 11.5.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOptionItem(
                icon = Icons.Default.Phone,
                title = "Call Restaurant Owner",
                subtitle = "+91 7321962452 • Direct line for reservations & support",
                iconTint = GoldPrimary,
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+917321962452"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOptionItem(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "WhatsApp Support",
                subtitle = "Chat live with our cafe team on WhatsApp",
                iconTint = AccentGreen,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/917321962452?text=Hello%20Somafern%20Cafe!"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileOptionItem(
                icon = Icons.Default.Restaurant,
                title = "Cafe FAQs & Help Center",
                subtitle = "Timings, location, and ordering guidelines",
                iconTint = GoldLight,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/917321962452?text=Hello%20Somafern,%20I%20need%20help!"))
                    try { context.startActivity(intent) } catch (_: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Secure Sign Out Option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(NavyCard)
                    .border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable {
                        try {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                            val client = GoogleSignIn.getClient(context, gso)
                            client.signOut()
                        } catch (_: Exception) {}
                        viewModel.signOut(context) {
                            onNavigate(Screen.Auth.route)
                        }
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = DangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Sign Out",
                        color = DangerRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // CUTE AVATAR SELECTION MODAL BOTTOM SHEET
        if (showAvatarSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAvatarSheet = false },
                containerColor = NavyCard,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Select Your Cute Avatar ✨",
                        color = GoldLight,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Choose a cute cafe persona for your profile!",
                        color = TextSecondary,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        avatarOptions.forEach { (emoji, label) ->
                            val isChosen = selectedAvatarEmoji == emoji
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isChosen) GoldPrimary.copy(alpha = 0.15f) else NavyBackground)
                                    .border(1.dp, if (isChosen) GoldPrimary else NavyBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.selectAvatarEmoji(emoji)
                                        showAvatarSheet = false
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(NavyCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = label,
                                    color = if (isChosen) GoldLight else TextPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isChosen) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GoldPrimary)
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            color = NavyBackground,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color = GoldPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

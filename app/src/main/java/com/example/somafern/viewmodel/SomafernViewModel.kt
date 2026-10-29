package com.example.somafern.viewmodel

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somafern.data.model.*
import com.example.somafern.data.repository.FirebaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider

class SomafernViewModel : ViewModel() {

    private val _menuItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val menuItems: StateFlow<List<FoodItem>> = _menuItems.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _addCutlery = MutableStateFlow(true)
    val addCutlery: StateFlow<Boolean> = _addCutlery.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders.asStateFlow()

    private val _favoriteItemIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteItemIds: StateFlow<Set<String>> = _favoriteItemIds.asStateFlow()

    private val defaultAddresses = listOf(
        AddressItem("addr_home", "Home 🏠", "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab", true),
        AddressItem("addr_campus", "Hostel / Campus 🏫", "Block 34, LPU Campus, Phagwara, Punjab", false),
        AddressItem("addr_work", "Work 🏢", "IT Park, Law Gate Rd, Phagwara, Punjab", false)
    )
    private val _savedAddresses = MutableStateFlow<List<AddressItem>>(defaultAddresses)
    val savedAddresses: StateFlow<List<AddressItem>> = _savedAddresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow("hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab")
    val selectedAddress: StateFlow<String> = _selectedAddress.asStateFlow()

    private val _lastAddedItemToast = MutableStateFlow<String?>(null)
    val lastAddedItemToast: StateFlow<String?> = _lastAddedItemToast.asStateFlow()

    private val _loyaltyStamps = MutableStateFlow(3)
    val loyaltyStamps: StateFlow<Int> = _loyaltyStamps.asStateFlow()

    private val _cafeCoins = MutableStateFlow(120)
    val cafeCoins: StateFlow<Int> = _cafeCoins.asStateFlow()

    private val _selectedAvatarEmoji = MutableStateFlow("👑")
    val selectedAvatarEmoji: StateFlow<String> = _selectedAvatarEmoji.asStateFlow()

    private val defaultNotifications = listOf(
        ActivityNotification(
            id = "notif_welcome",
            title = "Welcome to SOMAFERN ✨",
            message = "Explore handcrafted pastas, momos, shakes & cafe specials!",
            formattedTime = "Today",
            iconEmoji = "☕",
            isRead = false
        ),
        ActivityNotification(
            id = "notif_coupon",
            title = "Happy Hours Voucher Active ⚡",
            message = "Use code SOMA15 for Flat 15% OFF on your order!",
            formattedTime = "Today",
            iconEmoji = "🎁",
            isRead = false
        ),
        ActivityNotification(
            id = "notif_coins",
            title = "120 Cafe Coins Earned 🪙",
            message = "Collect 5 stamps to redeem a FREE Hazelnut Frappe!",
            formattedTime = "Yesterday",
            iconEmoji = "🪙",
            isRead = false
        )
    )

    private val _notifications = MutableStateFlow<List<ActivityNotification>>(defaultNotifications)
    val notifications: StateFlow<List<ActivityNotification>> = _notifications.asStateFlow()

    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All Dishes")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(emptyList())
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedDishForDetails = MutableStateFlow<FoodItem?>(null)
    val selectedDishForDetails: StateFlow<FoodItem?> = _selectedDishForDetails.asStateFlow()

    private val _liveOrders = MutableStateFlow<List<Order>>(emptyList())
    val liveOrders: StateFlow<List<Order>> = _liveOrders.asStateFlow()

    // 15-Screen Admin Panel States
    private val _deliveryZones = MutableStateFlow<List<DeliveryZone>>(FirebaseRepository.defaultDeliveryZones)
    val deliveryZones: StateFlow<List<DeliveryZone>> = _deliveryZones.asStateFlow()

    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(FirebaseRepository.defaultInventoryItems)
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()

    private val _reviews = MutableStateFlow<List<CustomerReview>>(FirebaseRepository.defaultReviews)
    val reviews: StateFlow<List<CustomerReview>> = _reviews.asStateFlow()

    private val _staffMembers = MutableStateFlow<List<StaffMember>>(FirebaseRepository.defaultStaff)
    val staffMembers: StateFlow<List<StaffMember>> = _staffMembers.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerItem>>(FirebaseRepository.defaultBanners)
    val banners: StateFlow<List<BannerItem>> = _banners.asStateFlow()

    private val _restaurantSettings = MutableStateFlow(RestaurantSettings())
    val restaurantSettings: StateFlow<RestaurantSettings> = _restaurantSettings.asStateFlow()

    init {
        viewModelScope.launch {
            FirebaseRepository.getMenuItemsFlow().collect { items ->
                _menuItems.value = items
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getCouponsFlow().collect { list ->
                _coupons.value = list
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getLiveOrdersFlow().collect { orders ->
                _liveOrders.value = orders
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getDeliveryZonesFlow().collect { zones ->
                _deliveryZones.value = zones
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getInventoryFlow().collect { items ->
                _inventoryItems.value = items
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getReviewsFlow().collect { revs ->
                _reviews.value = revs
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getStaffFlow().collect { staff ->
                _staffMembers.value = staff
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getBannersFlow().collect { bans ->
                _banners.value = bans
            }
        }
        viewModelScope.launch {
            FirebaseRepository.getRestaurantSettingsFlow().collect { settings ->
                _restaurantSettings.value = settings
            }
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleCutlery(enabled: Boolean) {
        _addCutlery.value = enabled
    }

    fun showDishDetails(item: FoodItem) {
        _selectedDishForDetails.value = item
    }

    fun dismissDishDetails() {
        _selectedDishForDetails.value = null
    }

    fun addToCart(item: FoodItem) {
        if (!item.isAvailable) return
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.foodItem.id == item.id && it.selectedAddOns.isEmpty() }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
        } else {
            current.add(CartItem(foodItem = item, quantity = 1, cartItemId = java.util.UUID.randomUUID().toString()))
        }
        _cartItems.value = current
        _lastAddedItemToast.value = "1x ${item.name} added to cart! 🛒"
    }

    fun addCustomizedToCart(
        item: FoodItem,
        quantity: Int,
        selectedAddOns: List<CartAddOn>,
        specialInstructions: String = ""
    ) {
        if (!item.isAvailable) return
        val current = _cartItems.value.toMutableList()
        val addOnsKey = selectedAddOns.map { it.name }.sorted().joinToString(",")
        val index = current.indexOfFirst {
            it.foodItem.id == item.id &&
            it.selectedAddOns.map { a -> a.name }.sorted().joinToString(",") == addOnsKey &&
            it.specialInstructions == specialInstructions
        }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + quantity)
        } else {
            current.add(
                CartItem(
                    foodItem = item,
                    quantity = quantity,
                    selectedAddOns = selectedAddOns,
                    specialInstructions = specialInstructions,
                    cartItemId = java.util.UUID.randomUUID().toString()
                )
            )
        }
        _cartItems.value = current
        _lastAddedItemToast.value = "${quantity}x ${item.name} added to cart! 🛒"
    }

    fun incrementQuantity(itemId: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.cartItemId == itemId || it.foodItem.id == itemId }
        if (index >= 0) {
            current[index] = current[index].copy(quantity = current[index].quantity + 1)
            _cartItems.value = current
        }
    }

    fun decrementQuantity(itemId: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.cartItemId == itemId || it.foodItem.id == itemId }
        if (index >= 0) {
            val currentQty = current[index].quantity
            if (currentQty > 1) {
                current[index] = current[index].copy(quantity = currentQty - 1)
            } else {
                current.removeAt(index)
            }
            _cartItems.value = current
        }
    }

    fun removeFromCart(itemId: String) {
        _cartItems.value = _cartItems.value.filter { it.cartItemId != itemId && it.foodItem.id != itemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    fun applyCoupon(code: String): Pair<Boolean, String> {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) return Pair(false, "Please enter a coupon code.")

        val found = _coupons.value.find { it.code.equals(trimmed, ignoreCase = true) }
        val discountPercent = when {
            trimmed == "CAFE20" -> 20
            trimmed == "SOMA15" -> 15
            trimmed == "PASTA10" -> 10
            trimmed == "WELCOME" || trimmed == "WELCOME10" -> 10
            found != null && found.discountPercent > 0 -> found.discountPercent
            else -> 0
        }

        if (discountPercent <= 0) {
            return Pair(false, "Invalid coupon code. Try 'SOMA15' or 'CAFE20'!")
        }

        val minOrder = found?.minOrderValue ?: 149.0
        if (getSubtotal() < minOrder) {
            return Pair(false, "Min order of ₹${minOrder.toInt()} required for '$trimmed'.")
        }

        if (found != null && !found.isActive) {
            return Pair(false, "Coupon '$trimmed' is currently inactive.")
        }

        _appliedCoupon.value = trimmed
        addNotification("Coupon Applied! 🎁", "Code '$trimmed' ($discountPercent% OFF) applied to your cart!", "🎁")
        return Pair(true, "Code '$trimmed' applied! You get $discountPercent% OFF 🎉")
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun getDiscount(): Double {
        val sub = getSubtotal()
        if (sub <= 0.0) return 0.0
        val currentCode = _appliedCoupon.value?.trim()?.uppercase()
        if (currentCode.isNullOrEmpty()) return 0.0

        val codePercent = when (currentCode) {
            "SOMA15" -> 0.15
            "CAFE20" -> 0.20
            "PASTA10" -> 0.10
            "WELCOME", "WELCOME10" -> 0.10
            else -> null
        }

        if (codePercent != null) {
            return kotlin.math.round(sub * codePercent * 100.0) / 100.0
        }

        val found = _coupons.value.find { it.code.equals(currentCode, ignoreCase = true) }
        val percent = if (found != null && found.discountPercent > 0) {
            found.discountPercent / 100.0
        } else {
            0.15
        }

        return kotlin.math.round(sub * percent * 100.0) / 100.0
    }

    fun saveCoupon(coupon: Coupon, onComplete: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        FirebaseRepository.saveCoupon(coupon) { success, error ->
            _isLoading.value = false
            onComplete(success, error)
        }
    }

    fun deleteCoupon(couponId: String, onComplete: (Boolean, String?) -> Unit) {
        _isLoading.value = true
        FirebaseRepository.deleteCoupon(couponId) { success, error ->
            _isLoading.value = false
            onComplete(success, error)
        }
    }

    fun toggleCouponActive(couponId: String, isActive: Boolean, onComplete: (Boolean, String?) -> Unit) {
        FirebaseRepository.toggleCouponActive(couponId, isActive, onComplete)
    }

    fun getSubtotal(): Double {
        return _cartItems.value.sumOf { it.unitPrice * it.quantity }
    }

    fun getTax(): Double {
        val taxableAmount = (getSubtotal() - getDiscount()).coerceAtLeast(0.0)
        return kotlin.math.round(taxableAmount * 0.05 * 100.0) / 100.0 // 5% GST
    }

    fun getTotal(): Double {
        val discountedSubtotal = (getSubtotal() - getDiscount()).coerceAtLeast(0.0)
        return kotlin.math.round((discountedSubtotal + getTax()) * 100.0) / 100.0
    }

    fun openNotificationCenter() {
        _showNotificationCenter.value = true
    }

    fun closeNotificationCenter() {
        _showNotificationCenter.value = false
        markAllNotificationsAsRead()
    }

    fun addNotification(title: String, message: String, iconEmoji: String = "🔔") {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        val formattedTime = sdf.format(java.util.Date())
        val newNotif = ActivityNotification(
            title = title,
            message = message,
            formattedTime = formattedTime,
            iconEmoji = iconEmoji,
            isRead = false
        )
        val current = _notifications.value.toMutableList()
        current.add(0, newNotif)
        _notifications.value = current
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun getUnreadNotificationCount(): Int {
        return _notifications.value.count { !it.isRead }
    }

    fun placeOrder(
        customerName: String = "Guest",
        customerPhone: String = "7321962452",
        deliveryAddress: String = "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab",
        paymentMethod: String = "Pay at Counter / On Delivery",
        paymentStatus: String = "Pay on Delivery",
        orderType: String = "Delivery",
        deliveryZone: String = "Zone A (0 - 5 km)",
        onSuccess: (Order) -> Unit
    ) {
        if (_cartItems.value.isEmpty()) return
        _isLoading.value = true
        FirebaseRepository.placeOrder(
            cartItems = _cartItems.value,
            subtotal = getSubtotal(),
            discount = getDiscount(),
            couponCode = _appliedCoupon.value ?: "",
            tax = getTax(),
            total = getTotal(),
            addCutlery = _addCutlery.value,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            orderType = orderType,
            deliveryZone = deliveryZone
        ) { success, order ->
            _isLoading.value = false
            if (success && order != null) {
                _currentOrder.value = order
                val history = _userOrders.value.toMutableList()
                history.add(0, order)
                _userOrders.value = history
                _loyaltyStamps.value = (_loyaltyStamps.value + 1) % 6
                _cafeCoins.value = _cafeCoins.value + (order.total / 10).toInt()
                _cartItems.value = emptyList()
                _appliedCoupon.value = null
                addNotification(
                    title = "Order Confirmed! 📦",
                    message = "Your order #${order.id} for ₹${order.total.toInt()} has been confirmed & sent to kitchen.",
                    iconEmoji = "📦"
                )
                onSuccess(order)
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        FirebaseRepository.updateOrderStatus(orderId, newStatus) { success, error ->
            if (success) {
                if (_currentOrder.value?.id == orderId) {
                    _currentOrder.value = _currentOrder.value?.copy(status = newStatus)
                }
                addNotification(
                    title = "Order Status Update 🛵",
                    message = "Order #${orderId} is now '$newStatus'!",
                    iconEmoji = if (newStatus.contains("Delivered", ignoreCase = true)) "🎉" else "🛵"
                )
            }
            onComplete(success, error)
        }
    }

    fun toggleItemAvailability(itemId: String, isAvailable: Boolean) {
        val current = _menuItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            current[index] = current[index].copy(isAvailable = isAvailable)
            _menuItems.value = current
        }
        FirebaseRepository.toggleItemAvailability(itemId, isAvailable)
    }

    fun listenToOrder(orderId: String): Flow<Order?> = FirebaseRepository.listenToOrder(orderId)

    fun selectAvatarEmoji(emoji: String) {
        _selectedAvatarEmoji.value = emoji
    }

    fun toggleFavorite(itemId: String) {
        val current = _favoriteItemIds.value.toMutableSet()
        if (current.contains(itemId)) {
            current.remove(itemId)
        } else {
            current.add(itemId)
        }
        _favoriteItemIds.value = current
    }

    fun isFavorite(itemId: String): Boolean {
        return _favoriteItemIds.value.contains(itemId)
    }

    fun selectAddress(fullAddress: String) {
        _selectedAddress.value = fullAddress
    }

    fun addAddress(label: String, address: String) {
        if (address.isBlank()) return
        val current = _savedAddresses.value.toMutableList()
        val newAddr = AddressItem("addr_${System.currentTimeMillis()}", label.ifBlank { "Saved Address" }, address, false)
        current.add(newAddr)
        _savedAddresses.value = current
        _selectedAddress.value = address
    }

    fun clearAddToCartToast() {
        _lastAddedItemToast.value = null
    }

    fun repeatOrder(order: Order) {
        val currentCart = _cartItems.value.toMutableList()
        order.items.forEach { pastItem ->
            val index = currentCart.indexOfFirst {
                it.foodItem.id == pastItem.foodItem.id &&
                it.selectedAddOns.map { a -> a.name }.sorted() == pastItem.selectedAddOns.map { a -> a.name }.sorted()
            }
            if (index >= 0) {
                currentCart[index] = currentCart[index].copy(quantity = currentCart[index].quantity + pastItem.quantity)
            } else {
                currentCart.add(pastItem.copy(cartItemId = java.util.UUID.randomUUID().toString()))
            }
        }
        _cartItems.value = currentCart
        _lastAddedItemToast.value = "Order items re-added to cart! 🛒"
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun signIn(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please enter both email and password"
            return
        }
        _isLoading.value = true
        FirebaseRepository.signIn(email, pass) { success, error ->
            _isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = error ?: "Login failed"
            }
        }
    }

    fun signUp(email: String, pass: String, name: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please enter all required fields"
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters"
            return
        }
        _isLoading.value = true
        FirebaseRepository.signUp(email, pass, name) { success, error ->
            _isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                _errorMessage.value = error ?: "Sign up failed"
            }
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        _isLoading.value = true
        FirebaseRepository.continueAsGuest { success, error ->
            _isLoading.value = false
            if (success) {
                onSuccess()
            } else {
                onSuccess()
            }
        }
    }

    fun continueWithGoogle(onSuccess: () -> Unit) {
        _isLoading.value = true
        FirebaseRepository.continueAsGoogle { success, error ->
            _isLoading.value = false
            onSuccess()
        }
    }

    fun handleGoogleSignInAccount(
        context: Context,
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        _isLoading.value = true
        val displayName = account.displayName ?: account.givenName ?: "Google User"
        val email = account.email ?: ""
        val photoUrl = account.photoUrl?.toString()
        val accountId = account.id ?: "google_${System.currentTimeMillis()}"

        val googleData = FirebaseRepository.GoogleUserData(
            id = accountId,
            name = displayName,
            email = email,
            photoUrl = photoUrl
        )
        FirebaseRepository.setGoogleUserData(context, googleData)

        // Sync or persist user profile in Firestore
        try {
            val userProfile = UserProfile(
                uid = accountId,
                name = displayName,
                email = email,
                phone = "7321962452"
            )
            FirebaseRepository.firestore?.collection("users")?.document(accountId)?.set(userProfile)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Also if idToken is present, sign in with Firebase Auth credential
        val idToken = account.idToken
        if (!idToken.isNullOrBlank()) {
            FirebaseRepository.signInWithGoogleToken(idToken) { _, _ ->
                _isLoading.value = false
                onSuccess()
            }
        } else {
            _isLoading.value = false
            onSuccess()
        }
    }

    fun signInWithGoogleCredential(activity: android.app.Activity, onSuccess: () -> Unit) {
        continueWithGoogle(onSuccess)
    }

    fun signOut(onComplete: () -> Unit) {
        signOut(null, onComplete)
    }

    fun signOut(context: Context?, onComplete: () -> Unit) {
        FirebaseRepository.signOut(context)
        onComplete()
    }

    fun addNewFoodItem(
        name: String,
        description: String,
        price: Double,
        category: String,
        imageBase64: String,
        isPopular: Boolean,
        isTodaySpecial: Boolean = false,
        offerTag: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (name.isBlank()) {
            onResult(false, "Please enter item name")
            return
        }
        if (price <= 0) {
            onResult(false, "Please enter a valid price")
            return
        }
        if (imageBase64.isBlank()) {
            onResult(false, "Please select an image for the item")
            return
        }

        _isLoading.value = true
        val id = "item_${System.currentTimeMillis()}"
        val newItem = FoodItem(
            id = id,
            name = name.trim(),
            description = description.trim(),
            price = price,
            rating = 4.5,
            category = category.ifBlank { "Pasta" },
            imageUrl = imageBase64,
            isPopular = isPopular,
            isTodaySpecial = isTodaySpecial,
            offerTag = offerTag.trim()
        )

        val current = _menuItems.value.toMutableList()
        current.add(0, newItem)
        _menuItems.value = current

        FirebaseRepository.addNewFoodItem(newItem) { success, error ->
            _isLoading.value = false
            onResult(success, error)
        }
    }

    fun saveFoodItem(item: FoodItem, onResult: (Boolean, String?) -> Unit) {
        val current = _menuItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            updateFoodItem(item, onResult)
        } else {
            _isLoading.value = true
            current.add(0, item)
            _menuItems.value = current
            FirebaseRepository.addNewFoodItem(item) { success, error ->
                _isLoading.value = false
                onResult(success, error)
            }
        }
    }

    fun updateFoodItem(
        updatedItem: FoodItem,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (updatedItem.name.isBlank()) {
            onResult(false, "Please enter item name")
            return
        }
        if (updatedItem.price <= 0) {
            onResult(false, "Please enter a valid price")
            return
        }

        _isLoading.value = true
        val current = _menuItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == updatedItem.id }
        if (index >= 0) {
            current[index] = updatedItem
            _menuItems.value = current
        }

        FirebaseRepository.updateFoodItem(updatedItem) { success, error ->
            _isLoading.value = false
            onResult(success, error)
        }
    }

    fun rateItem(itemId: String, userRating: Double) {
        val current = _menuItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId }
        if (index >= 0) {
            val oldRating = current[index].rating
            val updatedRating = kotlin.math.round(((oldRating + userRating) / 2.0) * 10.0) / 10.0
            val updatedItem = current[index].copy(rating = updatedRating)
            current[index] = updatedItem
            _menuItems.value = current
            FirebaseRepository.rateFoodItem(itemId, updatedRating)
        }
    }

    fun deleteFoodItem(
        itemId: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        _isLoading.value = true
        _menuItems.value = _menuItems.value.filter { it.id != itemId }
        _cartItems.value = _cartItems.value.filter { it.foodItem.id != itemId }

        FirebaseRepository.deleteFoodItem(itemId) { success, error ->
            _isLoading.value = false
            onResult(success, error)
        }
    }

    // 15-SCREEN ADMIN ACTIONS
    fun replyToReview(reviewId: String, reply: String) {
        val current = _reviews.value.toMutableList()
        val index = current.indexOfFirst { it.id == reviewId }
        if (index >= 0) {
            current[index] = current[index].copy(adminReply = reply)
            _reviews.value = current
        }
        FirebaseRepository.replyToReview(reviewId, reply) { _, _ -> }
    }

    fun toggleDeliveryZone(zoneId: String, isActive: Boolean) {
        val current = _deliveryZones.value.toMutableList()
        val index = current.indexOfFirst { it.id == zoneId }
        if (index >= 0) {
            current[index] = current[index].copy(isActive = isActive)
            _deliveryZones.value = current
        }
        FirebaseRepository.toggleDeliveryZone(zoneId, isActive) { _, _ -> }
    }

    fun toggleStaffActive(staffId: String, isActive: Boolean) {
        val current = _staffMembers.value.toMutableList()
        val index = current.indexOfFirst { it.id == staffId }
        if (index >= 0) {
            current[index] = current[index].copy(isActive = isActive)
            _staffMembers.value = current
        }
        FirebaseRepository.toggleStaffActive(staffId, isActive) { _, _ -> }
    }

    fun toggleBannerActive(bannerId: String, isActive: Boolean) {
        val current = _banners.value.toMutableList()
        val index = current.indexOfFirst { it.id == bannerId }
        if (index >= 0) {
            current[index] = current[index].copy(isActive = isActive)
            _banners.value = current
        }
        FirebaseRepository.toggleBannerActive(bannerId, isActive) { _, _ -> }
    }

    fun updateRestaurantSettings(settings: RestaurantSettings) {
        _restaurantSettings.value = settings
        FirebaseRepository.updateSettings(settings) { _, _ -> }
    }

    fun submitCustomerReview(review: CustomerReview) {
        val current = _reviews.value.toMutableList()
        val id = review.id.ifBlank { "rev_${System.currentTimeMillis()}" }
        val finalReview = review.copy(id = id)
        current.add(0, finalReview)
        _reviews.value = current
        FirebaseRepository.submitCustomerReview(finalReview) { _, _ -> }
    }

    fun saveInventoryItem(item: InventoryItem) {
        val id = item.id.ifBlank { "inv_${System.currentTimeMillis()}" }
        val status = if (item.quantity <= 0.0) "Out of Stock" else if (item.quantity <= item.lowStockThreshold) "Low Stock" else "In Stock"
        val finalItem = item.copy(id = id, status = status)
        val current = _inventoryItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = finalItem
        } else {
            current.add(0, finalItem)
        }
        _inventoryItems.value = current
        FirebaseRepository.saveInventoryItem(finalItem) { _, _ -> }
    }

    fun deleteInventoryItem(itemId: String) {
        _inventoryItems.value = _inventoryItems.value.filter { it.id != itemId }
        FirebaseRepository.deleteInventoryItem(itemId) { _, _ -> }
    }

    fun saveStaffMember(staff: StaffMember) {
        val id = staff.id.ifBlank { "staff_${System.currentTimeMillis()}" }
        val finalStaff = staff.copy(id = id)
        val current = _staffMembers.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = finalStaff
        } else {
            current.add(0, finalStaff)
        }
        _staffMembers.value = current
        FirebaseRepository.saveStaffMember(finalStaff) { _, _ -> }
    }

    fun deleteStaffMember(staffId: String) {
        _staffMembers.value = _staffMembers.value.filter { it.id != staffId }
        FirebaseRepository.deleteStaffMember(staffId) { _, _ -> }
    }

    fun saveBanner(banner: BannerItem) {
        val id = banner.id.ifBlank { "ban_${System.currentTimeMillis()}" }
        val finalBanner = banner.copy(id = id)
        val current = _banners.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = finalBanner
        } else {
            current.add(0, finalBanner)
        }
        _banners.value = current
        FirebaseRepository.saveBanner(finalBanner) { _, _ -> }
    }

    fun deleteBanner(bannerId: String) {
        _banners.value = _banners.value.filter { it.id != bannerId }
        FirebaseRepository.deleteBanner(bannerId) { _, _ -> }
    }

    fun saveDeliveryZone(zone: DeliveryZone) {
        val id = zone.id.ifBlank { "zone_${System.currentTimeMillis()}" }
        val finalZone = zone.copy(id = id)
        val current = _deliveryZones.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = finalZone
        } else {
            current.add(0, finalZone)
        }
        _deliveryZones.value = current
        FirebaseRepository.saveDeliveryZone(finalZone) { _, _ -> }
    }

    fun deleteDeliveryZone(zoneId: String) {
        _deliveryZones.value = _deliveryZones.value.filter { it.id != zoneId }
        FirebaseRepository.deleteDeliveryZone(zoneId) { _, _ -> }
    }
}

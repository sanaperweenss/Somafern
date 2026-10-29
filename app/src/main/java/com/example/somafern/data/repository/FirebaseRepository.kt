package com.example.somafern.data.repository

import android.os.Handler
import android.os.Looper
import com.example.somafern.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class GoogleUserData(
        val id: String,
        val name: String,
        val email: String,
        val photoUrl: String? = null
    )

    private var googleUserData: GoogleUserData? = null

    fun setGoogleUserData(context: android.content.Context?, data: GoogleUserData?) {
        googleUserData = data
        if (context != null) {
            try {
                val prefs = context.getSharedPreferences("somafern_auth_prefs", android.content.Context.MODE_PRIVATE)
                if (data != null) {
                    prefs.edit()
                        .putString("google_user_id", data.id)
                        .putString("google_user_name", data.name)
                        .putString("google_user_email", data.email)
                        .putString("google_user_photo", data.photoUrl ?: "")
                        .putBoolean("is_google_signed_in", true)
                        .apply()
                } else {
                    prefs.edit().clear().apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun restoreGoogleUserData(context: android.content.Context) {
        if (googleUserData == null) {
            try {
                val prefs = context.getSharedPreferences("somafern_auth_prefs", android.content.Context.MODE_PRIVATE)
                if (prefs.getBoolean("is_google_signed_in", false)) {
                    val id = prefs.getString("google_user_id", "") ?: ""
                    val name = prefs.getString("google_user_name", "") ?: ""
                    val email = prefs.getString("google_user_email", "") ?: ""
                    val photo = prefs.getString("google_user_photo", null)?.takeIf { it.isNotBlank() }
                    if (id.isNotBlank() || email.isNotBlank()) {
                        googleUserData = GoogleUserData(id = id, name = name, email = email, photoUrl = photo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getGoogleUserData(): GoogleUserData? = googleUserData

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid ?: googleUserData?.id
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email?.takeIf { it.isNotBlank() } ?: googleUserData?.email
    }

    fun getCurrentUserName(): String? {
        val fbName = auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
        if (fbName != null) return fbName
        val gName = googleUserData?.name?.takeIf { it.isNotBlank() }
        if (gName != null) return gName
        val email = getCurrentUserEmail()
        return if (!email.isNullOrBlank()) {
            val base = email.substringBefore("@")
            if (base.equals("guest", ignoreCase = true)) "SOMAFERN Guest"
            else base.replaceFirstChar { it.uppercase() }
        } else null
    }

    fun getCurrentUserPhoto(): String? {
        return auth.currentUser?.photoUrl?.toString() ?: googleUserData?.photoUrl
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null || googleUserData != null
    }

    fun isGoogleUser(): Boolean {
        return googleUserData != null || auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true
    }

    val defaultFoodItems = emptyList<FoodItem>()

    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.localizedMessage ?: "Login failed")
            }
    }

    fun signUp(email: String, pass: String, name: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid != null) {
                    val profile = UserProfile(
                        uid = uid,
                        name = name.ifBlank { "SOMAFERN" },
                        email = email
                    )
                    try {
                        firestore?.collection("users")?.document(uid)?.set(profile)
                    } catch (_: Exception) {}
                }
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.localizedMessage ?: "Account creation failed")
            }
    }

    fun continueAsGuest(onResult: (Boolean, String?) -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.localizedMessage ?: "Guest login failed")
            }
    }

    fun continueAsGoogle(onResult: (Boolean, String?) -> Unit) {
        auth.signInAnonymously()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.localizedMessage ?: "Google login failed")
            }
    }

    fun signInWithGoogleToken(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.localizedMessage ?: "Google Firebase auth failed")
            }
    }

    fun signOut(context: android.content.Context? = null) {
        try {
            auth.signOut()
        } catch (_: Exception) {}
        setGoogleUserData(context, null)
    }

    fun getMenuItemsFlow(): Flow<List<FoodItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose {}
            return@callbackFlow
        }

        try {
            val registration = db.collection("menu")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(FoodItem::class.java)
                        }
                        trySend(list)
                    } else {
                        trySend(emptyList())
                    }
                }
            awaitClose { registration.remove() }
        } catch (e: Exception) {
            trySend(emptyList())
            awaitClose {}
        }
    }

    fun placeOrder(
        cartItems: List<CartItem>,
        subtotal: Double,
        discount: Double = 0.0,
        couponCode: String = "",
        tax: Double,
        total: Double,
        addCutlery: Boolean,
        customerName: String = "Guest",
        customerPhone: String = "7321962452",
        deliveryAddress: String = "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab",
        paymentMethod: String = "Pay at Counter / On Delivery",
        paymentStatus: String = "Pay on Delivery",
        orderType: String = "Delivery",
        deliveryZone: String = "Zone A (0 - 5 km)",
        onComplete: (Boolean, Order?) -> Unit
    ) {
        val uid = getCurrentUserId() ?: "guest_${System.currentTimeMillis()}"
        val orderNum = (100000..999999).random()
        val orderId = "order_$orderNum"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val formattedDate = sdf.format(Date())

        val order = Order(
            id = orderId,
            userId = uid,
            customerName = customerName,
            customerPhone = customerPhone,
            items = cartItems,
            subtotal = subtotal,
            discount = discount,
            couponCode = couponCode,
            tax = tax,
            deliveryFee = 0.0,
            total = total,
            addCutlery = addCutlery,
            status = "Order Placed",
            timestamp = System.currentTimeMillis(),
            formattedDate = formattedDate,
            deliveryAddress = deliveryAddress,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            orderType = orderType,
            deliveryZone = deliveryZone
        )

        try {
            firestore?.collection("orders")?.document(orderId)?.set(order)
            if (auth.currentUser != null) {
                firestore?.collection("users")?.document(uid)?.collection("orders")?.document(orderId)?.set(order)
            }
            onComplete(true, order)
        } catch (e: Exception) {
            onComplete(true, order)
        }
    }

    fun getLiveOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Order::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun listenToOrder(orderId: String): Flow<Order?> = callbackFlow {
        val db = firestore
        if (db == null || orderId.isBlank()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("orders").document(orderId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    return@addSnapshotListener
                }
                val order = doc.toObject(Order::class.java)?.copy(id = doc.id)
                trySend(order)
            }
        awaitClose { listener.remove() }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onComplete: (Boolean, String?) -> Unit) {
        val db = firestore
        if (db == null) {
            onComplete(false, "Database connection not available")
            return
        }
        db.collection("orders").document(orderId)
            .update("status", newStatus)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { onComplete(false, it.localizedMessage ?: "Failed to update status") }
    }

    fun addNewFoodItem(item: FoodItem, onResult: (Boolean, String?) -> Unit) {
        var completed = false
        val handler = Handler(Looper.getMainLooper())

        val timeoutRunnable = Runnable {
            if (!completed) {
                completed = true
                onResult(true, null)
            }
        }
        handler.postDelayed(timeoutRunnable, 3500)

        try {
            val db = firestore
            if (db == null) {
                if (!completed) {
                    completed = true
                    handler.removeCallbacks(timeoutRunnable)
                    onResult(true, null)
                }
                return
            }

            db.collection("menu").document(item.id).set(item)
                .addOnSuccessListener {
                    if (!completed) {
                        completed = true
                        handler.removeCallbacks(timeoutRunnable)
                        onResult(true, null)
                    }
                }
                .addOnFailureListener { e ->
                    if (!completed) {
                        completed = true
                        handler.removeCallbacks(timeoutRunnable)
                        onResult(false, e.localizedMessage ?: "Failed to save to Cloud Firestore")
                    }
                }
        } catch (e: Exception) {
            if (!completed) {
                completed = true
                handler.removeCallbacks(timeoutRunnable)
                onResult(true, null)
            }
        }
    }

    fun updateFoodItem(item: FoodItem, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = firestore
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("menu").document(item.id).set(item)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to update item") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error updating item")
        }
    }

    fun toggleItemAvailability(itemId: String, isAvailable: Boolean, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        try {
            val db = firestore
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("menu").document(itemId).update("isAvailable", isAvailable)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to update availability") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error updating availability")
        }
    }

    fun rateFoodItem(itemId: String, newRating: Double, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        try {
            val db = firestore
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("menu").document(itemId).update("rating", newRating)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to update rating") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error updating rating")
        }
    }

    fun deleteFoodItem(itemId: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = firestore
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("menu").document(itemId).delete()
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to delete item") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error deleting item")
        }
    }

    val defaultCoupons = listOf(
        Coupon(
            id = "coupon_cafe20",
            code = "CAFE20",
            title = "WEEKEND SPECIAL ☕",
            description = "Flat 20% OFF on gourmet Shakes & Italian Pastas",
            discountPercent = 20,
            minOrderValue = 199.0,
            isActive = true,
            usageCount = 54
        ),
        Coupon(
            id = "coupon_pasta10",
            code = "PASTA10",
            title = "PASTA CRAVINGS 🍝",
            description = "10% OFF on All Artisan Pastas & Garlic Bread",
            discountPercent = 10,
            minOrderValue = 299.0,
            isActive = true,
            usageCount = 46
        ),
        Coupon(
            id = "coupon_soma15",
            code = "SOMA15",
            title = "HAPPY HOURS VOUCHER ⚡",
            description = "Valid on Pastas, Momos, Shakes & Sandwiches",
            discountPercent = 15,
            minOrderValue = 199.0,
            isActive = true,
            usageCount = 24
        )
    )

    fun getCouponsFlow(): Flow<List<Coupon>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultCoupons)
            awaitClose {}
            return@callbackFlow
        }

        try {
            val registration = db.collection("coupons")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(defaultCoupons)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Coupon::class.java)?.copy(id = doc.id)
                        }
                        trySend(list)
                    } else {
                        defaultCoupons.forEach { c ->
                            try {
                                db.collection("coupons").document(c.id).set(c)
                            } catch (_: Exception) {}
                        }
                        trySend(defaultCoupons)
                    }
                }
            awaitClose { registration.remove() }
        } catch (e: Exception) {
            trySend(defaultCoupons)
            awaitClose {}
        }
    }

    fun saveCoupon(coupon: Coupon, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = firestore
            val id = coupon.id.ifBlank { "coupon_${System.currentTimeMillis()}" }
            val finalCoupon = coupon.copy(id = id, code = coupon.code.trim().uppercase())
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("coupons").document(id).set(finalCoupon)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to save coupon") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error saving coupon")
        }
    }

    fun deleteCoupon(couponId: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = firestore
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("coupons").document(couponId).delete()
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to delete coupon") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error deleting coupon")
        }
    }

    fun toggleCouponActive(couponId: String, isActive: Boolean, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = firestore
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("coupons").document(couponId).update("isActive", isActive)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { e -> onResult(false, e.localizedMessage ?: "Failed to toggle coupon") }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage ?: "Error updating coupon")
        }
    }

    // -------------------------------------------------------------
    // 15-SCREEN ADMIN MODULE DATA FLOWS (INVENTORY, REVIEWS, ZONES, STAFF, ETC.)
    // -------------------------------------------------------------

    val defaultDeliveryZones = listOf(
        DeliveryZone("zone_a", "Zone A (0 - 5 km)", 20.0, 199.0, "30-40 mins", true),
        DeliveryZone("zone_b", "Zone B (5 - 10 km)", 30.0, 299.0, "40-50 mins", true),
        DeliveryZone("zone_c", "Zone C (10 - 15 km)", 50.0, 399.0, "50-60 mins", false)
    )

    val defaultInventoryItems = listOf(
        InventoryItem("inv_1", "Cheese", 2.0, "kg", 2.0, "Low Stock"),
        InventoryItem("inv_2", "Tomatoes", 4.0, "kg", 5.0, "Low Stock"),
        InventoryItem("inv_3", "Momos Sheets", 50.0, "pcs", 30.0, "In Stock"),
        InventoryItem("inv_4", "Pasta", 3.0, "kg", 1.5, "In Stock"),
        InventoryItem("inv_5", "Chicken", 1.5, "kg", 1.0, "In Stock"),
        InventoryItem("inv_6", "Garlic", 2.0, "kg", 0.5, "In Stock")
    )

    val defaultReviews = listOf(
        CustomerReview("rev_1", "order_834866", "Priya Mehta", "+91 84567 32109", 5, "Amazing food! The white sauce pasta is a must try! ❤️", "2 days ago"),
        CustomerReview("rev_2", "order_834865", "Rohan Verma", "+91 91234 56789", 5, "Best momos in town. Fresh and tasty!", "4 days ago"),
        CustomerReview("rev_3", "order_834864", "Ananya Singh", "+91 95887 77665", 4, "Great packaging and on-time delivery.", "1 week ago")
    )

    val defaultStaff = listOf(
        StaffMember("staff_1", "Rahul Sharma", "Manager", "+91 98765 43210", true),
        StaffMember("staff_2", "Neha Kapoor", "Chef", "+91 98765 43211", true),
        StaffMember("staff_3", "Vikram Singh", "Delivery Staff", "+91 98765 43212", true),
        StaffMember("staff_4", "Pooja Mehta", "Cashier", "+91 98765 43213", false)
    )

    val defaultBanners = listOf(
        BannerItem("ban_1", "Delicious Pastas", "Make your day better!", "", true),
        BannerItem("ban_2", "Weekend Combo", "Flat 20% OFF", "", true),
        BannerItem("ban_3", "Festive Offer", "Buy 1 Get 1", "", false)
    )

    fun getDeliveryZonesFlow(): Flow<List<DeliveryZone>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultDeliveryZones)
            awaitClose {}
            return@callbackFlow
        }
        val sub = db.collection("delivery_zones").addSnapshotListener { snap, _ ->
            if (snap != null && !snap.isEmpty) {
                val list = snap.documents.mapNotNull { it.toObject(DeliveryZone::class.java)?.copy(id = it.id) }
                trySend(list)
            } else {
                trySend(defaultDeliveryZones)
            }
        }
        awaitClose { sub.remove() }
    }

    fun getInventoryFlow(): Flow<List<InventoryItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultInventoryItems)
            awaitClose {}
            return@callbackFlow
        }
        val sub = db.collection("inventory").addSnapshotListener { snap, _ ->
            if (snap != null && !snap.isEmpty) {
                val list = snap.documents.mapNotNull { it.toObject(InventoryItem::class.java)?.copy(id = it.id) }
                trySend(list)
            } else {
                trySend(defaultInventoryItems)
            }
        }
        awaitClose { sub.remove() }
    }

    fun getReviewsFlow(): Flow<List<CustomerReview>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultReviews)
            awaitClose {}
            return@callbackFlow
        }
        val sub = db.collection("reviews").addSnapshotListener { snap, _ ->
            if (snap != null && !snap.isEmpty) {
                val list = snap.documents.mapNotNull { it.toObject(CustomerReview::class.java)?.copy(id = it.id) }
                trySend(list)
            } else {
                trySend(defaultReviews)
            }
        }
        awaitClose { sub.remove() }
    }

    fun getStaffFlow(): Flow<List<StaffMember>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultStaff)
            awaitClose {}
            return@callbackFlow
        }
        val sub = db.collection("staff").addSnapshotListener { snap, _ ->
            if (snap != null && !snap.isEmpty) {
                val list = snap.documents.mapNotNull { it.toObject(StaffMember::class.java)?.copy(id = it.id) }
                trySend(list)
            } else {
                trySend(defaultStaff)
            }
        }
        awaitClose { sub.remove() }
    }

    fun getBannersFlow(): Flow<List<BannerItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(defaultBanners)
            awaitClose {}
            return@callbackFlow
        }
        val sub = db.collection("banners").addSnapshotListener { snap, _ ->
            if (snap != null && !snap.isEmpty) {
                val list = snap.documents.mapNotNull { it.toObject(BannerItem::class.java)?.copy(id = it.id) }
                trySend(list)
            } else {
                trySend(defaultBanners)
            }
        }
        awaitClose { sub.remove() }
    }

    fun getRestaurantSettingsFlow(): Flow<RestaurantSettings> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(RestaurantSettings())
            awaitClose {}
            return@callbackFlow
        }
        val sub = db.collection("settings").document("cafe_config").addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                val config = doc.toObject(RestaurantSettings::class.java) ?: RestaurantSettings()
                trySend(config)
            } else {
                trySend(RestaurantSettings())
            }
        }
        awaitClose { sub.remove() }
    }

    fun replyToReview(reviewId: String, reply: String, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("reviews")?.document(reviewId)?.update("adminReply", reply)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun toggleDeliveryZone(zoneId: String, isActive: Boolean, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("delivery_zones")?.document(zoneId)?.update("isActive", isActive)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun toggleStaffActive(staffId: String, isActive: Boolean, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("staff")?.document(staffId)?.update("isActive", isActive)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun toggleBannerActive(bannerId: String, isActive: Boolean, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("banners")?.document(bannerId)?.update("isActive", isActive)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun updateSettings(settings: RestaurantSettings, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("settings")?.document("cafe_config")?.set(settings)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun submitCustomerReview(review: CustomerReview, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = firestore
            val id = review.id.ifBlank { "rev_${System.currentTimeMillis()}" }
            val finalReview = review.copy(id = id)
            if (db == null) {
                onResult(true, null)
                return
            }
            db.collection("reviews").document(id).set(finalReview)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { onResult(false, it.localizedMessage) }
        } catch (e: Exception) {
            onResult(false, e.localizedMessage)
        }
    }

    fun saveInventoryItem(item: InventoryItem, onResult: (Boolean, String?) -> Unit) {
        val id = item.id.ifBlank { "inv_${System.currentTimeMillis()}" }
        val status = if (item.quantity <= 0.0) "Out of Stock" else if (item.quantity <= item.lowStockThreshold) "Low Stock" else "In Stock"
        val finalItem = item.copy(id = id, status = status)
        firestore?.collection("inventory")?.document(id)?.set(finalItem)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun deleteInventoryItem(itemId: String, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("inventory")?.document(itemId)?.delete()
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun saveStaffMember(staff: StaffMember, onResult: (Boolean, String?) -> Unit) {
        val id = staff.id.ifBlank { "staff_${System.currentTimeMillis()}" }
        val finalStaff = staff.copy(id = id)
        firestore?.collection("staff")?.document(id)?.set(finalStaff)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun deleteStaffMember(staffId: String, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("staff")?.document(staffId)?.delete()
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun saveBanner(banner: BannerItem, onResult: (Boolean, String?) -> Unit) {
        val id = banner.id.ifBlank { "ban_${System.currentTimeMillis()}" }
        val finalBanner = banner.copy(id = id)
        firestore?.collection("banners")?.document(id)?.set(finalBanner)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun deleteBanner(bannerId: String, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("banners")?.document(bannerId)?.delete()
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun saveDeliveryZone(zone: DeliveryZone, onResult: (Boolean, String?) -> Unit) {
        val id = zone.id.ifBlank { "zone_${System.currentTimeMillis()}" }
        val finalZone = zone.copy(id = id)
        firestore?.collection("delivery_zones")?.document(id)?.set(finalZone)
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }

    fun deleteDeliveryZone(zoneId: String, onResult: (Boolean, String?) -> Unit) {
        firestore?.collection("delivery_zones")?.document(zoneId)?.delete()
            ?.addOnSuccessListener { onResult(true, null) }
            ?.addOnFailureListener { onResult(false, it.localizedMessage) }
            ?: onResult(true, null)
    }
}

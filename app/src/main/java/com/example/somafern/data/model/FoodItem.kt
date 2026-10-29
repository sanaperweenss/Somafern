package com.example.somafern.data.model

import com.google.firebase.firestore.Exclude

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val rating: Double = 4.5,
    val category: String = "Pasta",
    val imageUrl: String = "",
    val isPopular: Boolean = false,
    val isTodaySpecial: Boolean = false,
    val offerTag: String = "",
    val isAvailable: Boolean = true,
    val isVeg: Boolean = true,
    val drawableRes: Int? = null
)

val FoodItem.isVegItem: Boolean
    @Exclude
    get() {
        if (!isVeg) return false
        val n = name.lowercase()
        val d = description.lowercase()
        val c = category.lowercase()

        val nonVegKeywords = listOf("chicken", "mutton", "egg", "fish", "prawn", "meat", "non-veg", "non veg", "keema")
        return !nonVegKeywords.any { n.contains(it) || d.contains(it) || c.contains(it) }
    }

data class CartAddOn(
    val name: String = "",
    val price: Double = 0.0
)

data class CartItem(
    val foodItem: FoodItem = FoodItem(),
    var quantity: Int = 1,
    val selectedAddOns: List<CartAddOn> = emptyList(),
    val specialInstructions: String = "",
    val cartItemId: String = java.util.UUID.randomUUID().toString()
) {
    val unitPrice: Double
        @Exclude
        get() = foodItem.price + selectedAddOns.sumOf { it.price }

    val totalPrice: Double
        @Exclude
        get() = unitPrice * quantity
}

data class Order(
    val id: String = "",
    val userId: String = "",
    val customerName: String = "Guest",
    val customerPhone: String = "7321962452",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val couponCode: String = "",
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val total: Double = 0.0,
    val addCutlery: Boolean = false,
    val status: String = "Order Placed", // "Order Placed", "Preparing", "On the Way", "Delivered", "Cancelled", "Rejected"
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDate: String = "",
    val deliveryAddress: String = "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab",
    val paymentMethod: String = "Pay at Counter / On Delivery",
    val paymentStatus: String = "Pay on Delivery",
    val orderType: String = "Delivery", // "Delivery", "Dine-In", "Takeaway"
    val deliveryZone: String = "Zone A (0 - 5 km)"
)

data class AddressItem(
    val id: String = "",
    val label: String = "Home 🏠",
    val fullAddress: String = "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab",
    val isDefault: Boolean = false
)

data class UserProfile(
    val uid: String = "",
    val name: String = "SOMAFERN",
    val email: String = "",
    val title: String = "Food Lover",
    val phone: String = "7321962452",
    val address: String = "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab",
    val savedAddresses: List<AddressItem> = listOf(
        AddressItem("addr_home", "Home 🏠", "hi 7M2W+V7P, Law Gate Rd, Phagwara, Punjab", true),
        AddressItem("addr_campus", "Hostel / Campus 🏫", "Block 34, LPU Campus, Phagwara, Punjab", false),
        AddressItem("addr_work", "Work 🏢", "IT Park, Law Gate Rd, Phagwara, Punjab", false)
    ),
    val loyaltyStamps: Int = 3,
    val cafeCoins: Int = 120
)

data class Coupon(
    val id: String = "",
    val code: String = "",
    val title: String = "HAPPY HOURS VOUCHER ⚡",
    val description: String = "Valid on Pastas, Momos, Shakes & Sandwiches",
    val discountPercent: Int = 15, // 15%
    val minOrderValue: Double = 199.0,
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val usageCount: Int = 0,
    val totalRevenueGenerated: Double = 0.0
)

data class ActivityNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = "Just now",
    val iconEmoji: String = "🔔",
    val isRead: Boolean = false
)

data class DeliveryZone(
    val id: String = "",
    val name: String = "Zone A (0 - 5 km)",
    val fee: Double = 20.0,
    val minOrder: Double = 199.0,
    val estimatedTime: String = "30-40 mins",
    val isActive: Boolean = true
)

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "kg",
    val lowStockThreshold: Double = 2.0,
    val status: String = "In Stock", // "In Stock", "Low Stock", "Out of Stock"
    val imageUrl: String = ""
)

data class CustomerReview(
    val id: String = "",
    val orderId: String = "",
    val customerName: String = "Customer",
    val customerPhone: String = "",
    val rating: Int = 5,
    val reviewText: String = "",
    val timeAgo: String = "Just now",
    val adminReply: String? = null
)

data class AdminNotification(
    val id: String = "",
    val type: String = "Orders", // "Orders", "System"
    val title: String = "",
    val subtitle: String = "",
    val timeAgo: String = "Just now",
    val iconEmoji: String = "📦",
    val isRead: Boolean = false
)

data class StaffMember(
    val id: String = "",
    val name: String = "",
    val role: String = "Manager", // "Manager", "Chef", "Delivery Staff", "Cashier"
    val phone: String = "",
    val isActive: Boolean = true
)

data class BannerItem(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true
)

data class RestaurantSettings(
    val restaurantName: String = "SOMAfern Café",
    val phone: String = "+91 73219 24882",
    val address: String = "Law Gate Rd, Phagwara, Punjab",
    val gstNumber: String = "06ABCDE1234F1Z5",
    val openingHours: String = "9:00 AM - 11:00 PM",
    val isOnlineOrderingOpen: Boolean = true,
    val logoUrl: String = ""
)

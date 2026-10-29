package com.example.somafern.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.somafern.data.model.Order
import java.net.URLEncoder

object OrderUtils {
    const val RESTAURANT_PHONE = "7321962452"
    const val WHATSAPP_COUNTRY_CODE = "91" // India (+91)

    fun sendOrderToWhatsApp(context: Context, order: Order) {
        val targetPhone = "$WHATSAPP_COUNTRY_CODE$RESTAURANT_PHONE"

        val sb = StringBuilder()
        sb.append("🍽️ *NEW ORDER - THE SOMAFERN*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *Order ID:* #${order.id}\n")
        sb.append("🕒 *Date:* ${order.formattedDate}\n")
        sb.append("📍 *Fulfillment:* ${order.orderType}\n\n")

        sb.append("👤 *CUSTOMER DETAILS:*\n")
        sb.append("• *Name:* ${order.customerName}\n")
        sb.append("• *Phone:* ${order.customerPhone}\n")
        sb.append("• *Address / Location:* ${order.deliveryAddress}\n\n")

        sb.append("📋 *ITEMS ORDERED:*\n")
        order.items.forEachIndexed { index, item ->
            val itemTotal = item.totalPrice.toInt()
            sb.append("${index + 1}. *${item.foodItem.name}* × ${item.quantity} = ₹$itemTotal\n")
            if (item.selectedAddOns.isNotEmpty()) {
                sb.append("   ↳ Extras: ${item.selectedAddOns.joinToString { "${it.name} (+₹${it.price.toInt()})" }}\n")
            }
            if (item.specialInstructions.isNotBlank()) {
                sb.append("   ↳ Note: ${item.specialInstructions}\n")
            }
        }

        if (order.addCutlery) {
            sb.append("🍴 *Cutlery:* Included\n")
        }

        sb.append("\n💳 *PAYMENT:*\n")
        sb.append("• *Mode:* ${order.paymentMethod}\n")

        sb.append("\n💰 *BILL SUMMARY:*\n")
        sb.append("• *Subtotal:* ₹${order.subtotal.toInt()}\n")
        if (order.discount > 0.0) {
            sb.append("• *Coupon (${order.couponCode.ifBlank { "OFFER" }}):* -₹${order.discount.toInt()} 🎉\n")
        }
        sb.append("• *GST (5%):* ₹${order.tax.toInt()}\n")
        sb.append("• *Grand Total:* ₹${order.total.toInt()}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━\n")
        sb.append("✨ _Please confirm and start preparing my order._")

        try {
            val encodedMessage = URLEncoder.encode(sb.toString(), "UTF-8")
            val url = "https://wa.me/$targetPhone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playOrderConfirmationTone(context: Context) {
        try {
            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 90)
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

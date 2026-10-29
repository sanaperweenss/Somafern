# Production-Level UI Upgrade for ProfileScreen

This plan outlines the complete visual and functional redesign of `ProfileScreen.kt` to elevate it to world-class production standards (matching top-tier cafe and food delivery apps like Zomato/Swiggy/Starbucks).

## User Review Required

> [!IMPORTANT]
> The upgraded Profile Screen will retain all existing state bindings (Avatar emoji picker, Cafe Coins, Loyalty Stamps, Recent Orders & 1-tap reorder, Saved Addresses manager, WhatsApp/Call support, and Sign Out), while wrapping them in a gorgeous, modern, production-grade layout.

## Proposed Changes

### [Profile Screen Redesign]

#### [MODIFY] [ProfileScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Somafern/app/src/main/java/com/example/somafern/ui/screens/ProfileScreen.kt)
- **Hero Identity Card**: Immersive dual-tone gradient banner with VIP Gold badge, avatar selector with animated ring, membership status, and editable user greeting.
- **Interactive Quick Metrics Grid**: 4 glossy glassmorphism cards for Orders, Cafe Coins, Loyalty Stamps, and Saved Addresses.
- **Interactive Loyalty Stamp Card**: Redesigned 5-stamp reward card with progress tracker ("2 stamps left for Free Hazelnut Frappe!").
- **Recent Orders & 1-Tap Reorder Section**: Enhanced order cards with status pills (Delivered / Active) and sleek Reorder buttons.
- **Production Menu Options**:
  - Saved Addresses Manager (with selection indicators)
  - Cafe Wallet & Coin Rewards
  - Direct Call & WhatsApp Support
  - App Settings & Help Center
  - Secure Sign Out button with confirmation/danger styling
- **Cute Avatar Picker Bottom Sheet**: Polished modal bottom sheet for selecting cafe personas (`👑`, `☕`, `🍝`, `🥟`, `🍕`, `🍹`).

## Verification Plan

### Automated Tests
- Run Gradle build: `./gradlew :app:compileDebugKotlin`
### Manual Verification
- Deploy app and verify the production UI overhaul on the Profile tab.

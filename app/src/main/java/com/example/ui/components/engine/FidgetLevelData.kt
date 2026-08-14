package com.example.ui.components.engine

import androidx.compose.ui.graphics.Color

enum class RopeStyle {
    THREAD, COTTON, WOOL, CORD, STRIPED, METALLIC, CABLE, BRAIDED, CHAIN, MONOFILAMENT
}

enum class RopeMaterial(
    val strokeWidthDp: Float,
    val primaryColor: Color,
    val accentColor: Color,
    val style: RopeStyle,
    val defaultStiffness: Float,
    val defaultDamping: Float
) {
    THREAD(1.2f, Color(0xFFD4AF37), Color(0xFFFFF8DC), RopeStyle.THREAD, 0.85f, 0.96f),
    COTTON(2.8f, Color(0xFFE2E8F0), Color(0xFFCBD5E1), RopeStyle.COTTON, 0.80f, 0.95f),
    WOOL(3.5f, Color(0xFFF472B6), Color(0xFFF43F5E), RopeStyle.WOOL, 0.70f, 0.92f),
    CORD(3.0f, Color(0xFF38BDF8), Color(0xFF0284C7), RopeStyle.CORD, 0.82f, 0.95f),
    SHOELACE(3.2f, Color(0xFFF97316), Color(0xFFEA580C), RopeStyle.STRIPED, 0.78f, 0.94f),
    WIRE(2.0f, Color(0xFFA855F7), Color(0xFF9333EA), RopeStyle.METALLIC, 0.95f, 0.98f),
    ELECTRICAL_CABLE(4.0f, Color(0xFF334155), Color(0xFF0F172A), RopeStyle.CABLE, 0.90f, 0.97f),
    BRAIDED_ROPE(4.5f, Color(0xFFCA8A04), Color(0xFF854D0E), RopeStyle.BRAIDED, 0.88f, 0.96f),
    CHAIN(5.0f, Color(0xFF94A3B8), Color(0xFF64748B), RopeStyle.CHAIN, 0.98f, 0.99f),
    FISHING_LINE(1.0f, Color(0x88E0F2FE), Color(0xAA38BDF8), RopeStyle.MONOFILAMENT, 0.75f, 0.97f)
}

enum class AttachedObject(
    val displayName: String,
    val mass: Float,
    val gravity: Float,
    val damping: Float,
    val bounciness: Float,
    val widthDp: Float,
    val heightDp: Float
) {
    BUTTON("Button", 0.5f, 300f, 0.95f, 0.3f, 24f, 24f),
    KEY("Key", 0.9f, 450f, 0.94f, 0.2f, 28f, 14f),
    FEATHER("Feather", 0.1f, 80f, 0.85f, 0.1f, 30f, 16f),
    SHIRT("T-Shirt", 0.7f, 250f, 0.92f, 0.2f, 32f, 28f),
    SOCK("Sock", 0.4f, 200f, 0.93f, 0.2f, 24f, 26f),
    LAMP("Lamp", 1.8f, 600f, 0.96f, 0.2f, 26f, 32f),
    HEADPHONES("Headphones", 1.2f, 400f, 0.95f, 0.3f, 30f, 30f),
    SHOE("Shoe", 1.4f, 500f, 0.95f, 0.5f, 34f, 20f),
    PADLOCK("Padlock", 2.2f, 700f, 0.97f, 0.1f, 26f, 32f),
    ANCHOR("Anchor", 3.5f, 900f, 0.98f, 0.05f, 32f, 34f),
    BUCKET("Bucket", 2.0f, 650f, 0.96f, 0.2f, 30f, 30f),
    SMALL_BELL("Small Bell", 0.8f, 350f, 0.95f, 0.4f, 22f, 24f),
    PLUG("Power Plug", 0.8f, 350f, 0.95f, 0.2f, 24f, 22f),
    LOCK("Lock", 2.0f, 650f, 0.96f, 0.1f, 26f, 30f),
    SWEATER("Small Sweater", 0.8f, 220f, 0.91f, 0.15f, 34f, 30f)
}

enum class EnvironmentType {
    STUDIO_DARK, SOFT_WARM, COLD_BLUE, MINIMAL_BLACK
}

enum class CompletionStyle {
    PULL_TO_DISCOVER, PUZZLE_TANGLE, DISTANCE_COIL
}

enum class LengthProfile(val minDurationSec: Int, val maxDurationSec: Int, val targetLengthPx: Float) {
    TINY(2, 5, 1200f),
    SHORT(5, 15, 2500f),
    MEDIUM(10, 20, 4500f),
    LONG(20, 30, 7000f)
}

data class FidgetLevel(
    val id: Int,
    val ropeMaterial: RopeMaterial,
    val objectType: AttachedObject,
    val lengthPx: Float,
    val ropeMass: Float = 1.0f,
    val stiffness: Float = ropeMaterial.defaultStiffness,
    val damping: Float = ropeMaterial.defaultDamping,
    val objectMass: Float = objectType.mass,
    val objectGravity: Float = objectType.gravity,
    val objectDamping: Float = objectType.damping,
    val environment: EnvironmentType = EnvironmentType.STUDIO_DARK,
    val completionStyle: CompletionStyle = CompletionStyle.PULL_TO_DISCOVER,
    val isMysteryObject: Boolean = true,
    val feelDescription: String = ""
)

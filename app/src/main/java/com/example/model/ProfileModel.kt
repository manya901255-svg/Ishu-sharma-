package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class CrownAvatarOption(
    val id: String,
    val name: String,
    val emoji: String,
    val subtitle: String,
    val gemColor: Color
)

val CROWN_AVATAR_LIST = listOf(
    CrownAvatarOption(
        id = "crown_imperial_gold",
        name = "Imperial Gold Crown",
        emoji = "👑",
        subtitle = "Maharaja's 24K pure gold crown with ruby crest",
        gemColor = RoyalGold
    ),
    CrownAvatarOption(
        id = "crown_kohinoor_diamond",
        name = "Kohinoor Diamond Diadem",
        emoji = "💎",
        subtitle = "Cut from legendary Kohinoor with platinum trim",
        gemColor = SapphireBlueLight
    ),
    CrownAvatarOption(
        id = "crown_emerald_peacock",
        name = "Peacock Emerald Crown",
        emoji = "🦚",
        subtitle = "Exquisite Mayura feathers with emeralds",
        gemColor = EmeraldGreen
    ),
    CrownAvatarOption(
        id = "crown_ruby_sun",
        name = "Surya Dynasty Sun Crown",
        emoji = "☀️",
        subtitle = "Radiant solar gold crest with crimson rubies",
        gemColor = RubyRed
    ),
    CrownAvatarOption(
        id = "crown_sapphire_ocean",
        name = "Samudra Sapphire Tiara",
        emoji = "🌊",
        subtitle = "Deep oceanic blue sapphire crown of coastal kings",
        gemColor = SapphireBlue
    ),
    CrownAvatarOption(
        id = "crown_warrior_rajput",
        name = "Rajputana Royal Plume",
        emoji = "⚔️",
        subtitle = "Royal warrior turban plume with battle honor",
        gemColor = RoyalAmber
    ),
    CrownAvatarOption(
        id = "crown_maharani_pearl",
        name = "Basra Pearl Crown",
        emoji = "👸",
        subtitle = "Golden maang tikka with luminous Basra pearls",
        gemColor = Color(0xFFFFD54F)
    ),
    CrownAvatarOption(
        id = "crown_lion_emperor",
        name = "Singhasan Lion Crown",
        emoji = "🦁",
        subtitle = "The fierce imperial crest of the royal lion throne",
        gemColor = RoyalGoldDark
    ),
    CrownAvatarOption(
        id = "crown_phoenix_fire",
        name = "Garuda Phoenix Flame Crown",
        emoji = "🔥",
        subtitle = "Mythical celestial flame crown of invincibility",
        gemColor = RubyRedLight
    ),
    CrownAvatarOption(
        id = "crown_chandra_moon",
        name = "Chandra Crescent Diadem",
        emoji = "🌙",
        subtitle = "Silver moon diadem of tranquil royal nights",
        gemColor = Color(0xFFE0E0E0)
    ),
    CrownAvatarOption(
        id = "crown_lotus_blossom",
        name = "Padma Royal Lotus Crown",
        emoji = "🪷",
        subtitle = "Sacred gold and ruby lotus crown of purity",
        gemColor = Color(0xFFF48FB1)
    ),
    CrownAvatarOption(
        id = "crown_falcon_crest",
        name = "Sovereign Falcon Crown",
        emoji = "🦅",
        subtitle = "Sharp royal falcon crest of swift victories",
        gemColor = RoyalAmber
    )
)

data class RegalAvatar(
    val id: String,
    val name: String,
    val kingdom: String,
    val emoji: String,
    val title: String,
    val description: String,
    val crestColor: Color,
    val isUnlocked: Boolean = true
)

val ANCIENT_REGAL_AVATARS = listOf(
    RegalAvatar(
        id = "avatar_vikramaditya",
        name = "Vikramaditya",
        kingdom = "Ancient Ujjain Empire",
        emoji = "🤴",
        title = "Maharaja",
        description = "Legendary emperor of Navaratnas with celestial sun throne",
        crestColor = RoyalGold
    ),
    RegalAvatar(
        id = "avatar_rani_padmini",
        name = "Rani Padmini",
        kingdom = "Chittorgarh Rajputana",
        emoji = "👸",
        title = "Maharani",
        description = "Royal queen of eternal courage adorned in sacred lotus pearls",
        crestColor = Color(0xFFF48FB1)
    ),
    RegalAvatar(
        id = "avatar_ashoka",
        name = "Ashoka The Great",
        kingdom = "Maurya Imperial Dynasty",
        emoji = "🦁",
        title = "Samrat",
        description = "Sovereign of the ancient lion capital and universal peace decree",
        crestColor = RoyalGoldDark
    ),
    RegalAvatar(
        id = "avatar_chhatrapati",
        name = "Chhatrapati Veer",
        kingdom = "Maratha Swarajya",
        emoji = "🛡️",
        title = "Chhatrapati",
        description = "Dauntless protector of royal forts with flaming saffron plume",
        crestColor = Color(0xFFFF6D00)
    ),
    RegalAvatar(
        id = "avatar_rajput_king",
        name = "Maharana Pratap",
        kingdom = "Mewar Solar Kingdom",
        emoji = "⚔️",
        title = "Maharana",
        description = "Unbroken warrior monarch wielding dual sacred talwars",
        crestColor = RubyRed
    ),
    RegalAvatar(
        id = "avatar_shahenshah",
        name = "Shahenshah",
        kingdom = "Mughal Imperial Citadel",
        emoji = "👑",
        title = "Shahenshah",
        description = "Supreme emperor with gold-woven velvet robe and ruby sarpech",
        crestColor = EmeraldGreen
    ),
    RegalAvatar(
        id = "avatar_yuvraj",
        name = "Yuvraj Samar",
        kingdom = "Heir of Mewar",
        emoji = "🦚",
        title = "Yuvraj",
        description = "Noble royal prince wearing peacock-crested turquoise diadem",
        crestColor = SapphireBlue
    ),
    RegalAvatar(
        id = "avatar_citadel_guardian",
        name = "Citadel Guardian",
        kingdom = "Fortress of Gwalior",
        emoji = "🐅",
        title = "Senapati",
        description = "Fierce commander of royal palace guards with Bengal tiger sigil",
        crestColor = RoyalAmber
    ),
    RegalAvatar(
        id = "avatar_samudra_king",
        name = "Samudra Maharaja",
        kingdom = "Chola Maritime Empire",
        emoji = "🌊",
        title = "Devanampriya",
        description = "Master of eastern oceanic fleet with deep sapphire regalia",
        crestColor = SapphireBlueLight
    ),
    RegalAvatar(
        id = "avatar_celestial_sovereign",
        name = "Deva Sovereign",
        kingdom = "Indraprastha Mythic Realm",
        emoji = "✨",
        title = "Chakravartin",
        description = "Ancient mythic kingdom ruler surrounded by luminous golden aura",
        crestColor = Color(0xFFFFD54F)
    )
)

val ROYAL_TITLES = listOf("Maharaja", "Shahenshah", "Yuvraj", "Senapati", "Raja", "Sultan", "Dewan", "Samrat", "Maharani")

val RANDOM_ROYAL_NAMES = listOf(
    "Maharaja Ishu", "Raja Vikram", "Yuvraj Samar", "Shahenshah Kabir",
    "Maharani Gayatri", "Veer Rajput", "Chhatrapati Veer", "Sultan Amar",
    "Royal Phoenix", "Tiger of Mewar", "Golden Lion", "Lord of Courts"
)

fun getCrownAvatarEmoji(avatarId: String): String {
    // Check ancient regal avatars first
    ANCIENT_REGAL_AVATARS.firstOrNull { it.id == avatarId }?.let { return it.emoji }

    return when (avatarId) {
        "avatar_abdul" -> "🕶️"
        "avatar_sisiliya" -> "👩"
        "avatar_kiran" -> "🧑"
        "avatar_flora" -> "👱‍♀️"
        "avatar_maharaja" -> "🤴"
        "avatar_maharani" -> "👸"
        "avatar_warrior" -> "🧔"
        "avatar_prince" -> "👱"
        else -> CROWN_AVATAR_LIST.firstOrNull { it.id == avatarId }?.emoji ?: "👑"
    }
}

fun getAncientAvatar(avatarId: String): RegalAvatar {
    return ANCIENT_REGAL_AVATARS.firstOrNull { it.id == avatarId } ?: ANCIENT_REGAL_AVATARS[0]
}

fun getPawnCrownEmoji(avatarId: String): String {
    return CROWN_AVATAR_LIST.firstOrNull { it.id == avatarId }?.emoji
        ?: ANCIENT_REGAL_AVATARS.firstOrNull { it.id == avatarId }?.emoji
        ?: "👑"
}

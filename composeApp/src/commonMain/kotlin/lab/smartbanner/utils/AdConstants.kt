package lab.smartbanner.utils

import lab.smartbanner.getPlatform

object AdConstants {
    // Check if we are running on Android
    private val isAndroid = getPlatform().name.startsWith("Android")

    // --- HOME GRID BANNER ---
    private const val ANDROID_HOME_ID = "ca-app-pub-6016705200049000/2969709036" // Replace with real Android ID
    private const val IOS_HOME_ID = "ca-app-pub-6016705200049000/5412039547"     // Replace with real iOS ID

    val HOME_GRID_BANNER_ID: String
        get() = if (isAndroid) ANDROID_HOME_ID else IOS_HOME_ID

    // --- PREVIEW BOTTOM BANNER ---
    private const val ANDROID_PREVIEW_ID = "ca-app-pub-6016705200049000/5404300683" // Replace with real Android ID
    private const val IOS_PREVIEW_ID = "ca-app-pub-6016705200049000/9642928599"     // Replace with real iOS ID

    val PREVIEW_BOTTOM_BANNER_ID: String
        get() = if (isAndroid) ANDROID_PREVIEW_ID else IOS_PREVIEW_ID
}

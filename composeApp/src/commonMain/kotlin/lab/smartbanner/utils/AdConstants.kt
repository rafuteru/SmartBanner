package lab.smartbanner.utils

import lab.smartbanner.getPlatform

object AdConstants {
    private val isAndroid = getPlatform().name.startsWith("Android")

    // Set to true during development to ensure ads always load
    private const val USE_TEST_ADS = true

    // Google's official Test Banner IDs
    private const val ANDROID_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val IOS_TEST_ID = "ca-app-pub-3940256099942544/2934735716"

    // --- HOME GRID BANNER ---
    private const val ANDROID_HOME_ID = "ca-app-pub-6016705200049000/2969709036"
    private const val IOS_HOME_ID = "ca-app-pub-6016705200049000/5412039547"

    val HOME_GRID_BANNER_ID: String
        get() = if (USE_TEST_ADS) (if (isAndroid) ANDROID_TEST_ID else IOS_TEST_ID)
        else (if (isAndroid) ANDROID_HOME_ID else IOS_HOME_ID)

    // --- PREVIEW BOTTOM BANNER ---
    private const val ANDROID_PREVIEW_ID = "ca-app-pub-6016705200049000/5404300683"
    private const val IOS_PREVIEW_ID = "ca-app-pub-6016705200049000/9642928599"

    val PREVIEW_BOTTOM_BANNER_ID: String
        get() = if (USE_TEST_ADS) (if (isAndroid) ANDROID_TEST_ID else IOS_TEST_ID)
        else (if (isAndroid) ANDROID_PREVIEW_ID else IOS_PREVIEW_ID)
}
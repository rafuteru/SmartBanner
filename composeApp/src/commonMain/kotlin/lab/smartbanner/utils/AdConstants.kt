package lab.smartbanner.utils

import lab.smartbanner.getPlatform

object AdConstants {
    private val isAndroid = getPlatform().name.startsWith("Android")

    // Set to true during development to ensure ads always load
    private const val USE_TEST_ADS = true

    // Google's official Test IDs
    private const val ANDROID_BANNER_TEST_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val IOS_BANNER_TEST_ID = "ca-app-pub-3940256099942544/2934735716"
    
    private const val ANDROID_REWARDED_TEST_ID = "ca-app-pub-3940256099942544/5224354917"
    private const val IOS_REWARDED_TEST_ID = "ca-app-pub-3940256099942544/1712485313"

    // --- HOME GRID BANNER ---
    private const val ANDROID_HOME_ID = "ca-app-pub-6016705200049000/2969709036"
    private const val IOS_HOME_ID = "ca-app-pub-6016705200049000/5412039547"

    val HOME_GRID_BANNER_ID: String
        get() = if (USE_TEST_ADS) (if (isAndroid) ANDROID_BANNER_TEST_ID else IOS_BANNER_TEST_ID)
        else (if (isAndroid) ANDROID_HOME_ID else IOS_HOME_ID)

    // --- PREVIEW BOTTOM BANNER ---
    private const val ANDROID_PREVIEW_ID = "ca-app-pub-6016705200049000/5404300683"
    private const val IOS_PREVIEW_ID = "ca-app-pub-6016705200049000/9642928599"

    val PREVIEW_BOTTOM_BANNER_ID: String
        get() = if (USE_TEST_ADS) (if (isAndroid) ANDROID_BANNER_TEST_ID else IOS_BANNER_TEST_ID)
        else (if (isAndroid) ANDROID_PREVIEW_ID else IOS_PREVIEW_ID)

    // --- REWARDED AD ---
    private const val ANDROID_REWARDED_ID = "ca-app-pub-6016705200049000/2808064958"
    private const val IOS_REWARDED_ID = "ca-app-pub-6016705200049000/1018671424"

    val REWARDED_AD_ID: String
        get() = if (USE_TEST_ADS) (if (isAndroid) ANDROID_REWARDED_TEST_ID else IOS_REWARDED_TEST_ID)
        else (if (isAndroid) ANDROID_REWARDED_ID else IOS_REWARDED_ID)
}
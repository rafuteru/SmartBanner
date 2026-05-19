package lab.smartbanner

interface AdManager {
    fun showRewardedAd(adUnitId: String, onRewardEarned: () -> Unit)
    fun showInterstitialAd(adUnitId: String, onAdClosed: () -> Unit)
    fun showAppOpenAd(adUnitId: String)
}

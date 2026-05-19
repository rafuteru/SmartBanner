package lab.smartbanner

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AndroidAdManager(private val context: Context) : AdManager {

    override fun showRewardedAd(adUnitId: String, onRewardEarned: () -> Unit) {
        val activity = context.findActivity() ?: return
        
        RewardedAd.load(activity, adUnitId, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {}

            override fun onAdLoaded(ad: RewardedAd) {
                ad.show(activity) {
                    onRewardEarned()
                }
            }
        })
    }

    override fun showInterstitialAd(adUnitId: String, onAdClosed: () -> Unit) {
        val activity = context.findActivity() ?: run {
            onAdClosed()
            return
        }

        InterstitialAd.load(activity, adUnitId, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdClosed()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        onAdClosed()
                    }
                }
                interstitialAd.show(activity)
            }
        })
    }

    override fun showAppOpenAd(adUnitId: String) {
        val activity = context.findActivity() ?: return
        
        AppOpenAd.load(activity, adUnitId, AdRequest.Builder().build(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                ad.show(activity)
            }
        })
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}

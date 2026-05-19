package lab.smartbanner

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AndroidPlatform(private val context: Context?) : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    
    override val isDebug: Boolean
        get() = context?.let {
            (it.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } ?: false

    override val deviceId: String
        get() = context?.let {
            Settings.Secure.getString(it.contentResolver, Settings.Secure.ANDROID_ID)
        } ?: "unknown_android"

    override fun openEmail(recipient: String, subject: String, body: String) {
        context?.let { ctx ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$recipient")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                ctx.startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    ctx.startActivity(Intent.createChooser(fallbackIntent, "Send Email").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (ex: Exception) {
                }
            }
        }
    }

    override fun createImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }

    override fun showRewardedAd(adUnitId: String, onRewardEarned: () -> Unit) {
        val activity = context?.findActivity() ?: return
        
        RewardedAd.load(activity, adUnitId, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {}

            override fun onAdLoaded(ad: RewardedAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {}
                ad.show(activity) {
                    onRewardEarned()
                }
            }
        })
    }

    override fun showInterstitialAd(adUnitId: String, onAdClosed: () -> Unit) {
        val activity = context?.findActivity() ?: run {
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

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        onAdClosed()
                    }
                }
                interstitialAd.show(activity)
            }
        })
    }

    override fun showAppOpenAd(adUnitId: String) {
        val activity = context?.findActivity() ?: return
        
        AppOpenAd.load(activity, adUnitId, AdRequest.Builder().build(), AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                ad.show(activity)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
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

private var platformInstance: Platform? = null

fun initializePlatform(context: Context) {
    platformInstance = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = platformInstance ?: AndroidPlatform(null)

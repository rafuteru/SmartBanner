package lab.smartbanner.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
actual fun AdBanner(
    modifier: Modifier,
    adUnitId: String,
    onAdLoaded: () -> Unit,
    onAdFailedToLoad: () -> Unit
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        onAdLoaded()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        onAdFailedToLoad()
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Update if needed
        }
    )
}

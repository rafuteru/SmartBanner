package lab.smartbanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        initializePlatform(this)
        
        // Initialize Mobile Ads SDK
        MobileAds.initialize(this) {}

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
}

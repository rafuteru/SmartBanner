package lab.smartbanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import lab.smartbanner.data.DataStoreDraftRepository
import lab.smartbanner.data.LocalTemplateRepository
import lab.smartbanner.utils.createDataStore
import lab.smartbanner.utils.createPosterExporter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        initializePlatform(this)

        setContent {
            val context = LocalContext.current
            val templateRepository = remember { LocalTemplateRepository() }
            val dataStore = remember { createDataStore(context) }
            val draftRepository = remember { DataStoreDraftRepository(dataStore) }
            val posterExporter = remember { createPosterExporter(context) }

            App(
                templateRepository = templateRepository,
                draftRepository = draftRepository,
                posterExporter = posterExporter
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
}

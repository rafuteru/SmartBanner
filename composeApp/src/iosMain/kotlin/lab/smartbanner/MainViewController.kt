package lab.smartbanner

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import lab.smartbanner.data.DataStoreDraftRepository
import lab.smartbanner.data.LocalTemplateRepository
import lab.smartbanner.utils.createDataStore
import lab.smartbanner.utils.createPosterExporter

fun MainViewController() = ComposeUIViewController {
    val templateRepository = remember { LocalTemplateRepository() }
    val dataStore = remember { createDataStore(null) }
    val draftRepository = remember { DataStoreDraftRepository(dataStore) }
    val posterExporter = remember { createPosterExporter(null) }

    App(
        templateRepository = templateRepository,
        draftRepository = draftRepository,
        posterExporter = posterExporter
    )
}

package lab.smartbanner

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import platform.UIKit.UIDevice
import platform.UIKit.UIApplication
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIWindow
import platform.Foundation.NSURL
import platform.Foundation.NSCharacterSet
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.NSString
import platform.Foundation.create
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.popoverPresentationController
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean = kotlin.native.Platform.isDebugBinary

    override val deviceId: String
        get() = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_ios"

    @OptIn(ExperimentalForeignApi::class)
    override fun openEmail(recipient: String, subject: String, body: String) {
        val allowedSet = NSCharacterSet.URLQueryAllowedCharacterSet
        val encodedRecipient = NSString.create(string = recipient).stringByAddingPercentEncodingWithAllowedCharacters(allowedSet) ?: ""
        val encodedSubject = NSString.create(string = subject).stringByAddingPercentEncodingWithAllowedCharacters(allowedSet) ?: ""
        val encodedBody = NSString.create(string = body).stringByAddingPercentEncodingWithAllowedCharacters(allowedSet) ?: ""
        
        val apps = listOf(
            "Mail" to "mailto:$encodedRecipient?subject=$encodedSubject&body=$encodedBody",
            "Gmail" to "googlegmail:///co?to=$encodedRecipient&subject=$encodedSubject&body=$encodedBody"
        )

        val availableApps = apps.filter { (_, urlString) ->
            val url = NSURL.URLWithString(urlString)
            url != null && UIApplication.sharedApplication.canOpenURL(url)
        }

        if (availableApps.isEmpty()) {
            val fallbackUrl = "mailto:$encodedRecipient?subject=$encodedSubject&body=$encodedBody"
            NSURL.URLWithString(fallbackUrl)?.let { UIApplication.sharedApplication.openURL(it) }
            return
        }

        if (availableApps.size == 1) {
            NSURL.URLWithString(availableApps[0].second)?.let { UIApplication.sharedApplication.openURL(it) }
            return
        }

        val alert = UIAlertController.alertControllerWithTitle(
            title = "Choose Email App",
            message = "Select an app to contact support",
            preferredStyle = UIAlertControllerStyleActionSheet
        )

        availableApps.forEach { (name, urlString) ->
            alert.addAction(
                UIAlertAction.actionWithTitle(
                    title = name,
                    style = UIAlertActionStyleDefault,
                    handler = { _ ->
                        NSURL.URLWithString(urlString)?.let { UIApplication.sharedApplication.openURL(it) }
                    }
                )
            )
        }

        alert.addAction(
            UIAlertAction.actionWithTitle(
                title = "Cancel",
                style = UIAlertActionStyleCancel,
                handler = null
            )
        )

        // Find the active window and root view controller more reliably
        val window = UIApplication.sharedApplication.windows
            .mapNotNull { it as? UIWindow }
            .firstOrNull { it.isKeyWindow() }
            ?: UIApplication.sharedApplication.keyWindow

        val rootVC = window?.rootViewController
        
        if (rootVC != null) {
            alert.popoverPresentationController?.let { popover ->
                popover.sourceView = rootVC.view
                popover.sourceRect = rootVC.view.bounds
            }
            
            rootVC.presentViewController(alert, animated = true, completion = null)
        } else {
            // Fallback if we can't find a view controller to present from
            NSURL.URLWithString(availableApps[0].second)?.let { UIApplication.sharedApplication.openURL(it) }
        }
    }

    override fun createImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
}

actual fun getPlatform(): Platform = IOSPlatform()

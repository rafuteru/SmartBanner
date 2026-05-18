package lab.smartbanner

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import platform.UIKit.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.experimental.ExperimentalNativeApi

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean = kotlin.native.Platform.isDebugBinary

    override val deviceId: String
        get() = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: "unknown_ios"

    @OptIn(ExperimentalForeignApi::class)
    override fun openEmail(recipient: String, subject: String, body: String) {
        fun String.urlEncode(): String {
            val allowedSet = NSCharacterSet.alphanumericCharacterSet.mutableCopy() as NSMutableCharacterSet
            allowedSet.addCharactersInString("-._~")
            return (this as NSString).stringByAddingPercentEncodingWithAllowedCharacters(allowedSet) ?: this
        }

        val encodedRecipient = recipient.urlEncode()
        val encodedSubject = subject.urlEncode()
        val encodedBody = body.urlEncode()

        val mailUrl = NSURL.URLWithString("mailto:$encodedRecipient?subject=$encodedSubject&body=$encodedBody")
        val gmailUrl = NSURL.URLWithString("googlegmail:///co?to=$encodedRecipient&subject=$encodedSubject&body=$encodedBody")

        val apps = mutableListOf<Pair<String, NSURL>>()
        
        if (mailUrl != null && UIApplication.sharedApplication.canOpenURL(mailUrl)) {
            apps.add("Mail" to mailUrl)
        }
        if (gmailUrl != null && UIApplication.sharedApplication.canOpenURL(gmailUrl)) {
            apps.add("Gmail" to gmailUrl)
        }

        if (apps.isEmpty()) {
            mailUrl?.let { UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any?>(), null) }
            return
        }

        if (apps.size == 1) {
            UIApplication.sharedApplication.openURL(apps[0].second, emptyMap<Any?, Any?>(), null)
            return
        }

        val alert = UIAlertController.alertControllerWithTitle(
            title = "Choose Email App",
            message = "Select an app to contact support",
            preferredStyle = UIAlertControllerStyleActionSheet
        )

        apps.forEach { (name, url) ->
            alert.addAction(
                UIAlertAction.actionWithTitle(
                    title = name,
                    style = UIAlertActionStyleDefault,
                    handler = { _ ->
                        UIApplication.sharedApplication.openURL(url, emptyMap<Any?, Any?>(), null)
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
            UIApplication.sharedApplication.openURL(apps[0].second, emptyMap<Any?, Any?>(), null)
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

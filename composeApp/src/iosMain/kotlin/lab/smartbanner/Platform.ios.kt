package lab.smartbanner

import platform.UIKit.UIDevice
import platform.UIKit.UIApplication
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    override fun openEmail(recipient: String, subject: String, body: String) {
        val urlString = "mailto:$recipient?subject=${subject.replace(" ", "%20")}&body=${body.replace(" ", "%20")}"
        val url = NSURL.URLWithString(urlString)
        if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
}

actual fun getPlatform(): Platform = IOSPlatform()
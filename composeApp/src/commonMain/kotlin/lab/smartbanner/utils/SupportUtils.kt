package lab.smartbanner.utils

import lab.smartbanner.getPlatform

object SupportConstants {
    const val SUPPORT_EMAIL = "coffeeat202labs@gmail.com"
}

fun contactSupport(subject: String, body: String) {
    getPlatform().openEmail(
        recipient = SupportConstants.SUPPORT_EMAIL,
        subject = subject,
        body = body
    )
}

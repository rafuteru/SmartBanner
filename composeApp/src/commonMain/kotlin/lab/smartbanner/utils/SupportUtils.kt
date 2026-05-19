package lab.smartbanner.utils

import lab.smartbanner.getPlatform

object SupportConstants {
    const val SUPPORT_EMAIL = "support@shuvian.com"
}

fun contactSupport(subject: String, body: String, accessCode: String) {
    val formattedBody = """
        $body
        
        ---
        Access Code: $accessCode
    """.trimIndent()

    getPlatform().openEmail(
        recipient = SupportConstants.SUPPORT_EMAIL,
        subject = subject,
        body = formattedBody
    )
}

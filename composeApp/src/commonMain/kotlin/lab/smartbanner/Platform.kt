package lab.smartbanner

interface Platform {
    val name: String
    val isDebug: Boolean
    fun openEmail(recipient: String, subject: String, body: String)
}

expect fun getPlatform(): Platform

package lab.smartbanner

interface Platform {
    val name: String
    fun openEmail(recipient: String, subject: String, body: String)
}

expect fun getPlatform(): Platform

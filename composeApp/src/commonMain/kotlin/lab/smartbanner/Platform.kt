package lab.smartbanner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
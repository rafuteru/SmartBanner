package lab.smartbanner.model

data class Template(
    val id: String,
    val name: String,
    val category: String,
    val previewUrl: String? = null
)

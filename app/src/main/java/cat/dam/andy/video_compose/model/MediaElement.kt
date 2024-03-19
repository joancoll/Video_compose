package cat.dam.andy.video_compose.model
// Classe que representa un element de mitjà per a reproducció

data class MediaElement(
    val title: String,
    val description: String,
    val author: String,
    val mediaLink: String,
    val mimeType: String
)

package mpeciakk.text

class TextComponent {
    var text: String = ""
    var color: String = ""
    var bold = false
    var italic = false
    var underlined = false
    var strikethrough = false
    var obfuscated = false

    val extra = mutableListOf<TextComponent>()
}
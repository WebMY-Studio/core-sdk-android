package us.webmy.core.ui.compose.utils

fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

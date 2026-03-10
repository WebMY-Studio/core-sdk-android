package us.webmy.core_sdk_compose.utils

fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

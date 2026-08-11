package us.webmy.core.internal.theme

internal fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

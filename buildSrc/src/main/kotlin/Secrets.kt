import java.util.Properties

fun Properties.getStringProperty(key: String) = "\"${getProperty(key)}\""

fun Properties.readSecret(secretName: String): String {
    val localPropSecret = getProperty(secretName)
    val secret = localPropSecret ?: System.getenv(secretName)
    return if (secret.isNullOrEmpty()) secretNotFound(secretName) else secret
}

private fun secretNotFound(secretName: String): Nothing {
    throw NoSuchElementException("$secretName secret is not found in local.properties or environment variables")
}
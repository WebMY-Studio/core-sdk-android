package us.webmy.core.internal

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "Internal WebMY SDK API. It may change or disappear without notice — do not use from application code.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
annotation class InternalWebmyApi

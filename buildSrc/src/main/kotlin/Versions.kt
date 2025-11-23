import org.gradle.api.Project

private const val DefaultVersionName = "0.1.19"
private val DefaultVersionCode = (System.currentTimeMillis() / 1000).toInt()

fun Project.computeVersionName(): String = DefaultVersionName
fun Project.computeVersionCode(): Int = DefaultVersionCode

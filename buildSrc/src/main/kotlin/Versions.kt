import org.gradle.api.Project

const val CompileSdkVersion = 37
const val MinSdkVersion = 28
const val TargetSdkVersion = 37
private const val DefaultVersionName = "1.0.4"
private val DefaultVersionCode = (System.currentTimeMillis() / 1000).toInt()

fun Project.computeVersionName(): String = DefaultVersionName

fun Project.computeVersionCode(): Int = DefaultVersionCode

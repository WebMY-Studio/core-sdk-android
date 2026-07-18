import org.gradle.api.Project

const val CompileSdkVersion = 36
const val MinSdkVersion = 28
const val TargetSdkVersion = 36
private const val DefaultVersionName = "0.5.16"
private val DefaultVersionCode = (System.currentTimeMillis() / 1000).toInt()

fun Project.computeVersionName(): String = DefaultVersionName

fun Project.computeVersionCode(): Int = DefaultVersionCode

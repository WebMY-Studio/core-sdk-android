import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class PushAndTag @Inject constructor(
    private val execOps: ExecOperations
) : DefaultTask() {

    init {
        group = "release"
        description = "Commits, pushes, and tags a new release."
    }

    @TaskAction
    fun release() {
        val versionName = project.computeVersionName()

        fun runCommand(vararg command: String) {
            execOps.exec {
                commandLine(*command)
            }
        }

        println("🚀 Releasing version $versionName")

        runCommand("git", "add", ".")
        runCommand("git", "commit", "-m", "Release v$versionName")
        runCommand("git", "push")
        runCommand("git", "tag", versionName)
        runCommand("git", "push", "origin", versionName)

        println("✅ Release v$versionName pushed and tagged.")
    }
}
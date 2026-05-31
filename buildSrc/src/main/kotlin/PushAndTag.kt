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
        val tagName = if (versionName.startsWith("v")) versionName else "v$versionName"

        fun runCommand(vararg command: String) {
            execOps.exec {
                commandLine(*command)
            }
        }

        println("🚀 Releasing version $tagName")

        runCommand("git", "add", ".")
        runCommand("git", "commit", "-m", "Release $tagName")
        runCommand("git", "push")
        runCommand("git", "tag", tagName)
        runCommand("git", "push", "origin", tagName)

        println("✅ Release $tagName pushed and tagged.")
    }
}
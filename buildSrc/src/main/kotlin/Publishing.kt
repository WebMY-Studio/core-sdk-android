import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

fun Project.configureMavenPublishing(artifactIdValue: String, includeDebug: Boolean = false) {
    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("release") {
                    from(components.getByName("release"))
                    groupId = "com.github.WebMY-Studio"
                    artifactId = artifactIdValue
                    version = rootProject.computeVersionName()
                }

                if (includeDebug) {
                    create<MavenPublication>("debug") {
                        from(components.getByName("debug"))
                        groupId = "com.github.WebMY-Studio"
                        artifactId = "$artifactIdValue-debug"
                        version = rootProject.computeVersionName()
                    }
                }
            }
            repositories {
                mavenLocal()
            }
        }
    }
}
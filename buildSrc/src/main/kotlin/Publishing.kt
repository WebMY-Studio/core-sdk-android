import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

fun Project.configureMavenPublishing(artifactIdValue: String) {
    afterEvaluate {
        extensions.configure<PublishingExtension> {
            val webmyGroupId = "com.github.WebMY-Studio"
            publications {
                create<MavenPublication>("release") {
                    from(components.getByName("release"))
                    groupId = webmyGroupId
                    artifactId = artifactIdValue
                    version = rootProject.computeVersionName()
                }
            }
            repositories {
                mavenLocal()
            }
        }
    }
}
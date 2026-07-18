pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven("https://jitpack.io")
        maven("https://artifactory.appodeal.com/appodeal")
        maven("https://verve.jfrog.io/artifactory/verve-gradle-release/")
    }
}

rootProject.name = "WebMY Core SDK"
include(":app")
include(":core")
include(":core-monetization-billing")
include(":core-monetization-ads")

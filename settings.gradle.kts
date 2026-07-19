pluginManagement {
    includeBuild("build-logic")
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
    }
}

rootProject.name = "PulseReport"

include(":app")
include(":core:model")
include(":core:ui")
include(":core:database")
include(":core:healthconnect")
include(":feature:dashboard")
include(":feature:health")
include(":feature:mood")
include(":feature:diary")
include(":feature:export")
include(":feature:sources")
include(":tools:seeder")

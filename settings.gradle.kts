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
    }
}

rootProject.name = "SystemLeveling"

include(":app")
include(":core")
include(":feature:onboarding")
include(":feature:home")
include(":feature:skills")
include(":feature:quests")
include(":feature:inventory")
include(":feature:titles")
include(":feature:finance")
include(":feature:library")
include(":feature:journal")
include(":feature:calendar")

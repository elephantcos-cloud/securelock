pluginManagement {
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

gradle.startParameter.excludedTaskNames.addAll(listOf(":buildSrc:testClasses"))

rootProject.name = "SecureLock"
include(":app")

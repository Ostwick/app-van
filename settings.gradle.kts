pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.scijava.org/content/repositories/public/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://repository.liferay.com/nexus/content/repositories/public/")
        maven(url = "https://maven.scijava.org/content/repositories/public/")
    }
}

rootProject.name = "PDFVan"
include(":app")

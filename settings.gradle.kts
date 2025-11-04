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
        maven { url = java.net.URI("https://jitpack.io") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Chimera"
include(":app")
include(":t8rin:fancy-slider-library")
include(":t8rin:embedded-picker-library")
include(":t8rin:image-reorder-carousel-library")
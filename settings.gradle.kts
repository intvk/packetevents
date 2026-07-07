dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }

        create("testlibs") {
            from(files("testlibs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        maven {
            name = "FabricMC"
            url = uri("https://maven.fabricmc.net/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "packetevents"
include("api")
include("netty-common")
// Platform modules (spigot/bungeecord/velocity/sponge/fabric*) are dropped in the Silnestium
// slim consumption: only api + netty-common are needed as a composite build, and the fabric
// modules drag in fabric-loom which is expensive and irrelevant to a headless bridge.
// The :patch:adventure-text-serializer-* modules (Adventure-4 internals forks) are dropped
// too — under Adventure 5 the api shadowJar bundles the stock 5.x serializers instead.

// Workspace composite override (grim.sh writes this file on clone/pull to rename
// rootProject when the workspace pulls multiple sibling repos with the same name).
if (file("workspace.gradle.kts").exists()) apply(from = "workspace.gradle.kts")

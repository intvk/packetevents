// Silnestium fork: buildSrc is its own build, so the root settings' foojay-resolver does not
// auto-provision toolchains here and jvmToolchain(21) fails on machines that only have a newer
// JDK (e.g. the JDK-25 container image). Versionless apply: the root settings already load
// foojay 1.0.0 onto the classpath, and re-stating a version there is an error.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
import com.github.retrooper.compression.strategy.dir.JsonBase64DataDirStrategy
import com.github.retrooper.compression.strategy.dir.JsonRegistryCompressionDirStrategy
import com.github.retrooper.compression.strategy.dir.JsonToNbtDirStrategy
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    packetevents.`shadow-conventions`
    packetevents.`library-conventions`
    `mapping-compression`
    `pe-version`
}

// papermc repo + disableAutoTargetJvm needed for mockbukkit
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

java {
    withJavadocJar()
}

dependencies {
    compileOnlyApi(libs.bundles.adventure)
    compileOnlyApi(libs.bundles.adventure.serializers)
    implementation(libs.adventure.api)
    // Silnestium slim build: the vendored :patch:adventure-text-serializer-* sources are
    // Adventure-4 internals forks and do not compile against Adventure 5. The stock 5.x
    // serializer artifacts are bundled into the shadowJar instead (compileShadowOnly =
    // compileOnly + shadowed), where the existing relocation rewrites them to the same
    // io.github.retrooper.packetevents.adventure.serializer.* packages consumers import.
    "compileShadowOnly"(libs.adventure.text.serializer.gson)
    "compileShadowOnly"(libs.adventure.text.serializer.legacy)
    compileOnly(libs.gson)
    compileOnly(libs.adventure.text.logger.slf4j)
    // Adventure 5's text-logger-slf4j no longer leaks slf4j-api onto consumers' compile
    // classpaths; Slf4jLogManager needs it declared explicitly.
    compileOnly(libs.slf4j.api)
    compileOnly(libs.log4j.api)
    compileOnly(libs.checkerqual)

    testRuntimeOnly(testlibs.bundles.adventure)
    testRuntimeOnly(testlibs.bundles.adventure.serializers)
    testImplementation(libs.bundles.adventure)
    testImplementation(libs.adventure.text.serializer.gson)
    testImplementation(libs.adventure.text.serializer.legacy)
    testImplementation(libs.adventure.text.logger.slf4j)
    testImplementation(project(":netty-common"))
    testImplementation(testlibs.mockbukkit)
    testImplementation(testlibs.paper.api)
    testImplementation(testlibs.slf4j)
    testImplementation(testlibs.bundles.junit)
    testImplementation(libs.netty)
    testImplementation(libs.classgraph)
    // (:spigot test dependency dropped — the spigot module is excluded from the Silnestium slim build)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.2")
}

mappingCompression {
    mappingDirectory = rootDir.resolve("mappings")
    outDirectory = project.layout.buildDirectory.dir("mappings/generated/assets/mappings")

    with<JsonToNbtDirStrategy> {
        compress("data")
    }
    with<JsonBase64DataDirStrategy> {
        compress("item_base_components")
    }
    with<JsonRegistryCompressionDirStrategy> {
        compress("registries")
    }
}

tasks {
    assemble {
        setDependsOn(dependsOn.filterNot { it == "shadowNoAdventure" })
    }

    javadoc {
        val options = options as StandardJavadocDocletOptions
        options.use(true)
        options.tags("versions:A:Minecraft Versions:")
        mustRunAfter(generateVersionsFile)
    }

    sourcesJar {
        mustRunAfter(generateVersionsFile)
    }

    withType<JavaCompile> {
        dependsOn(generateVersionsFile)
    }

    processResources {
        dependsOn(compressMappings)
        from(project.layout.buildDirectory.dir("mappings/generated").get())
    }

    generateVersionsFile {
        packageName = "com.github.retrooper.packetevents.util"
    }

    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    shadowJar {
        exclude {
            val path = it.path
            path.startsWith("net/kyori") && !path.startsWith("net/kyori/adventure/text/serializer") && !path.startsWith(
                "net/kyori/option"
            )
        }
    }
}

publishing {
    publications {
        named<MavenPublication>("shadow") {
            artifact(tasks["javadocJar"])
        }
    }
}

// When packetevents is consumed as an included (composite) build — gradle.parent != null, e.g. the
// Silnestium `includeBuild("packetevents")` — expose the relocated shadowJar as the api/runtime
// artifact instead of the plain jar. The published Maven `packetevents-api` jar IS the shadowJar
// (relocated `net.kyori.adventure.text.serializer`/`option` -> `io.github.retrooper.packetevents.*`
// plus the bundled `assets/mappings` resources), and downstream consumers (grim:common,
// anticheat-grim) compile against those relocated packages. Without this redirect, dependency
// substitution hands out the un-relocated plain jar and grim's relocated-package imports fail to
// resolve. Standalone builds (gradle.parent == null) are unaffected.
if (gradle.parent != null) {
    configurations.apiElements.get().outgoing.apply {
        artifacts.clear()
        // Secondary variants (classes/resources dirs) would still win for project-to-project
        // compilation and bypass the shadowJar, hiding the relocated packages — drop them.
        variants.clear()
        artifact(tasks.shadowJar)
    }
    configurations.runtimeElements.get().outgoing.apply {
        artifacts.clear()
        variants.clear()
        artifact(tasks.shadowJar)
    }
}

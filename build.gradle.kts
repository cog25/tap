/*
 * Copyright (c) 2020 Noonmaru
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.HttpURLConnection
import java.net.URL

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    `maven-publish`
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/")
        mavenLocal()
    }

    dependencies {
        compileOnly(kotlin("stdlib-jdk8"))
        // Custom dependency builder
        compileOnly(
            "https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar",
            "ProtocolLib.jar"
        )
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

        testImplementation("junit:junit:4.13")
        testImplementation("org.mockito:mockito-core:3.3.3")
        testImplementation("org.powermock:powermock-module-junit4:2.0.7")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.7")
        testImplementation("org.slf4j:slf4j-api:1.7.25")
        testImplementation("org.apache.logging.log4j:log4j-core:2.8.2")
        testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.8.2")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
        javadoc {
            options.encoding = "UTF-8"
        }
    }
}

project(":paper") {
    dependencies {
        implementation(project(":api"))
        subprojects.filter { it.name != path }.forEach { subproject ->
            implementation(subproject)
        }
    }
}

val jitpackPath = "jitpack"
val jitpackFileTree = fileTree(mapOf("dir" to "jitpack", "include" to listOf("*-$version.jar")))

subprojects {
    if (path in setOf(":api", ":paper")) {
        // setup api & test plugin
        dependencies {
            compileOnly("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")

            testImplementation("com.destroystokyo.paper:paper-api:1.12.2-R0.1-SNAPSHOT")
        }
    } else {
        //setup nms
        dependencies {
            implementation(project(":api"))
        }
        tasks {
            // Move net.minecraft.server artifacts to $rootDir/jitpack for jitpack.io
            create<Copy>("jitpack") {
                from(jar)
                into(File(rootDir, jitpackPath))
            }
        }
    }
}

tasks {
    jar {
        subprojects.filter { it.name != ":paper" }.forEach { subproject ->
            from(subproject.sourceSets["main"].output)
        }
    }
    // Build JavaPlugin
    create<Jar>("paperJar") {
        subprojects.forEach { subproject ->
            from(subproject.sourceSets["main"].output)
        }
        archiveBaseName.set("Tap")
        archiveVersion.set("") // For bukkit plugin update
        archiveClassifier.set("") // Remove 'all'
        dependsOn(classes)
    }
    create<Copy>("copyPaperJarToDocker") {
        from(named("paperJar"))

        var dest = File(".docker/plugins")
        // Copy bukkit plugin update folder
        if (File(dest, jar.get().archiveFileName.get()).exists()) dest = File(dest, "update")

        into(dest)

        doLast {
            println("Copy to ${dest.path}")
        }
    }
    create<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        for (subproject in subprojects) {
            from(subproject.sourceSets["main"].allSource)
        }
    }
    // jitpack.io
    shadowJar {
        // remove classifier for maven repo
        gradle.taskGraph.whenReady {
            if (hasTask(":publishTapPublicationToMavenLocal"))
                archiveClassifier.set("")
        }
    }

    create<Delete>("cleanJitpack") {
        delete(jitpackFileTree)
    }
}

dependencies {
    implementation(project(":api"))
    implementation(jitpackFileTree)
}

publishing {
    publications {
        create<MavenPublication>("Tap") {
            project.shadow.component(this)
            artifact(tasks["sourcesJar"])
        }
    }
}

fun DependencyHandlerScope.compileOnly(url: String, name: String): Dependency? {
    File("libs").mkdir()
    val jar = File("libs", name)
    val log = File("libs", "$name.log")
    if (!log.exists()) {
        log.createNewFile()
    }
    (URL(url).openConnection() as HttpURLConnection).run {
        val lastModified = getHeaderField("Last-Modified")
        // (lib.jar).log 파일로 최신버전 관리
        if (lastModified != String(log.readBytes())) {
            inputStream.use { stream ->
                jar.writeBytes(stream.readBytes())
                log.writeText(lastModified)
            }
        }
    }
    return compileOnly(files(jar.toURI()))
}
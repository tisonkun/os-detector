/*
 * Copyright 2024 tison <wander4096@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.benediktritter.maven.plugin.development.task.GenerateHelpMojoSourcesTask
import de.benediktritter.maven.plugin.development.task.GenerateMavenPluginDescriptorTask

plugins {
    id("com.diffplug.spotless") version "6.24.0"
    // @see https://www.benediktritter.de/maven-plugin-development/
    id("de.benediktritter.maven-plugin-development") version "0.4.2"
    id("java")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "java")

    spotless {
        format("misc") {
            target("*.gradle", "*.md", ".gitignore")
            endWithNewline()
            indentWithSpaces(4)
            trimTrailingWhitespace()
        }
        java {
            palantirJavaFormat("2.36.0")
            importOrder("\\#|")
            removeUnusedImports()
            endWithNewline()
            indentWithSpaces(4)
            trimTrailingWhitespace()
        }
    }

    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")
        testCompileOnly("org.projectlombok:lombok:1.18.30")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.assertj:assertj-core:3.11.1")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

project("lib") {
    dependencies {
        implementation("org.slf4j:slf4j-api:1.7.36")
        testImplementation("org.slf4j:slf4j-simple:1.7.36")
    }
}

project("plugin-maven") {
    apply(plugin = "de.benediktritter.maven-plugin-development")
    mavenPlugin {
        name = "OS Detector Maven Plugin"
        artifactId = "os-detector-maven-plugin"
    }
    tasks.withType<GenerateMavenPluginDescriptorTask>().configureEach {
        this.notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
    }
    tasks.withType<GenerateHelpMojoSourcesTask>().configureEach {
        this.notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
    }

    dependencies {
        compileOnly("org.apache.maven:maven-core:3.9.4")
        compileOnly("org.apache.maven:maven-plugin-api:3.9.4")
        compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.9.0")
        implementation(project(":lib"))
    }
}

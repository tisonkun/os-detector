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
    id("java-publish")
    // @see https://www.benediktritter.de/maven-plugin-development/
    id("de.benediktritter.maven-plugin-development") version "0.4.2"
}

mavenPlugin {
    name = "OS Detector :: Maven Plugin"
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

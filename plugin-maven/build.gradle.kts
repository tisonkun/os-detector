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
    id("de.benediktritter.maven-plugin-development") version "0.4.3"
}

mavenPlugin {
    name = project.property("artifactName").toString()
    artifactId = project.property("artifactId").toString()
}

tasks.withType<GenerateMavenPluginDescriptorTask>().configureEach {
    this.notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
}

tasks.withType<GenerateHelpMojoSourcesTask>().configureEach {
    this.notCompatibleWithConfigurationCache("https://github.com/britter/maven-plugin-development/issues/8")
}

dependencies {
    compileOnly("org.apache.maven:maven-api-spi:4.0.0-alpha-13")
    compileOnly("org.apache.maven:maven-plugin-api:3.9.9")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.11.0")
    implementation("org.apache.maven:maven-core:3.9.9")
    implementation("org.codehaus.plexus:plexus-utils:4.0.0")
    implementation(project(":lib"))
    testImplementation("org.apache.maven.shared:maven-invoker:3.3.0")
}

tasks.test {
    dependsOn(tasks.publishToMavenLocal)
    dependsOn(project(":lib").tasks.publishToMavenLocal)
    systemProperty("project.basedir", projectDir)
    systemProperty("project.version", version)
}

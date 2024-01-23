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

plugins {
    id("groovy")
    id("com.gradle.plugin-publish") version "1.2.0"
    id("java-gradle-plugin")
}

dependencies {
    implementation(localGroovy())
    implementation(project(":lib"))
    implementation("javax.inject:javax.inject:1")
    implementation("org.slf4j:slf4j-api:1.7.36")
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = "https://github.com/tisonkun/os-detector/"
    vcsUrl = "https://github.com/tisonkun/os-detector.git"

    plugins {
        create("osDetectorPlugin") {
            id = "com.tisonkun.osdetector"
            implementationClass = "com.tisonkun.os.gradle.DetectPlugin"
            displayName = project.findProperty("artifactName") as String
            description = project.findProperty("description") as String
            tags = listOf("os", "osdetector", "arch", "classifier")
        }
    }
}

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
    id("com.gradle.plugin-publish") version "1.2.0"
    id("groovy")
    id("java-gradle-plugin")
    id("signing")
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

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            artifactId = project.property("artifactId").toString()

            pom {
                description = project.property("description").toString()
                name = project.property("artifactName").toString()
                url = "https://github.com/tisonkun/os-detector/"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "tison"
                        name = "Zili Chen"
                        email = "wander4096@gmail.com"
                    }
                }

                scm {
                    connection = "scm:git:https://github.com/tisonkun/os-detector.git"
                    developerConnection = "scm:git:https://github.com/tisonkun/os-detector.git"
                    url = "https://github.com/tisonkun/os-detector/"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

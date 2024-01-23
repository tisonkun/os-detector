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
    id("java")
    id("maven-publish")
    id("signing")
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("artifactId").toString()
            from(components["java"])

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

                if (artifactId.equals("os-detector-maven-plugin")) {
                    withXml {
                        asNode().appendNode("prerequisites").appendNode("maven", "3.1.0")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

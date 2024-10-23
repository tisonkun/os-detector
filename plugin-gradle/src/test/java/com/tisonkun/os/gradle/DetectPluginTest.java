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

package com.tisonkun.os.gradle;

import static org.assertj.core.api.Assertions.assertThat;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

class DetectPluginTest {
    @Test
    void pluginAddsExtensionToProject() {
        final Project project = ProjectBuilder.builder().build();
        project.apply(action -> action.plugin("com.tisonkun.osdetector"));

        final OSDetector detector = (OSDetector) project.getExtensions().getByName("osdetector");
        assertThat(detector).isNotNull();
        assertThat(detector.getOs()).isNotNull();
        assertThat(detector.getArch()).isNotNull();
        assertThat(detector.getClassifier()).isEqualTo(detector.getOs() + "-" + detector.getArch());
        System.err.println("classifier=" + detector.getClassifier());

        if (detector.getOs().equals("linux")) {
            final OSDetector.Release release = detector.getRelease();
            assertThat(release.getId()).isNotNull();
            System.err.println("release.id=" + release.getId());
            System.err.println("release.version=" + release.getVersion());
            System.err.println("release.isLike(debian)=" + release.isLike("debian"));
            System.err.println("release.isLike(redhat)=" + release.isLike("redhat"));
        } else {
            assertThat(detector.getRelease()).isNull();
        }
    }
}

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

package com.tisonkun.os.gradle

import static org.assertj.core.api.Assertions.assertThat;
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThatThrownBy

class DetectPluginTest {
    @Test
    void pluginAddsExtensionToProject() {
        final Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.tisonkun.osdetector'

        assertThat(project.osdetector).isNotNull()
        assertThat(project.osdetector.os).isNotNull()
        assertThat(project.osdetector.arch).isNotNull()
        assertThat(project.osdetector.os + '-' + project.osdetector.arch).isEqualTo(project.osdetector.classifier)
        System.err.println('classifier=' + project.osdetector.classifier)

        if (project.osdetector.os == 'linux') {
            assertThat(project.osdetector.release.id).isNotNull()
            System.err.println('release.id=' + project.osdetector.release.id)
            System.err.println('release.version=' + project.osdetector.release.version)
            System.err.println('release.isLike(debian)=' + project.osdetector.release.isLike('debian'))
            System.err.println('release.isLike(redhat)=' + project.osdetector.release.isLike('redhat'))
        } else {
            assertThat(project.osdetector.release).isNull()
        }
    }

    @Test
    void setClassifierWithLikes() {
        final Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.tisonkun.osdetector'
        project.osdetector.classifierWithLikes = ['debian', 'fedora']
        assertThat(project.osdetector.os).isNotNull()
        assertThat(project.osdetector.arch).isNotNull()
        System.err.println('classifier=' + project.osdetector.classifier)
        assertThatThrownBy {
            project.osdetector.classifierWithLikes = ['debian']
        }.isExactlyInstanceOf(IllegalStateException)
    }
}

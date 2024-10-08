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

package org.tisonkun.os.maven;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.File;
import java.util.Collections;
import java.util.Properties;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Verify os-detector-maven-plugin can be used as an extension.
 */
class DetectExtensionTest {

    @ParameterizedTest
    @ValueSource(strings = {"mvn31", "mvn32", "mvn33", "mvn35", "mvn36", "mvn38", "mvn39", "mvn40beta4"})
    void testExtension(String mavenVersion) throws Exception {
        final Properties properties = new Properties();
        properties.put("os-detector-maven-plugin.version", System.getProperty("project.version"));

        final InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File("src/test/resources/test-project-extension/pom.xml"));
        request.setProperties(properties);
        request.setBatchMode(true);
        request.setGoals(Collections.singletonList("test"));

        final Invoker invoker = new DefaultInvoker();
        invoker.setMavenExecutable(new File(mavenVersion + "/mvnw"));
        final InvocationResult result = invoker.execute(request);
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getExecutionException()).isNull();
    }
}

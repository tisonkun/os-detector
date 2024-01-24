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

package com.tisonkun.os.core;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collections;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class DetectorTest {
    @Test
    void testDetectProperties() {
        final Properties properties = new Properties();
        final Detector detector = new Detector(System.out::println);
        detector.detect(properties, Collections.emptyList());
        assertThat(properties)
                .containsKeys(
                        "os.detected.name",
                        "os.detected.arch",
                        "os.detected.bitness",
                        "os.detected.classifier",
                        "os.detected.version");
    }

    @Test
    void testDetectedData() {
        final Detector detector = new Detector(System.out::println);
        final Detected detected = detector.detect();
        assertThat(detected.os).isNotNull();
        assertThat(detected.arch).isNotNull();
        assertThat(detected.bitness).isNotZero();
        assertThat(detected.classifier).isNotEmpty();
        assertThat(detected.version).isNotEmpty();
    }
}

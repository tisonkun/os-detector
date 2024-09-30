/*
 * Copyright 2024 gnodet <gnodet@gmail.com>
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

package com.tisonkun.os.maven;

import com.tisonkun.os.core.Detector;
import com.tisonkun.os.core.FileOperationProvider;
import com.tisonkun.os.core.SystemPropertyOperationProvider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import org.apache.maven.api.spi.PropertyContributor;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;

/**
 * Set Maven session user properties.
 */
@Component(role = PropertyContributor.class)
public class DetectPropertyContributor implements PropertyContributor {

    private final Logger logger;

    @Inject
    DetectPropertyContributor(Logger logger) {
        super();
        this.logger = logger;
    }

    @Override
    public void contribute(Map<String, String> map) {
        logger.info("The os-detector Maven 4 extension is registered, OS and CPU architecture properties will be provided.");
        DetectExtension.disable();

        final Properties props = new Properties();
        props.putAll(map);

        final Detector detector =
                new Detector(new SimpleSystemPropertyOperations(map), new SimpleFileOperations(), logger::debug);
        detector.detect(props, getClassifierWithLikes(map));
    }

    /**
     * Inspects the session's user and project properties for the {@link
     * DetectMojo#CLASSIFIER_WITH_LIKES_PROPERTY} and separates the property into a list.
     */
    private static List<String> getClassifierWithLikes(Map<String, String> map) {
        // Check to see if the project defined the
        return DetectMojo.getClassifierWithLikes(map.get(DetectMojo.CLASSIFIER_WITH_LIKES_PROPERTY));
    }

    private static class SimpleSystemPropertyOperations implements SystemPropertyOperationProvider {
        final Map<String, String> map;

        private SimpleSystemPropertyOperations(Map<String, String> map) {
            this.map = map;
        }

        @Override
        public String getSystemProperty(String name) {
            return System.getProperty(name);
        }

        @Override
        public String getSystemProperty(String name, String def) {
            return System.getProperty(name, def);
        }

        @Override
        public String setSystemProperty(String name, String value) {
            map.put(name, value);
            return System.setProperty(name, value);
        }
    }

    private static class SimpleFileOperations implements FileOperationProvider {
        @Override
        public InputStream readFile(String fileName) throws IOException {
            return Files.newInputStream(Paths.get(fileName));
        }
    }
}

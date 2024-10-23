/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.tisonkun.os.gradle;

import com.tisonkun.os.core.DefaultFileOperations;
import com.tisonkun.os.core.DefaultSystemPropertyOperations;
import com.tisonkun.os.core.Detector;
import com.tisonkun.os.core.FileOperationProvider;
import com.tisonkun.os.core.SystemPropertyOperationProvider;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

@SuppressWarnings("unused")
@Slf4j
public abstract class OSDetector {
    @Inject
    public abstract ProviderFactory getProviderFactory();

    @Inject
    public abstract ProjectLayout getProjectLayout();

    @SuppressWarnings("FieldCanBeLocal")
    private final Project project;

    private Impl impl;

    public OSDetector(Project project) {
        this.project = project;
    }

    public String getOs() {
        return (String) getImpl().detectedProperties.get(Detector.DETECTED_NAME);
    }

    public String getArch() {
        return (String) getImpl().detectedProperties.get(Detector.DETECTED_ARCH);
    }

    public String getClassifier() {
        return (String) getImpl().detectedProperties.get(Detector.DETECTED_CLASSIFIER);
    }

    public Release getRelease() {
        Impl impl = getImpl();
        Object releaseId = impl.detectedProperties.get(Detector.DETECTED_RELEASE);
        if (releaseId == null) {
            return null;
        }
        return new Release(impl);
    }

    private synchronized Impl getImpl() {
        if (impl == null) {
            if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0) {
                impl = new Impl(
                        new ConfigurationTimeSafeSystemPropertyOperations(), new ConfigurationTimeSafeFileOperations());
            } else {
                impl = new Impl(new DefaultSystemPropertyOperations(), new DefaultFileOperations());
            }
        }
        return impl;
    }

    /**
     * Accessor to information about the current OS release.
     */
    public static class Release {
        private final Impl impl;

        private Release(Impl impl) {
            this.impl = impl;
        }

        /**
         * Returns the release ID.
         */
        public String getId() {
            return (String) impl.detectedProperties.get(Detector.DETECTED_RELEASE);
        }

        /**
         * Returns the version ID.
         */
        public String getVersion() {
            return (String) impl.detectedProperties.get(Detector.DETECTED_RELEASE_VERSION);
        }

        /**
         * Returns {@code true} if this release is a variant of the given base release (for example,
         * ubuntu is "like" debian).
         */
        public boolean isLike(String baseRelease) {
            return impl.detectedProperties.containsKey(Detector.DETECTED_RELEASE_LIKE_PREFIX + baseRelease);
        }
    }

    private static class Impl {
        private final Properties detectedProperties = new Properties();

        private Impl(SystemPropertyOperationProvider sysPropOps, FileOperationProvider fsOps) {
            final Detector detector = new Detector(sysPropOps, fsOps, log::info);
            detector.detect(detectedProperties);
        }
    }

    @SuppressWarnings("deprecation")
    private static <T> Provider<T> forUseAtConfigurationTime(Provider<T> provider) {
        // Deprecated and a noop starting in 7.4
        if (GradleVersion.current().compareTo(GradleVersion.version("7.4")) < 0) {
            return provider.forUseAtConfigurationTime();
        } else {
            return provider;
        }
    }

    /**
     * Provides system property operations compatible with Gradle configuration cache.
     */
    private final class ConfigurationTimeSafeSystemPropertyOperations implements SystemPropertyOperationProvider {
        @Override
        public String getSystemProperty(String name) {
            return forUseAtConfigurationTime(getProviderFactory().systemProperty(name))
                    .getOrNull();
        }

        @Override
        public String getSystemProperty(String name, String def) {
            return forUseAtConfigurationTime(getProviderFactory().systemProperty(name))
                    .getOrElse(def);
        }

        @Override
        public String setSystemProperty(String name, String value) {
            // no-op
            return null;
        }
    }

    /**
     * Provides filesystem operations compatible with Gradle configuration cache.
     */
    private final class ConfigurationTimeSafeFileOperations implements FileOperationProvider {
        @Override
        public InputStream readFile(String fileName) throws IOException {
            RegularFile file = getProjectLayout().getProjectDirectory().file(fileName);
            byte[] bytes = forUseAtConfigurationTime(
                            getProviderFactory().fileContents(file).getAsBytes())
                    .getOrNull();
            if (bytes == null) {
                throw new FileNotFoundException(fileName + " not exist");
            }
            return new ByteArrayInputStream(bytes);
        }
    }
}

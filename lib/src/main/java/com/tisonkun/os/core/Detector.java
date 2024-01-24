/*
 * Copyright 2014 Trustin Heuiseung Lee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tisonkun.os.core;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Detector {
    public static final String DETECTED_NAME = "os.detected.name";
    public static final String DETECTED_ARCH = "os.detected.arch";
    public static final String DETECTED_BITNESS = "os.detected.bitness";
    public static final String DETECTED_VERSION = "os.detected.version";
    public static final String DETECTED_VERSION_MAJOR = DETECTED_VERSION + ".major";
    public static final String DETECTED_VERSION_MINOR = DETECTED_VERSION + ".minor";
    public static final String DETECTED_CLASSIFIER = "os.detected.classifier";
    public static final String DETECTED_RELEASE = "os.detected.release";
    public static final String DETECTED_RELEASE_VERSION = DETECTED_RELEASE + ".version";
    public static final String DETECTED_RELEASE_LIKE_PREFIX = DETECTED_RELEASE + ".like.";

    private static final String LINUX_ID_PREFIX = "ID=";
    private static final String LINUX_ID_LIKE_PREFIX = "ID_LIKE=";
    private static final String LINUX_VERSION_ID_PREFIX = "VERSION_ID=";
    private static final String[] LINUX_OS_RELEASE_FILES = {"/etc/os-release", "/usr/lib/os-release"};
    private static final String REDHAT_RELEASE_FILE = "/etc/redhat-release";
    private static final String[] DEFAULT_REDHAT_VARIANTS = {"rhel", "fedora"};
    private static final Pattern VERSION_REGEX = Pattern.compile("((\\d+)\\.(\\d+)).*");
    private static final Pattern REDHAT_MAJOR_VERSION_REGEX = Pattern.compile("(\\d+)");

    private final SystemPropertyOperationProvider systemPropertyOperationProvider;
    private final FileOperationProvider fileOperationProvider;
    private final LoggingProvider loggingProvider;

    public Detector(LoggingProvider loggingProvider) {
        this(new DefaultSystemPropertyOperations(), new DefaultFileOperations(), loggingProvider);
    }

    public Detector(
            SystemPropertyOperationProvider systemPropertyOperationProvider,
            FileOperationProvider fileOperationProvider,
            LoggingProvider loggingProvider) {
        this.systemPropertyOperationProvider = systemPropertyOperationProvider;
        this.fileOperationProvider = fileOperationProvider;
        this.loggingProvider = loggingProvider;
    }

    public Detected detect() {
        return detect(Collections.emptyList());
    }

    public Detected detect(List<String> classifierWithLikes) {
        final String osName = systemPropertyOperationProvider.getSystemProperty("os.name");
        final String osArch = systemPropertyOperationProvider.getSystemProperty("os.arch");
        final String osVersion = systemPropertyOperationProvider.getSystemProperty("os.version");

        final OS detectedName = normalizeOs(osName);
        final Arch detectedArch = normalizeArch(osArch);
        final int detectedBitness = determineBitness(detectedArch.name());

        // Assume the default classifier, without any os "like" extension.
        final StringBuilder detectedClassifierBuilder = new StringBuilder();
        detectedClassifierBuilder.append(detectedName);
        detectedClassifierBuilder.append('-');
        detectedClassifierBuilder.append(detectedArch);

        // For Linux systems, add additional properties regarding details of the OS.
        final LinuxRelease linuxRelease = OS.linux != detectedName ? null : getLinuxRelease();
        if (linuxRelease != null) {
            for (String classifierLike : classifierWithLikes) {
                if (linuxRelease.like.contains(classifierLike)) {
                    detectedClassifierBuilder.append('-');
                    detectedClassifierBuilder.append(classifierLike);
                    // First one wins.
                    break;
                }
            }
        }

        final String detectedClassifier = detectedClassifierBuilder.toString();
        return new Detected(detectedBitness, osVersion, detectedClassifier, detectedName, detectedArch, linuxRelease);
    }

    public void detect(Properties props, List<String> classifierWithLikes) {
        loggingProvider.info("------------------------------------------------------------------------");
        loggingProvider.info("Detecting the operating system and CPU architecture");
        loggingProvider.info("------------------------------------------------------------------------");

        final Detected detected = detect(classifierWithLikes);

        setProperty(props, DETECTED_NAME, detected.os.name());
        setProperty(props, DETECTED_ARCH, detected.arch.name());
        setProperty(props, DETECTED_BITNESS, String.valueOf(detected.bitness));

        final Matcher versionMatcher = VERSION_REGEX.matcher(detected.version);
        if (versionMatcher.matches()) {
            setProperty(props, DETECTED_VERSION, versionMatcher.group(1));
            setProperty(props, DETECTED_VERSION_MAJOR, versionMatcher.group(2));
            setProperty(props, DETECTED_VERSION_MINOR, versionMatcher.group(3));
        }

        final String failOnUnknownOS = systemPropertyOperationProvider.getSystemProperty("failOnUnknownOS");
        if (!"false".equalsIgnoreCase(failOnUnknownOS)) {
            if (detected.os.isUnknown()) {
                final String osName = systemPropertyOperationProvider.getSystemProperty("os.name");
                throw new DetectionException("unknown os.name: " + osName);
            }
            if (detected.arch.isUnknown()) {
                final String osArch = systemPropertyOperationProvider.getSystemProperty("os.arch");
                throw new DetectionException("unknown os.arch: " + osArch);
            }
        }

        // For Linux systems, add additional properties regarding details of the OS.
        final LinuxRelease linuxRelease = detected.linuxRelease;
        if (linuxRelease != null) {
            setProperty(props, DETECTED_RELEASE, linuxRelease.id);
            if (linuxRelease.version != null) {
                setProperty(props, DETECTED_RELEASE_VERSION, linuxRelease.version);
            }

            // Add properties for all systems that this OS is "like".
            for (String like : linuxRelease.like) {
                final String propKey = DETECTED_RELEASE_LIKE_PREFIX + like;
                setProperty(props, propKey, "true");
            }
        }

        setProperty(props, DETECTED_CLASSIFIER, detected.classifier);
    }

    private void setProperty(Properties props, String name, String value) {
        props.setProperty(name, value);
        systemPropertyOperationProvider.setSystemProperty(name, value);
        loggingProvider.info(name + ": " + value);
    }

    private static OS normalizeOs(String value) {
        value = normalize(value);
        if (value.startsWith("aix")) {
            return OS.aix;
        }
        if (value.startsWith("hpux")) {
            return OS.hpux;
        }
        if (value.startsWith("os400")) {
            // avoid the names such as os4000
            final boolean cornerCase = value.length() > 5 && Character.isDigit(value.charAt(5));
            if (!cornerCase) {
                return OS.os400;
            }
        }
        if (value.startsWith("linux")) {
            return OS.linux;
        }
        if (value.startsWith("mac") || value.startsWith("osx")) {
            return OS.osx;
        }
        if (value.startsWith("freebsd")) {
            return OS.freebsd;
        }
        if (value.startsWith("openbsd")) {
            return OS.openbsd;
        }
        if (value.startsWith("netbsd")) {
            return OS.netbsd;
        }
        if (value.startsWith("solaris") || value.startsWith("sunos")) {
            return OS.sunos;
        }
        if (value.startsWith("windows")) {
            return OS.windows;
        }
        if (value.startsWith("zos")) {
            return OS.zos;
        }
        return OS.unknown;
    }

    private static Arch normalizeArch(String value) {
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return Arch.x86_64;
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return Arch.x86_32;
        }
        if (value.matches("^(ia64w?|itanium64)$")) {
            return Arch.itanium_64;
        }
        if ("ia64n".equals(value)) {
            return Arch.itanium_32;
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return Arch.sparc_32;
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return Arch.sparc_64;
        }
        if (value.matches("^(arm|arm32)$")) {
            return Arch.arm_32;
        }
        if ("aarch64".equals(value)) {
            return Arch.aarch_64;
        }
        if (value.matches("^(mips|mips32)$")) {
            return Arch.mips_32;
        }
        if (value.matches("^(mipsel|mips32el)$")) {
            return Arch.mipsel_32;
        }
        if ("mips64".equals(value)) {
            return Arch.mips_64;
        }
        if ("mips64el".equals(value)) {
            return Arch.mipsel_64;
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return Arch.ppc_32;
        }
        if (value.matches("^(ppcle|ppc32le)$")) {
            return Arch.ppcle_32;
        }
        if ("ppc64".equals(value)) {
            return Arch.ppc_64;
        }
        if ("ppc64le".equals(value)) {
            return Arch.ppcle_64;
        }
        if ("s390".equals(value)) {
            return Arch.s390_32;
        }
        if ("s390x".equals(value)) {
            return Arch.s390_64;
        }
        if (value.matches("^(riscv|riscv32)$")) {
            return Arch.riscv;
        }
        if ("riscv64".equals(value)) {
            return Arch.riscv64;
        }
        if ("e2k".equals(value)) {
            return Arch.e2k;
        }
        if ("loongarch64".equals(value)) {
            return Arch.loongarch_64;
        }
        return Arch.unknown;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    private int determineBitness(String architecture) {
        // try the widely adopted sun specification first.
        String bitness = systemPropertyOperationProvider.getSystemProperty("sun.arch.data.model", "");

        if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
            return Integer.parseInt(bitness, 10);
        }

        // bitness from sun.arch.data.model cannot be used. Try the IBM specification.
        bitness = systemPropertyOperationProvider.getSystemProperty("com.ibm.vm.bitmode", "");

        if (!bitness.isEmpty() && bitness.matches("[0-9]+")) {
            return Integer.parseInt(bitness, 10);
        }

        // as a last resort, try to determine the bitness from the architecture.
        return guessBitnessFromArchitecture(architecture);
    }

    public static int guessBitnessFromArchitecture(String arch) {
        return arch.contains("64") ? 64 : 32;
    }

    private LinuxRelease getLinuxRelease() {
        // First, look for the os-release file.
        for (String osReleaseFileName : LINUX_OS_RELEASE_FILES) {
            LinuxRelease res = parseLinuxOsReleaseFile(osReleaseFileName);
            if (res != null) {
                return res;
            }
        }

        // Older versions of redhat don't have /etc/os-release. In this case, try
        // parsing this file.
        return parseLinuxRedhatReleaseFile();
    }

    /**
     * Parses a file in the format of {@code /etc/os-release} and return a {@link LinuxRelease}
     * based on the {@code ID}, {@code ID_LIKE}, and {@code VERSION_ID} entries.
     */
    private LinuxRelease parseLinuxOsReleaseFile(String fileName) {
        BufferedReader reader = null;
        try {
            InputStream in = fileOperationProvider.readFile(fileName);
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            String id = null;
            String version = null;
            final Set<String> likeSet = new LinkedHashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the ID line.
                if (line.startsWith(LINUX_ID_PREFIX)) {
                    // Set the ID for this version.
                    id = normalizeOsReleaseValue(line.substring(LINUX_ID_PREFIX.length()));

                    // Also add the ID to the "like" set.
                    likeSet.add(id);
                    continue;
                }

                // Parse the VERSION_ID line.
                if (line.startsWith(LINUX_VERSION_ID_PREFIX)) {
                    // Set the ID for this version.
                    version = normalizeOsReleaseValue(line.substring(LINUX_VERSION_ID_PREFIX.length()));
                    continue;
                }

                // Parse the ID_LIKE line.
                if (line.startsWith(LINUX_ID_LIKE_PREFIX)) {
                    line = normalizeOsReleaseValue(line.substring(LINUX_ID_LIKE_PREFIX.length()));

                    // Split the line on any whitespace.
                    final String[] parts = line.split("\\s+");
                    Collections.addAll(likeSet, parts);
                }
            }

            if (id != null) {
                return new LinuxRelease(id, version, likeSet);
            }
        } catch (IOException ignored) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            closeQuietly(reader);
        }
        return null;
    }

    /**
     * Parses the {@code /etc/redhat-release} and returns a {@link LinuxRelease} containing the
     * ID and like ["rhel", "fedora", ID]. Currently only supported for CentOS, Fedora, and RHEL.
     * Other variants will return {@code null}.
     */
    private LinuxRelease parseLinuxRedhatReleaseFile() {
        BufferedReader reader = null;
        try {
            InputStream in = fileOperationProvider.readFile(REDHAT_RELEASE_FILE);
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

            // There is only a single line in this file.
            String line = reader.readLine();
            if (line != null) {
                line = line.toLowerCase(Locale.US);

                final String id;
                String version = null;
                if (line.contains("centos")) {
                    id = "centos";
                } else if (line.contains("fedora")) {
                    id = "fedora";
                } else if (line.contains("red hat enterprise linux")) {
                    id = "rhel";
                } else {
                    // Other variants are not currently supported.
                    return null;
                }

                final Matcher versionMatcher = REDHAT_MAJOR_VERSION_REGEX.matcher(line);
                if (versionMatcher.find()) {
                    version = versionMatcher.group(1);
                }

                final Set<String> likeSet = new LinkedHashSet<String>(Arrays.asList(DEFAULT_REDHAT_VARIANTS));
                likeSet.add(id);

                return new LinuxRelease(id, version, likeSet);
            }
        } catch (IOException ignored) {
            // Just absorb. Don't treat failure to read /etc/os-release as an error.
        } finally {
            closeQuietly(reader);
        }
        return null;
    }

    // Remove any quotes from the string.
    private static String normalizeOsReleaseValue(String value) {
        return value.trim().replace("\"", "");
    }

    private static void closeQuietly(Closeable obj) {
        try {
            if (obj != null) {
                obj.close();
            }
        } catch (IOException ignored) {
        }
    }
}

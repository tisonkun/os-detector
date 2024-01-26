# OS Detector

A detector for the OS name and architecture, providing a uniform classifier to be used in the names of native artifacts.

This repository is an effort to provide the OS detection logic the same as [os-maven-plugin](https://github.com/trustin/os-maven-plugin/) as a standalone artifact, and redistribute the Maven plugin as well as [the Gradle plugin](https://github.com/google/osdetector-gradle-plugin) (developed by Google) based on such a core lib to align the manner.

I'm seeking for merging these three efforts into one. Check [this issue](https://github.com/trustin/os-maven-plugin/issues/70#issuecomment-1906110062) for more information.

Currently, I'm actively maintaining this repository (lib, plugin-maven and plugin-gradle) for publicly testing, as well as bugfixes + improvements. You can use it as a production-ready solution since the original logics are battle-tested over the years.

## Programmable

OS detector is provided as a normal module.

You can use it with Maven:

```xml
 <dependency>
    <groupId>com.tisonkun.os</groupId>
    <artifactId>os-detector-core</artifactId>
    <version>${os-detector.version}</version>
</dependency>
```

... or Gradle:

```groovy
implementation("com.tisonkun.os:os-detector-core:$osDetectorVersion")
```

The common usage is:

```java
public static void main(String[] args) {
    final Detector detector = new Detector(/* ... */);
    final Detected detected = detector.detect();
}
```

... where the `Detected` structure is defined as:

```java
public class Detected {
    public final int bitness;
    public final String version;
    public final String classifier;
    public final OS os;
    public final Arch arch;
    @Nullable
    public final LinuxRelease linuxRelease;
}
```

## Maven Extension

You can use OS detector as a Maven extension:

```xml
<build>
    <extensions>
        <extension>
            <groupId>com.tisonkun.os</groupId>
            <artifactId>os-detector-maven-plugin</artifactId>
            <version>${os-detector.version}</version>
        </extension>
    </extensions>
</build>
```

## Gradle Plugin

You can use OS detector as a Gradle plugin:

```groovy
plugins {
  id "com.tisonkun.osdetector" version "$osDetectorVersion"
}
```

The plugin creates osdetector extension in your project, through which you can access the following attributes:

* `osdetector.os`: normalized OS name
* `osdetector.arch`: architecture
* `osdetector.classifier`: classifier, which is `osdetector.os + '-' + osdetector.arch`, e.g., `linux-x86_64`
* `osdetector.release`: only available if `osdetector.os` is `linux`. `null` on non-linux systems. It provides additional information about the linux release:
  * `id`: the ID for the linux release
  * `version`: the version ID for this linux release
  * `isLike(baseRelease)`: `true` if this release is a variant of the given base release. For example, ubuntu is a variant of debian, so on a debian or ubuntu system `isLike('debian')` returns `true`.

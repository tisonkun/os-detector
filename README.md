# OS Detector

A detector for the OS name and architecture, providing a uniform classifier to be used in the names of native artifacts.

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
    final Properties props = new Properties();
    detector.detect(props, Collections.emptyList());
}
```

It's possible to expose the `props` in a more programmable structure, and define the returned properties in constant enums (patch is welcome).

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

# How to release OS Detector

```shell
./gradlew publishToMavenLocal
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
./gradlew publishPlugins --validate-only
./gradlew publishPlugins
```

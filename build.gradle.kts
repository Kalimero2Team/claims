plugins {
    `java-library`
    id("com.github.ben-manes.versions") version "0.44.0"
}

allprojects{
    group = "com.kalimero2.team"
    version = "1.1.3"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

subprojects{
    apply{
        plugin("java-library")
    }
}
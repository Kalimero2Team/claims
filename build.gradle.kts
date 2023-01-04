plugins {
    `java-library`
}

allprojects{
    group = "com.kalimero2.team"
    version = "1.1.5-SNAPSHOT"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

subprojects{
    apply{
        plugin("java-library")
    }
}
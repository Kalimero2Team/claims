plugins {
    `java-library`
}

allprojects{
    group = "com.kalimero2.team"
    version = "2.0.1"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

subprojects{
    apply{
        plugin("java-library")
    }
}
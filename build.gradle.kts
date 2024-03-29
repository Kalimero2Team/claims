plugins {
    `java-library`
}

allprojects{
    group = "com.kalimero2.team"
    version = "2.0.6"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

subprojects{
    apply{
        plugin("java-library")
    }
}

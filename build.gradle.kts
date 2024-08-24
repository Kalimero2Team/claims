plugins {
    `java-library`
}

allprojects{
    group = "com.kalimero2.team"
    version = "2.0.7"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

subprojects{
    apply{
        plugin("java-library")
    }
}

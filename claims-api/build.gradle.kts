plugins {
    java
    `maven-publish`
}

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

publishing {
    repositories {
        maven {
            name = "kalimero2Repo"
            url = if(version.toString().endsWith("-SNAPSHOT")) {
                uri("https://repo.kalimero2.com/snapshots")
            } else {
                uri("https://repo.kalimero2.com/releases")
            }
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}


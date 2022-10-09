plugins {
    java
    `maven-publish`
}

group = "com.kalimero2.team"
version = "2.7-SNAPSHOT"



publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}


import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.plugin.yml)
}

group = "com.kalimero2.team"
version = "2.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.squaremap.api)
    compileOnly(project(":claims-api"))
}

bukkit {
    main = "com.kalimero2.team.claims.squaremap.SquareMapClaims"
    apiVersion = "1.20"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("byquanton")
    depend = listOf("squaremap","claims-paper")
}
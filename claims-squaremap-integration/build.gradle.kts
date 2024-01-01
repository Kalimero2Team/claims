import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.plugin.yml)
}


repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    compileOnly(platform("org.spongepowered:configurate-bom:4.1.2"))
    compileOnly("org.spongepowered:configurate-yaml")

    compileOnly(libs.paper.api)
    compileOnly(libs.squaremap.api)
    compileOnly(project(":claims-api"))
}

bukkit {
    main = "com.kalimero2.team.claims.squaremap.SquaremapClaims"
    apiVersion = "1.20"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("byquanton")
    depend = listOf("squaremap","claims-paper")
}
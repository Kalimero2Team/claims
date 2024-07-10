import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    alias(libs.plugins.paper.run)
    alias(libs.plugins.plugin.yml)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    bukkitLibrary(libs.cloud.paper)
    bukkitLibrary(libs.commons.dbcp2)
    implementation(project(":claims-api"))
}

tasks{
    assemble {
        dependsOn(shadowJar)
    }
    runServer{
        downloadPlugins {
            // ViaVersion
            modrinth("viaversion","5.0.2-SNAPSHOT+457")
            // squaremap
            modrinth("squaremap", "1.2.4")
            // Geyser (in this case Thermalquelle)
            github("Kalimero2Team", "Thermalquelle","latest-dev", "Geyser-Spigot.jar")
            // Floodgate
            url("https://download.geysermc.org/v2/projects/floodgate/versions/2.2.3/builds/109/downloads/spigot")
        }

        minecraftVersion("1.20.6")
    }


}

bukkit {
    main = "com.kalimero2.team.claims.paper.PaperClaims"
    apiVersion = "1.20"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    authors = listOf("byquanton")
    softDepend = listOf("floodgate")
}
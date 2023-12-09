dependencyResolutionManagement{
    versionCatalogs{
        create("libs"){
            // Core
            plugin("shadow","com.github.johnrengelman.shadow").version("8.1.1")

            version("floodgate-api","2.0-SNAPSHOT")
            version("cloud", "1.8.4")

            library("floodgate-api","org.geysermc.floodgate","api").versionRef("floodgate-api")

            // Paper
            plugin("paper-run","xyz.jpenilla.run-paper").version("2.2.0")
            plugin("paper-userdev","io.papermc.paperweight.userdev").version("1.5.8")
            plugin("plugin-yml","net.minecrell.plugin-yml.bukkit").version("0.6.0")

            library("paper-api","io.papermc.paper","paper-api").version("1.20.1-R0.1-SNAPSHOT")

            library("cloud-paper","cloud.commandframework","cloud-paper").versionRef("cloud")

            // SquareMap Integration

            library("squaremap-api","xyz.jpenilla","squaremap-api").version("1.2.3")
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "claims"
include("claims-api")
include("claims-paper")
include("claims-squaremap-integration")

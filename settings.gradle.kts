dependencyResolutionManagement{
    versionCatalogs{
        create("libs"){
            // Core
            plugin("shadow","com.github.johnrengelman.shadow").version("8.1.1")

            version("floodgate-api","2.0-SNAPSHOT")
            version("cloud", "1.8.4")
            version("customblockdata","2.2.0")
            version("morepersistentdatatypes","2.4.0")

            library("floodgate-api","org.geysermc.floodgate","api").versionRef("floodgate-api")
            library("customblockdata","com.jeff_media","CustomBlockData").versionRef("customblockdata")
            library("morepersistentdatatypes","com.jeff_media","MorePersistentDataTypes").versionRef("morepersistentdatatypes")

            // Paper
            plugin("paper-run","xyz.jpenilla.run-paper").version("2.1.0")
            plugin("paper-userdev","io.papermc.paperweight.userdev").version("1.5.5")
            plugin("plugin-yml","net.minecrell.plugin-yml.bukkit").version("0.5.3")

            version("paper-api","1.19.4-R0.1-SNAPSHOT")

            library("cloud-paper","cloud.commandframework","cloud-paper").versionRef("cloud")
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

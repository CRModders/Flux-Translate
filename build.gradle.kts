plugins {
    id("application") // TODO: Remove this plugin
    id("maven-publish")
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    ivy {
        name = "Cosmic Reach"
        url = URI("https://cosmic-archive.netlify.app/")
        patternLayout {
            artifact("/Cosmic Reach-[revision].jar")
        }

        metadataSources {
            artifact()
        }

        content {
            includeGroup("finalforeach")
        }
    }
    ivy {
        // The game provider for Fabric (equivalent of Cosmic Quilt for Fabric)
        name = "Galactic Loader"
        url = "https://github.com/GalacticLoader/GalacticLoader/releases/download/"
        patternLayout {
            artifact("/[revision]/GalacticLoader-[revision].jar")
        }
        // This is required in Gradle 6.0+ as metadata file (ivy.xml) is mandatory
        metadataSources {
            artifact()
        }

        content {
            includeGroup("galacticloader")
        }
    }

    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.fabricmc.net/")
    maven("https://repo.spongepowered.org/maven/")
}


configurations {
    cosmicreach // Config to provide the Cosmic Reach project
    compileOnly.extendsFrom(cosmicreach) // Allows cosmic reach to be used in the codebase

    shadowMe // Allows specifying which stuff gets shadowed
    api.extendsFrom(shadowMe)

    internal { // Allows to include something without it being in the maven
        visible = false
        canBeConsumed = false
        canBeResolved = false
    }
    compileClasspath.extendsFrom(internal)
    runtimeClasspath.extendsFrom(internal)
    testCompileClasspath.extendsFrom(internal)
    testRuntimeClasspath.extendsFrom(internal)


    gameMod // Config to be able to load Fabric Mods (Quilt loads mods from the classpath)
    internal.extendsFrom(gameMod)
}

shadowJar {
    configurations = [project.configurations.shadowMe]
    mainClassName = "dev.crmodders.flux.FluxAPI"
}

val modIncl: Configuration by configurations.creating {
    configurations.compileOnly.get().extendsFrom(this)
    configurations.runtimeOnly.get().extendsFrom(this)
}

dependencies {
    // Cosmic Reach jar
    cosmicreach("finalforeach:cosmicreach:${cosmic_reach_version}")

    // Fabric Loader (for accessing its stuff in the code)
    compileOnly("net.fabricmc:fabric-loader:${fabric_loader_version}") // Include the base Fabric Loader so we can only use classes from that
    compileOnly("net.fabricmc:sponge-mixin:0.12.5+mixin.0.8.5")
    compileOnly("io.github.llamalad7:mixinextras-fabric:0.3.5")

    // Modmenu
    internal("org.codeberg.CRModders:modmenu:${modmenu_version}")

    // Flux API
//    compileOnly("dev.crmodders:FluxAPI:${flux_api_version}")

    // Json stuff
//    shadowMe("com.google.guava:guava:33.0.0-jre")
//    shadowMe("com.google.code.gson:gson:2.9.1")
//    shadowMe("org.hjson:hjson:${hjson_version}")

    // Logging
//    shadowMe("org.tinylog:tinylog:${tiny_logger_version}")
//    shadowMe("com.github.tobiasrm:tinylog-coloredconsole:${tiny_logger_version}")

    modIncl("dev.crmodders:FluxAPI:0.5.0")

}

tasks.processResources {
    val resourceTargets = listOf("fabric.mod.json")

    // Left item is the name in the target, right is the variable name
    val replaceProperties = mutableMapOf(
            "mod_version"     to project.version,
            "mod_name"        to project.name,
            "mod_id"          to id,
            "mod_desc"        to description,
            "mod_group"       to project.group,
            "cosmic_reach_version"        to cosmic_reach_version,
    )


    inputs.properties(replaceProperties)
    replaceProperties.put["project"] = project
    filesMatching(resourceTargets) {
        expand(replaceProperties)
    }
}


// Sets up all the fabric mods (quilt's mods are gotten through the classpath)
val getModLocations: (Configuration config) {
    StringBuilder sb = new StringBuilder()
    for (obj in config.allDependencies) {
        sb.append(File.pathSeparator + config.files(obj)[0])
    }
    return sb.toString()
}

// Sets the current working path
val setCurWorkingDir(Task curTask) {
    // Change the run directory
    File runningDir = new File("run/")
    if (!runningDir.exists())
        runningDir.mkdirs()
    curTask.workingDir = runningDir
}

tasks.register("runQuilt", JavaExec) {
    group = "runs" // Sets the task's group
    dependsOn "jar" // To run this project in the game, depend on the creation of jar task

    dependencies {
        // Cosmic Quilt
        implementation("org.codeberg.CRModders:cosmic-quilt:${cosmic_quilt_version}")
    }


    jvmArgs = [
            "-Dloader.development=true", // Allows stuff to be found through the classpath
            "-Dloader.gameJarPath=" + configurations.cosmicreach.asPath, // Defines path to Cosmic Reach
    ]

    setCurWorkingDir(tasks.runQuilt)
    classpath = sourceSets.main.runtimeClasspath
    mainClass = "org.quiltmc.loader.impl.launch.knot.KnotClient" // Quilt's main class
}

tasks.register("runFabric", JavaExec) {
    group = "runs" // Sets the task's group
    dependsOn "shadowJar" // Fabric doesnt search classpaths correctly, so we use the shadowed jar

    dependencies {
        // Galactic Loader
        implementation("galacticloader:galacticloader:${fabric_loader_version}")

        // Fabric Loader and its stuff
        implementation("net.fabricmc:fabric-loader:${fabric_loader_version}")
        implementation("net.fabricmc:tiny-mappings-parser:0.2.2.14")
        implementation("net.fabricmc:access-widener:2.1.0")
        implementation("net.fabricmc:sponge-mixin:0.12.5+mixin.0.8.5")
        implementation("org.ow2.asm:asm:9.6")
        implementation("org.ow2.asm:asm-util:9.6")
        implementation("org.ow2.asm:asm-tree:9.6")
        implementation("org.ow2.asm:asm-analysis:9.6")
        implementation("org.ow2.asm:asm-commons:9.6")
        implementation("io.github.llamalad7:mixinextras-fabric:0.3.5")
    }


    jvmArgs = [
            "-Dfabric.skipMcProvider=true", // Stops Fabric from attempting to find mappings, and all the other Minecraft stuff
            "-Dfabric.gameJarPath=" + configurations.cosmicreach.asPath, // Defines path to Cosmic Reach
            "-Dfabric.addMods=" +
                    shadowJar.archiveFile.get().asFile + // Add the jar of this project (using shadow on Fabric)
                    getModLocations(configurations.gameMod) // Adds the jars of any Quilt or Fabric mods added (only needed for Fabric as it doesnt load stuff from classpath)
    ]

    setCurWorkingDir(tasks.runFabric)
    classpath = sourceSets.main.runtimeClasspath // Loads all the classpaths
    mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient" // Fabric's main class
}



java {
    withSourcesJar()
    // withJavadocJar() // If docs are included with the project, this line can be un-commented

    // Sets the Java version
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        maven(MavenPublication) {
            groupId = group
            //DO NOT REMOVE; THIS IS NEEDED FOR JITPACK
            artifactId = id

            from components.java
        }
    }
}
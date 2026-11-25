plugins {
    `java-library`
}

group = property("group") as String
version = property("version") as String

// Default target is Paper 26.1.2. Override with e.g.
//   ./gradlew build -PpaperApi=26.2.build.135-stable -PapiVersion=26.2
val paperApi = providers.gradleProperty("paperApi").getOrElse("26.1.2.build.74-stable")
val apiVersion = providers.gradleProperty("apiVersion").getOrElse("26.1")
val javaVersion = providers.gradleProperty("javaVersion").getOrElse("25").toInt()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperApi")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
    options.compilerArgs.add("-Xlint:all,-processing")
}

tasks.processResources {
    val props = mapOf("version" to project.version, "apiVersion" to apiVersion)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    archiveBaseName.set("SlimeRecipe")
}

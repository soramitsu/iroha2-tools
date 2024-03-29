import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.8.22"
}

group = "jp.co.soramitsu"
version = "1.0-SNAPSHOT"

tasks.jar {
    manifest {
        attributes["Main-Class"] = "jp.co.soramitsu.MainKt"
    }
    from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val iroha2Ver by System.getProperties()
    val kotlinVer by System.getProperties()

    implementation(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")

    // iroha2
    api("com.github.hyperledger.iroha-java:client:$iroha2Ver")
    implementation("com.github.hyperledger.iroha-java:model:$iroha2Ver")
    implementation("com.github.hyperledger.iroha-java:block:$iroha2Ver")

    testImplementation("com.github.hyperledger.iroha-java:test-tools:$iroha2Ver")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVer")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

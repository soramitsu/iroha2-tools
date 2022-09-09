import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.6.10"
}

group = "jp.co.soramitsu"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    val iroha2Ver by System.getProperties()
    val kotlinVer by System.getProperties()

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.apache.commons:commons-csv:1.8")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")

    // iroha2
    api("com.github.hyperledger.iroha-java:client:$iroha2Ver")
    implementation("com.github.hyperledger.iroha-java:model:$iroha2Ver")
    implementation("com.github.hyperledger.iroha-java:block:$iroha2Ver")

    testImplementation("com.github.hyperledger.iroha-java:testcontainers:$iroha2Ver")
    testImplementation("com.github.hyperledger.iroha-java:test-tools:$iroha2Ver")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVer")

    // crypto
    implementation("net.i2p.crypto:eddsa:0.3.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.65")
    implementation("com.github.multiformats:java-multihash:1.3.0")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

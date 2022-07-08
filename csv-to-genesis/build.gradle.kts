import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "jp.co.soramitsu.orillion"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    val iroha2Ver by System.getProperties()
    val kotlinVer by System.getProperties()

    implementation("org.apache.commons:commons-csv:1.8")

//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVer")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVer")

//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

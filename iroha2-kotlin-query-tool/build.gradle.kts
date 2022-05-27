import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
	id("org.jmailen.kotlinter") version "3.9.0"
}

group = "jp.co.soramitsu.orillion"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11


repositories {
	mavenCentral()
	maven(url = "https://jitpack.io")
}

dependencies {
	val iroha2Ver by System.getProperties()
	val i2pCryptoEddsa by System.getProperties()
	val bouncyCastleVer by System.getProperties()
	val multihashVersion by System.getProperties()
	val coroutinesVer by System.getProperties()

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVer")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVer")

	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

	//iroha2
	implementation("com.github.hyperledger.iroha-java:client:$iroha2Ver")
	implementation("com.github.hyperledger.iroha-java:model:$iroha2Ver")

	//crypto
	implementation("net.i2p.crypto:eddsa:$i2pCryptoEddsa")
	implementation("org.bouncycastle:bcprov-jdk15on:$bouncyCastleVer")
	implementation("com.github.multiformats:java-multihash:$multihashVersion")
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

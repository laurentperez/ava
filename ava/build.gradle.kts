plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.allopen") version "1.7.21"
    id("io.quarkus")
    id("org.jetbrains.kotlin.plugin.jpa") version "1.7.21"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.7.21"
}

repositories {
    mavenCentral()
    mavenLocal()
//    maven {
//        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//	mavenContent {
//            snapshotsOnly()
//        }
//    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-rest-client-jackson")
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-smallrye-openapi")
    implementation("io.quarkus:quarkus-quartz")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

//    implementation("net.bis5.mattermost4j:mattermost4j-core:0.25.0")
    implementation("net.bis5.mattermost4j:mattermost4j-core:1.0.0-beta.1")
    implementation("net.bis5.mattermost4j:mattermost4j-resteasy:1.0.0-beta.1")

    //implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    //implementation("jakarta.activation:jakarta.activation-api:2.1.0")
//    implementation("org.jboss.slf4j:slf4j-jboss-logmanager")
//    implementation("org.slf4j:jul-to-slf4j")
    // implementation("org.glassfish.jersey.connectors:jersey-apache-connector:3.0.4")

    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    annotationProcessor("io.quarkus:quarkus-panache-common")
    implementation("io.quarkus:quarkus-jdbc-postgresql")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation("io.rest-assured:rest-assured")
}

group = "fr.ava"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.Embeddable")
    annotation("jakarta.persistence.MappedSuperclass")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}

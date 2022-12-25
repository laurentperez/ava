plugins {
	java
	id("org.springframework.boot") version "3.0.1"
	id("io.spring.dependency-management") version "1.1.0"
}

group = "com.ia"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	//implementation("org.springframework.boot:spring-boot-starter-web")
runtimeOnly("org.springframework.boot:spring-boot-devtools")	
implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("net.bis5.mattermost4j:mattermost4j-core:0.25.0")
	
testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

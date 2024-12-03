plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("jacoco")
}

group = "ru.tbank"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

val wiremockTestcontainersVersion: String by extra("1.0-alpha-13")

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.liquibase:liquibase-core")
	runtimeOnly("org.postgresql:postgresql")

	implementation("org.springframework.boot:spring-boot-starter-validation")

	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.sun.mail:javax.mail:1.6.2")
	implementation("javax.activation:activation:1.1.1")

	implementation("org.telegram:telegrambots:6.9.7.1")

	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.jsoup:jsoup:1.18.1")

	implementation("org.springframework.data:spring-data-redis:3.4.0")
	implementation("redis.clients:jedis:5.2.0")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

	implementation("com.vladsch.flexmark:flexmark-all:0.64.8")

	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")

	testImplementation("io.rest-assured:rest-assured")
	testImplementation("org.wiremock:wiremock-standalone:3.6.0")
	testImplementation("org.wiremock.integrations.testcontainers:wiremock-testcontainers-module:$wiremockTestcontainersVersion")
	testImplementation("org.testcontainers:junit-jupiter:1.19.7")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.8"
}

val jacocoExclusions = listOf(
	"ru/tbank/emailcheckerbot/configuration/**",
	"ru/tbank/emailcheckerbot/dto/**",
	"ru/tbank/emailcheckerbot/entity/**",
	"ru/tbank/emailcheckerbot/exception/**",
	"ru/tbank/emailcheckerbot/repository/**"
)

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	classDirectories.setFrom(
		files(
			classDirectories.files.map {
				fileTree(it) {
					exclude(jacocoExclusions)
				}
			}
		)
	)
	reports {
		xml.required.set(true)
		csv.required.set(true)
		html.required.set(true)
	}
}
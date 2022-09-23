
group = "fr.amanin"
version = "0.1"

plugins {
    java
    val ktVersion = "1.7.10"
    kotlin("jvm") version ktVersion
    kotlin("plugin.serialization") version ktVersion
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url="https://repo.spring.io/release")
}

val springBootVersion = "2.7.3"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:2.7.3"))

    // Kt specifics
    implementation(kotlin("stdlib-jdk8"))

    implementation(kotlinx.coroutines)
    // Kt serialization
    implementation(kotlinx.serialization.json)

    // Spring reactive stack
    implementation("org.springframework:spring-webflux")
//    implementation(platform("io.projectreactor:reactor-bom:2020.0.7"))
//    implementation("io.projectreactor:reactor-core")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor.netty:reactor-netty")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // For SO answer: https://stackoverflow.com/questions/61312690/how-to-elegantly-serialize-and-deserialize-opencv-yaml-calibration-data-in-java/
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation(libs.opencv)

    // For fr.amanin.stackoverflow.ktorstaticaccess
    implementation(libs.ktor.server.jetty)
    implementation("com.github.ben-manes.caffeine:caffeine")

    // For SO question https://stackoverflow.com/questions/64137800/kotlin-and-exposed-orm
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation("com.h2database:h2")
    implementation("com.zaxxer:HikariCP")

    implementation("org.jsoup:jsoup:1.15.3")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }

    compileJava
    compileTestJava

    test {
        useJUnitPlatform()
    }
}

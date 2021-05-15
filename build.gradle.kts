
group = "fr.amanin"
version = "0.1"

val ktorVersion = "1.3.1"
val exposedVersion = "0.24.1"

plugins {
    java
    val ktVersion = "1.5.0"
    kotlin("jvm") version ktVersion
    kotlin("plugin.serialization") version ktVersion
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()
    maven(url="http://repo.spring.io/release")
}

dependencies {
    // Kt specifics
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")
    // Kt serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")

    // Spring reactive stack
    implementation("org.springframework:spring-webflux:5.3.7")
    implementation(platform("io.projectreactor:reactor-bom:2020.0.7"))
//    implementation("io.projectreactor:reactor-core")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.projectreactor.netty:reactor-netty")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")

    // For SO answer: https://stackoverflow.com/questions/61312690/how-to-elegantly-serialize-and-deserialize-opencv-yaml-calibration-data-in-java/
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.3")
    implementation("org.openpnp:opencv:4.3.0-1")

    // For fr.amanin.stackoverflow.ktorstaticaccess
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.5")

    // For SO question https://stackoverflow.com/questions/64137800/kotlin-and-exposed-orm
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.h2database:h2:1.4.199")
    implementation("com.zaxxer", "HikariCP", "3.4.5")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileJava
    compileTestJava

    test {
        useJUnitPlatform()
    }
}

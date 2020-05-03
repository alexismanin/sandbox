plugins {
    java
    kotlin("jvm") version "1.3.71"
}

group = "fr.amanin"
version = "0.1"

repositories {
    mavenCentral()
    maven(url="http://repo.spring.io/release")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.5")

    implementation(platform("io.projectreactor:reactor-bom:Dysprosium-SR6"))
    implementation("io.projectreactor:reactor-core")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")

    // For SO answer: https://stackoverflow.com/questions/61312690/how-to-elegantly-serialize-and-deserialize-opencv-yaml-calibration-data-in-java/
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.3")
    implementation("org.openpnp:opencv:4.3.0-1")
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

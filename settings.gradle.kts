rootProject.name = "reactive-sandbox"

dependencyResolutionManagement {
    versionCatalogs {
        create("kotlinx") {
            library("serialization.json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        }

        create("libs") {
            // library("exposed", )
            library("opencv", "org.openpnp:opencv:4.3.0-1")

            version("exposed", "0.24.1").let {
                val grp = "org.jetbrains.exposed"
                library("exposed.core", grp, "exposed-core").versionRef(it)
                library("exposed.dao", grp, "exposed-dao").versionRef(it)
                library("exposed.jdbc", grp, "exposed-jdbc").versionRef(it)
                library("exposed.java.time", grp, "exposed-java-time").versionRef(it)
            }

            version("ktor", "1.3.1").let {
                val grp = "io.ktor"
                library("ktor.server.jetty", grp, "ktor-server-jetty").versionRef(it)
            }
        }
    }
}
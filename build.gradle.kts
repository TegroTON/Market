plugins {
    application
    kotlin("multiplatform") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
}

val ktorVersion = "2.2.1"
val kodeinVersion = "7.15.0"

group = "money.tegro"
version = "1.3.4"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("io.ktor:ktor-server-call-logging-jvm:2.1.2")
    implementation("io.ktor:ktor-server-call-id-jvm:2.1.2")
    implementation("io.ktor:ktor-server-cors-jvm:2.1.2")
    implementation("io.ktor:ktor-server-core-jvm:2.1.2")
    implementation("io.ktor:ktor-server-host-common-jvm:2.1.2")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.1.2")
}

application {
    mainClass.set("money.tegro.market.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf(
        "-Dio.ktor.development=$isDevelopment",
        "-Dorg.slf4j.simpleLogger.defaultLogLevel=${if (isDevelopment) "debug" else "info"}"
    )
}

kotlin {
    jvm("server") {
        withJava()

        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    js("web", IR) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                devServer?.port = 8081
                cssSupport {
                    enabled = true
                    mode = "extract"
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Dependency Injection
                implementation("org.kodein.di:kodein-di:$kodeinVersion")
                implementation("org.kodein.di:kodein-di-conf:$kodeinVersion")

                // Logging
                implementation("io.github.microutils:kotlin-logging:3.0.2")

                // Coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                // Cache
                implementation("io.github.reactivecircus.cache4k:cache4k:0.11.0")

                // Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("com.ionspin.kotlin:bignum-serialization-kotlinx:0.3.8")

                // Big integers
                implementation("com.ionspin.kotlin:bignum:0.3.7")

                // Time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

                // Ktor
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-resources:$ktorVersion")
            }
        }

        val serverMain by getting {
            val exposedVersion = "0.40.1"
            val ktorVersion = "2.1.2"

            dependencies {
                dependsOn(commonMain)

                implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodeinVersion")
                implementation("org.kodein.di:kodein-di-framework-ktor-server-controller-jvm:$kodeinVersion")

                runtimeOnly("org.slf4j:slf4j-simple:2.0.3")

                // Ton
                implementation("org.ton:ton-kotlin:0.1.1")

                // Database access
                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
                runtimeOnly("org.postgresql:postgresql:42.5.0")

                // Ktor
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-host-common:$ktorVersion")
                implementation("io.ktor:ktor-server-partial-content:$ktorVersion")
                implementation("io.ktor:ktor-server-resources:$ktorVersion")
                implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
                implementation("io.ktor:ktor-server-call-id:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-cors:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
            }
        }

        val webMain by getting {
            dependencies {
                dependsOn(commonMain)

                // Framework
                implementation("dev.fritz2:core:1.0-RC1")

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.399")

                // Tailwindcss and loaders
                implementation(npm("tailwindcss", "3.1.8"))
                implementation(devNpm("postcss", "^8.4.17"))
                implementation(devNpm("postcss-loader", "7.0.1"))
                implementation(devNpm("autoprefixer", "10.4.12"))

                // Ton connect
                implementation(npm("@tonconnect/sdk", "0.0.42"))
            }
        }
    }
}

tasks.getByName<Jar>("serverJar") {
    val taskName = if (!project.ext.has("development")
        || project.gradle.startParameter.taskNames.contains("installDist")
    ) {
        "webBrowserProductionWebpack"
    } else {
        "webBrowserDevelopmentWebpack"
    }
    val webpackTask = tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>(taskName)
    dependsOn(webpackTask)
    from(files(webpackTask.destinationDirectory))
}

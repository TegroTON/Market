kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines)
                implementation(libs.datetime)
                implementation(libs.exposed.core)
                implementation(libs.exposed.dao)
                implementation(libs.exposed.kotlin.datetime)
                implementation(libs.logback)
                implementation(libs.logging)
                implementation(libs.serialization.core)
                implementation(libs.serialization.json)
                implementation(libs.slf4j)
                implementation(libs.ton)
            }
        }
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.coroutines)
                implementation(libs.ipfs)
                implementation(libs.koin)
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

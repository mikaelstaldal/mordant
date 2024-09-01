plugins {
    kotlin("multiplatform")
    id("mordant-native-core-conventions")
}

kotlin {
    // Add targets not supported by the markdown library
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm64()
    watchosX64()
}

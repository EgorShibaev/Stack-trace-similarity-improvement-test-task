plugins {
    id("java-conventions")
}

group = "org.csc.java"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(libs.junit.jupyter.api)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.jupyter.engine)
}

/*
* MIT License
*
* Copyright (c) 2018 Miguel Hernández
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
import org.jetbrains.kotlin.ir.backend.js.compile

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
}

plugins {
    base
    java
    kotlin("jvm") version "1.2.51"
    `maven-publish`

    id("com.diffplug.gradle.spotless") version "3.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.0.0.RC8"
}

subprojects {

    group = "com.ukbar"

    version = "1.0"

    apply {
        plugin("kotlin")
        plugin("com.diffplug.gradle.spotless")
        plugin("org.gradle.maven-publish")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    spotless {
        kotlin {
            ktlint()

            licenseHeaderFile(rootProject.file("LICENSE"))
        }

        kotlinGradle {
            ktlint()

            licenseHeaderFile(rootProject.file("LICENSE"),
                    "(import|rootProject|dependencies|plugins|apply|include)")
        }
    }

    dependencies {
        compile(kotlin("stdlib-jdk8"))
    }

    val sourcesJar by tasks.creating(Jar::class) {
        classifier = "sources"
        from(java.sourceSets["main"].allSource)
    }

    publishing {

        repositories {
            maven {
            }
        }

        (publications) {
            "mavenJava"(MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar)
            }
        }
    }
}

detekt {
    defaultProfile(Action {
        input = "$projectDir"
        output = "$buildDir/reports/detekt"
        config = "$projectDir/detekt.yml"
        filters = ".*/resources/.*,.*/build/.*"
    })
}
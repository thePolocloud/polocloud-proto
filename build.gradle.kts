import com.google.protobuf.gradle.*

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.5"
    id("com.gradleup.shadow") version "9.0.0"

    alias(libs.plugins.nexus.publish)
    `maven-publish`
}

group = "dev.httpmarco.polocloud"
version = "3.0.0-pre.7-SNAPSHOT"


val grpcVersion = "1.77.0"
val protobufVersion = "4.33.1"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-java:${protobufVersion}")
    api("com.google.protobuf:protobuf-java-util:${protobufVersion}")
    api("io.grpc:grpc-netty:${grpcVersion}")
    api("io.grpc:grpc-stub:${grpcVersion}")
    api("io.grpc:grpc-protobuf:${grpcVersion}")

    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
    options.encoding = "UTF-8"
}

protobuf {
    protoc {
        // dont update this version
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {}
            }
        }
    }
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    enabled = false
}

tasks.shadowJar {
    archiveFileName.set("polocloud-proto-$version.jar")
}

artifacts {
    archives(tasks.shadowJar)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withSourcesJar()
}

tasks {
    withType<Jar> { duplicatesStrategy = DuplicatesStrategy.EXCLUDE }
    test {
        useJUnitPlatform()
    }
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }

        proto {
            srcDir("src/proto")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar.get()) {
                classifier = null
            }

            pom {
                name.set(project.name)
                url.set("https://github.com/thePolocloud/polocloud")
                description.set("PoloCloud is the simplest and easiest Cloud for Minecraft")
                licenses {
                    license {
                        name.set("Apache License")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        name.set("Mirco Lindenau")
                        email.set("mirco.lindenau@gmx.de")
                    }
                }
                scm {
                    url.set("https://github.com/thePolocloud/polocloud")
                    connection.set("https://github.com/httpmarco/polocloud.git")
                }
            }
        }
    }
}

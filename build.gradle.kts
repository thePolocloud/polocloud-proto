import com.google.protobuf.gradle.*

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.6"
    id("com.gradleup.shadow") version "9.0.0"
    alias(libs.plugins.nexus.publish)

    `maven-publish`
}

group = "dev.httpmarco.polocloud"
version = "3.0.0-pre.8-SNAPSHOT"

val grpcVersion = "1.78.0"
val protobufVersion = "4.33.3"

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.protobuf:protobuf-java:$protobufVersion")
    api("io.grpc:grpc-stub:$grpcVersion")
    api("io.grpc:grpc-protobuf:$grpcVersion")

    // Compile only annotation API
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.encoding = "UTF-8"
}

sourceSets {
    main {
        proto {
            srcDir("src/proto")
        }

        java {
            srcDir("${buildDir}/generated/source/proto/main/java")
            srcDir("${buildDir}/generated/source/proto/main/grpc")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set(null)
    mergeServiceFiles()
}


tasks.publish {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {

            artifact(tasks.shadowJar.get())

            pom {
                name.set("polocloud-proto")
                description.set("PoloCloud gRPC API with bundled dependencies")
                url.set("https://github.com/thePolocloud/polocloud")

                licenses {
                    license {
                        name.set("Apache License 2.0")
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
                    connection.set("scm:git:https://github.com/thePolocloud/polocloud.git")
                    developerConnection.set("scm:git:https://github.com/thePolocloud/polocloud.git")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/releases/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(System.getenv("ossrhUsername") ?: "")
            password.set(System.getenv("ossrhPassword") ?: "")
        }
    }
    // todo find a better way to determine if we are in a staging or release build
    useStaging.set(!project.rootProject.version.toString().endsWith("-SNAPSHOT"))
}
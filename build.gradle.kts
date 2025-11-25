import com.google.protobuf.gradle.*

plugins {
    id("java-library")
    id("com.google.protobuf") version "0.9.5"
}

group = "dev.httpmarco.polocloud.proto"
version = "3.0.0-pre.7-SNAPSHOT"


val grpcVersion = "1.77.0"
val protobufVersion = "4.28.0"

repositories {
    mavenCentral()
}

dependencies {
    api("io.grpc:grpc-netty:${grpcVersion}")
    api("io.grpc:grpc-stub:${grpcVersion}")
    api("io.grpc:grpc-protobuf:${grpcVersion}")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_21.toString()
    targetCompatibility = JavaVersion.VERSION_21.toString()
    options.encoding = "UTF-8"
}

protobuf {
    protoc {
        // dont update this version
        artifact = "com.google.protobuf:protoc:3.25.8"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.77.0"
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
    archiveFileName.set("polocloud-proto-$version.jar")
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

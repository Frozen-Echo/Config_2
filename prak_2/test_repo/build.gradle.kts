plugins {
    java
}

group = "com.kweezy"
version = "1.2"

repositories {
    mavenCentral()
}


dependencies {
    compileOnly(files("lib/spigot152.jar"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
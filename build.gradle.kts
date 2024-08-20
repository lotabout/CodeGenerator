plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "me.lotabout"
version = "1.7.0"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    create("IC", "2023.2")
    bundledPlugin("com.intellij.java")
    pluginVerifier()
    zipSigner()
    instrumentationTools()
  }
  implementation("org.apache.commons:commons-lang3:3.12.0")
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("999.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

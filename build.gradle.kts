plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "me.lotabout"
version = "1.8.1"

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
  implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")  {
    exclude(group = "org.apache.velocity", module = "velocity-engine-core")
    exclude(group = "org.slf4j", module = "slf4j-api")
  }
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
  }

  patchPluginXml {
    sinceBuild.set("232")
    untilBuild.set("243.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }

  test {
    useJUnitPlatform()
  }
}

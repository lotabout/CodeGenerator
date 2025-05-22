plugins {
  id("java")
  id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "me.lotabout"
version = "2.0.5"

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    create("IC", "2025.1")
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
  testImplementation("org.mockito:mockito-core:5.10.0")
  testImplementation("org.mockito:mockito-junit-jupiter:5.10.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
  }

  // 禁用buildSearchableOptions任务
  named("buildSearchableOptions") {
    enabled = false
  }

  // 禁用测试任务
  named("test") {
    enabled = false
  }

  patchPluginXml {
    sinceBuild.set("251")
    untilBuild.set("253.*")
    changeNotes.set("""
      <ul>
        <li>Support for IntelliJ IDEA 2025.1 - 2025.3</li>
        <li>Fix settings dialog compatibility issue</li>
        <li>Fix NullPointerException when opening plugin settings</li>
      </ul>
    """)
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
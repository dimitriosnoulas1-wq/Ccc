plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  // AI Studio / fresh clones may only have the base SDK 36 image, not 36.1.
  compileSdk = 36

  defaultConfig {
    applicationId = "com.aistudio.cryptocycles.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 134
    versionName = "1.134.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    val hubUrl = (System.getenv("MARKET_HUB_URL") ?: "").replace("\"", "")
    buildConfigField("String", "MARKET_HUB_URL", "\"$hubUrl\"")
    // Maps Cloud/AI Studio secrets (GEMINI_API_KEY or Gemini) into the APK without committing .env.
    val injectedGeminiKey = (System.getenv("GEMINI_API_KEY") ?: System.getenv("Gemini") ?: "")
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "")
      .replace("\r", "")
    buildConfigField("String", "GEMINI_INJECTED_API_KEY", "\"$injectedGeminiKey\"")
    val injectedOpenAiKey = (
      System.getenv("OPENAI_API_KEY")
        ?: System.getenv("OPENAI")
        ?: System.getenv("ChatGPT")
        ?: System.getenv("gpt")
        ?: System.getenv("GPT")
        ?: ""
      )
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "")
      .replace("\r", "")
    buildConfigField("String", "OPENAI_INJECTED_API_KEY", "\"$injectedOpenAiKey\"")
  }

  signingConfigs {
    create("release") {
      val uploadKey = file("${rootDir}/my-upload-key.jks")
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      val targetFile = if (uploadKey.exists()) uploadKey else file(keystorePath)
      // Do not fail configuration when the upload key is missing (AI Studio debug preview).
      if (targetFile.exists()) {
        storeFile = targetFile
        storePassword = System.getenv("STORE_PASSWORD") ?: "android"
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      val releaseStore = signingConfigs.getByName("release").storeFile
      if (releaseStore != null && releaseStore.exists()) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    // Default Android debug keystore — debug.keystore is gitignored and missing in AI Studio.
    debug { }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      isReturnDefaultValues = true
    }
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
  lint {
    checkReleaseBuilds = false
    abortOnError = false
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.billing.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.coil.gif)
  implementation(libs.converter.moshi)
  // Firebase is unused: Gemini talks to the REST API, and there is no google-services.json.
  // Shipping firebase-ai / App Check still auto-inits and crashes the AI Studio emulator.
  implementation("androidx.browser:browser:1.8.0")
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

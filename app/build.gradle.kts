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
    versionCode = 142
    versionName = "1.142.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    val hubUrl = (System.getenv("MARKET_HUB_URL") ?: "").replace("\"", "")
    buildConfigField("String", "MARKET_HUB_URL", "\"$hubUrl\"")
    // AI keys are never built into the APK: they live on the hub (see hub/README.md).
  }

  signingConfigs {
    create("release") {
      // The keystore is never committed; it comes from KEYSTORE_PATH or a local, gitignored file.
      val targetFile = file(System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks")
      val storePass = System.getenv("STORE_PASSWORD").orEmpty()
      val keyPass = System.getenv("KEY_PASSWORD").orEmpty()
      // Without key + passwords the release stays unsigned; debug builds (AI Studio preview) are unaffected.
      if (targetFile.exists() && storePass.isNotEmpty() && keyPass.isNotEmpty()) {
        storeFile = targetFile
        storePassword = storePass
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = keyPass
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
  // Never turn AI keys from .env into BuildConfig fields.
  ignoreList.add("(?i).*(gemini|openai|chatgpt|gpt).*")
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

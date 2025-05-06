plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlinKSP)
	alias(libs.plugins.kotlin.compose)
	`maven-publish`
}

group = "com.github.appoly"

android {
	namespace = "uk.co.appoly.droid.appsnackbar"
	compileSdk = libs.versions.compileSdk.get().toInt()

	publishing {
		singleVariant("release") {
			withSourcesJar()
		}
	}

	defaultConfig {
		minSdk = libs.versions.appSnackBarMinSdk.get().toInt()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
	buildFeatures {
		compose = true
	}
}

dependencies {

	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)

	api(project(":AppSnackBar"))
	api(project(":UiState"))

	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}

publishing {
	publications {
		create<MavenPublication>("release") {
			afterEvaluate {
				from(components["release"])
			}
			groupId = "com.github.appoly"
			artifactId = project.name
			version = libs.versions.toolboxVersion.get()
		}
	}
}
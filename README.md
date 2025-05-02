# AppolyDroid

Appoly's Android startup toolbox

[![Release](https://jitpack.io/v/appoly/AppolyDroid-Toolbox.svg)](https://jitpack.io/#appoly/AppolyDroid-Toolbox)

https://jitpack.io/#appoly/AppolyDroid-Toolbox

see https://jitpack.io/private#auth for information on how to set up authentication

Add it to your `build.gradle.kts` with:
```gradle.kts
dependencyResolutionManagement {
	repositories {
		...
		maven {
			url = uri("https://jitpack.io")
			credentials.username = providers.gradleProperty("authToken").get()
		}
	}
}
```
or in your `settings.gradle` with:
```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
        credentials { username authToken }
    }
}
```

and: in your module with version catalog:
```toml
[versions]
appolydroidToolbox = "Tag"

[libraries]
#AppolyDroid-Toolbox
appolydroid-toolbox-baseRepo = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "BaseRepo", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-baseRepo-s3 = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "BaseRepo-S3Uploader", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-baseRepo-paging = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "BaseRepo-Paging", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "UiState", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-DateHelper = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "DateHelperUtil", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-DateHelper-Room = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "DateHelperUtil-Room", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-DateHelper-Serialization = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "DateHelperUtil-Serialization", version.ref = "appolydroidToolbox" }
```
```gradle.kts
dependencies {
    implementation(libs.appolydroid.toolbox.baseRepo)
    implementation(libs.appolydroid.toolbox.baseRepo.s3)
    implementation(libs.appolydroid.toolbox.baseRepo.paging)
    implementation(libs.appolydroid.toolbox.uiState)
    implementation(libs.appolydroid.toolbox.DateHelper)
    implementation(libs.appolydroid.toolbox.DateHelper.room)
    implementation(libs.appolydroid.toolbox.DateHelper.serialization)
}
```
or just gradle.kts as:
```gradle.kts
dependencies {
    implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-S3Uploader:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-Paging:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:UiState:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Room:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Serialization:Tag")
}
```
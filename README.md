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

`[versions]`
```toml
appolydroidToolbox = "Tag"
```

`[libraries]`
```toml
#AppolyDroid-Toolbox
appolydroid-toolbox-baseRepo = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "BaseRepo", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-baseRepo-s3 = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "BaseRepo-S3Uploader", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-baseRepo-paging = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "BaseRepo-Paging", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-dateHelper = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "DateHelperUtil", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-dateHelper-room = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "DateHelperUtil-Room", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-dateHelper-serialization = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "DateHelperUtil-Serialization", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "UiState", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-appSnackBar = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "AppSnackBar", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-appSnackBar-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "AppSnackBar-UiState", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-lazyListPagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "LazyListPagingExtensions", version.ref = "appolydroidToolbox" }
appolydroid-toolbox-lazyGridPagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "LazyGridPagingExtensions", version.ref = "appolydroidToolbox" }
```

`build.gradle.kts`
```gradle.kts
dependencies {
    //AppolyDroid-Toolbox
    implementation(libs.appolydroid.toolbox.baseRepo)
    implementation(libs.appolydroid.toolbox.baseRepo.s3)
    implementation(libs.appolydroid.toolbox.baseRepo.paging)
    implementation(libs.appolydroid.toolbox.dateHelper)
    implementation(libs.appolydroid.toolbox.dateHelper.room)
    implementation(libs.appolydroid.toolbox.dateHelper.serialization)
    implementation(libs.appolydroid.toolbox.uiState)
    implementation(libs.appolydroid.toolbox.appSnackBar)
    implementation(libs.appolydroid.toolbox.appSnackBar.uiState)
    implementation(libs.appolydroid.toolbox.lazyListPagingExtensions)
    implementation(libs.appolydroid.toolbox.lazyGridPagingExtensions)
}
```

or without version catalog:

`build.gradle.kts`
```gradle.kts
dependencies {
    //AppolyDroid-Toolbox
    implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-S3Uploader:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-Paging:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Room:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Serialization:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:UiState:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar-UiState:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:LazyListPagingExtensions:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:LazyGridPagingExtensions:Tag")
}
```
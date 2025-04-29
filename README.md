# AppolyDroid

Appoly's Android startup toolbox

[![Release](https://jitpack.io/v/appoly/AppolyDroid-Toolbox.svg)](https://jitpack.io/#appoly/AppolyDroid-Toolbox)

https://jitpack.io/#appoly/AppolyDroid-Toolbox

see https://jitpack.io/private#auth for information on how to set up authentication

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
        credentials { username authToken }
    }
}
```
or in your `settings.gradle.kts` with:
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
and: in your module with version catalog:
```toml
    [versions]
    appolydroidToolbox = "Tag"

    [libraries]
    #AppolyDroid-Toolbox
    appolydroid-toolbox-AppolyDroidBaseRepo = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "AppolyDroidBaseRepo", version.ref = "appolydroidToolbox" }
    appolydroid-toolbox-AppolyDroidBaseRepoS3 = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "AppolyDroidBaseRepoS3", version.ref = "appolydroidToolbox" }
    appolydroid-toolbox-AppolyDroidBaseRepoPaging = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "AppolyDroidBaseRepoPaging", version.ref = "appolydroidToolbox" }
```
```gradle.kts
    dependencies {
        implementation(libs.appolydroid.toolbox.AppolyDroidBaseRepo)
        implementation(libs.appolydroid.toolbox.AppolyDroidBaseRepoS3)
        implementation(libs.appolydroid.toolbox.AppolyDroidBaseRepoPaging)
    }
```
or just gradle.kts as:
```gradle.kts
dependencies {
    implementation("com.github.appoly.AppolyDroid-Toolbox:AppolyDroidBaseRepo:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:AppolyDroidBaseRepoS3:Tag")
    implementation("com.github.appoly.AppolyDroid-Toolbox:AppolyDroidBaseRepoPaging:Tag")
}
```
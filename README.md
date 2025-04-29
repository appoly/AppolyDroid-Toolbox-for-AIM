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
and:

```gradle
dependencies {
    implementation("com.github.appoly:AppolyDroid-Toolbox:AppolyDroidBaseRepo:Tag")
    implementation("com.github.appoly:AppolyDroid-Toolbox:AppolyDroidBaseRepoS3:Tag")
    implementation("com.github.appoly:AppolyDroid-Toolbox:AppolyDroidBaseRepoPaging:Tag")
}
```

# AppolyDroid

Appoly's Android startup toolbox

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
    implementation("TODO")
}
```

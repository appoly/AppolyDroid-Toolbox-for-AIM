# Android-S3ImageUploader
Aws S3 Image Uploader module for use in Android Apps

Available via JitPAck [![Release](https://jitpack.io/v/appoly/AppolyDroid-Toolbox.svg)](https://jitpack.io/#appoly/AppolyDroid-Toolbox)

https://jitpack.io/#appoly/AppolyDroid-Toolbox

## Setup
Add the JitPack Maven repository to your build file

Add it to your `build.gradle.kts` with:
```gradle.kts
dependencyResolutionManagement {
	repositories {
		...
		maven {
			url = uri("https://jitpack.io")
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
    }
}
```

and: in your module with version catalog:

```toml
[versions]
appolydroidToolbox = "Tag"

[libraries]
#AppolyDroid-Toolbox
appolydroid-toolbox-s3Uploader = { group = "com.github.appoly.AppolyDroid-Toolbox", name = "S3Uploader", version.ref = "appolydroidToolbox" }
```

`build.gradle.kts`
```gradle.kts
dependencies {
    //AppolyDroid-Toolbox
    implementation(libs.appolydroid.toolbox.s3Uploader)
}
```

or without version catalog:

`build.gradle.kts`
```gradle.kts
dependencies {
    //AppolyDroid-Toolbox
    implementation("com.github.appoly.AppolyDroid-Toolbox:S3Uploader:Tag")
}
```


## Usage
### Initialisation
Initialise the module in your Application class
```kotlin
fun onCreate() {
    super.onCreate()

    S3Uploader.initS3Uploader(
        tokenProvider = { app_user_authToken }, // <- Provide a function that returns your auth token
        loggingLevel = if(isDebug) { LoggingLevel.V } else { LoggingLevel.W }, // <- Optional set your desired logging level
		logger = Log // <- Optional implementation of your App's [FlexiLogger](https://github.com/projectdelta6/FlexiLogger) Log class
    )
}
```

Upload your image
```kotlin
val result: UploadResult = try {
    S3Uploader.uploadFile(
        file = your_image_file, // File, the file to be uploaded
        getPresignedUrlAPI = your_API_endpoint, // String, the API endpoint that returns the presigned url data to upload onto
        progressFlow = you_progress_flow // (Optional) MutableStateFlow<Float>, the flow that will receive the upload progress
    )
} catch(e: Exception) {
    UploadResult.Error("Unknown Error", e)
}
when(result) {
    is UploadResult.Success -> {
        result.filePath // <- The file path of the uploaded image
        // Handle success
    }
    is UploadResult.Failure -> {
        result.message // <- The error message
        result.throwable // <- The exception (nullable)
        // Handle failure
    }
}
```

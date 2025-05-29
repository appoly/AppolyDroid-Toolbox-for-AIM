# BaseRepo-S3Uploader

An extension module for BaseRepo that adds Amazon S3 file upload capabilities.

## Features

- Seamless integration with the BaseRepo module
- Direct file uploads to Amazon S3
- Progress tracking for uploads
- Pre-signed URL generation support
- Error handling and retry mechanisms

## Installation

```gradle.kts
// Requires the base BaseRepo module
implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo:1.0.12")
implementation("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-S3Uploader:1.0.12")
```

## Usage

### Uploading a File to S3

```kotlin
class DataRepo : BaseRepo() {
    suspend fun uploadProfileImage(imageFile: File): APIResult<String> {
        return uploadFileToS3(
            generatePresignedURL = RetrofitClient.getGeneratePresignedURL(),
            file = imageFile
        )
    }
}
```

### Uploading with Progress Tracking

```kotlin
val progressFlow = MutableStateFlow(0f)

suspend fun uploadVideo(videoFile: File): APIResult<String> {
    return uploadFileToS3(
        generatePresignedURL = RetrofitClient.getGeneratePresignedURL(),
        file = videoFile,
        progressMutableFlow = progressFlow
    )
}

// In your UI:
val uploadProgress by progressFlow.collectAsState()
LinearProgressIndicator(progress = { uploadProgress / 100f })
```

### Using the Upload Result

```kotlin
suspend fun uploadAndProcessImage(imageFile: File) {
    when (val result = uploadFileToS3(
        generatePresignedURL = RetrofitClient.getGeneratePresignedURL(),
        file = imageFile
    )) {
        is APIResult.Success -> {
            val fileUrl = result.data
            // Process the successful upload
            updateUserProfile(imageUrl = fileUrl)
        }
        is APIResult.Error -> {
            // Handle the error
            Log.e("Upload", "Failed to upload: ${result.message}")
        }
    }
}
```

### Using with Repository Methods

```kotlin
suspend fun createUserWithProfilePic(name: String, imageFile: File?): APIResult<UserData> {
    // Upload profile image if provided
    val profileImageUrl = imageFile?.let { file ->
        uploadFileToS3(
            generatePresignedURL = RetrofitClient.getGeneratePresignedURL(),
            file = file
        ).let { result ->
            result.successOrNull() ?: return APIResult.Error(result as APIResult.Error)
        }
    }
    
    // Create user with profile image URL
    return doAPICall("createUser") {
        userService.api.createUser(
            UserCreateBody(
                name = name,
                profileImageUrl = profileImageUrl
            )
        )
    }
}
```

## API Reference

### Main Function

```kotlin
suspend fun uploadFileToS3(
    generatePresignedURL: String,
    file: File,
    progressMutableFlow: MutableStateFlow<Float>? = null
): APIResult<String>
```

#### Parameters

- `generatePresignedURL`: API endpoint for generating a presigned S3 URL
- `file`: The File object to upload
- `progressMutableFlow`: Optional MutableStateFlow to track upload progress (0-100)

#### Returns

- `APIResult<String>`: On success, returns the URL of the uploaded file. On failure, returns an error.

## Dependencies

- [BaseRepo](../BaseRepo/README.md) module
- [FlexiLogger](https://github.com/projectdelta6/FlexiLogger) for logging
- OkHttp for HTTP requests

## Notes

- The S3 upload functionality requires your backend to provide an endpoint for generating pre-signed URLs.
- For standalone S3 upload functionality without BaseRepo, see the [S3Uploader](../S3Uploader/README.md) module.

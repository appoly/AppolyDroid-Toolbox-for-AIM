# AppolyDroid Toolbox (For AIM)

This is a client/project specific fork of the original [AppolyDroid-Toolbox](https://github.com/appoly/AppolyDroid-Toolbox) repository, tailored to handle the specific json response structure used by
AIM's APIs.

Appoly's Android development toolbox - a collection of utilities and components to accelerate Android app development

[![](https://jitpack.io/v/appoly/AppolyDroid-Toolbox-for-AIM.svg)](https://jitpack.io/#appoly/AppolyDroid-Toolbox-for-AIM)

## Overview

AppolyDroid Toolbox is a comprehensive collection of Android utility modules that simplify common development tasks. The library provides ready-to-use solutions for:

- API data handling with `BaseRepo`
- AWS S3 file uploading
- Date/time operations
- UI state management
- Snackbar notifications
- Jetpack Compose pagination utilities
- And more!

## Installation

Add the JitPack repository to your project build file:

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

### Using the BOM (Bill of Materials)

For easier dependency management, you can use the AppolyDroid BOM which provides version alignment for all modules and their shared dependencies:

#### Using Version Catalog

In your `libs.versions.toml` file:

```toml
[versions]
appolydroidToolbox_AIM = "1.0.34" # Replace with the latest version

[libraries]
appolydroid-toolbox-bom = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "AppolyDroid-Toolbox-bom", version.ref = "appolydroidToolbox_AIM" }
# AppolyDroid modules (versions managed by BOM)
appolydroid-toolbox-baseRepo = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "BaseRepo" }
appolydroid-toolbox-baseRepo-s3 = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "BaseRepo-S3Uploader" }
appolydroid-toolbox-baseRepo-paging = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "BaseRepo-Paging" }
appolydroid-toolbox-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "UiState" }
appolydroid-toolbox-appSnackBar = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "AppSnackBar" }
appolydroid-toolbox-appSnackBar-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "AppSnackBar-UiState" }
appolydroid-toolbox-dateHelper = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "DateHelperUtil" }
appolydroid-toolbox-dateHelper-room = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "DateHelperUtil-Room" }
appolydroid-toolbox-dateHelper-serialization = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "DateHelperUtil-Serialization" }
appolydroid-toolbox-compose-extensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "ComposeExtensions" }
appolydroid-toolbox-lazyListPagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "LazyListPagingExtensions" }
appolydroid-toolbox-lazyGridPagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "LazyGridPagingExtensions" }
appolydroid-toolbox-pagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "PagingExtensions" }
appolydroid-toolbox-s3Uploader = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "S3Uploader" }
```

Then in your module's `build.gradle.kts`:

```gradle.kts
dependencies {
    // Import the BOM
    implementation(platform(libs.appolydroid.toolbox.bom))

    // Now you can use AppolyDroid modules without specifying versions
    implementation(libs.appolydroid.toolbox.baseRepo)
    implementation(libs.appolydroid.toolbox.baseRepo.s3)
    implementation(libs.appolydroid.toolbox.baseRepo.paging)
    implementation(libs.appolydroid.toolbox.uiState)
    implementation(libs.appolydroid.toolbox.appSnackBar)
    implementation(libs.appolydroid.toolbox.appSnackBar.uiState)
    implementation(libs.appolydroid.toolbox.dateHelper)
    implementation(libs.appolydroid.toolbox.dateHelper.room)
    implementation(libs.appolydroid.toolbox.dateHelper.serialization)
    implementation(libs.appolydroid.toolbox.compose.extensions)
    implementation(libs.appolydroid.toolbox.lazyListPagingExtensions)
    implementation(libs.appolydroid.toolbox.lazyGridPagingExtensions)
    implementation(libs.appolydroid.toolbox.pagingExtensions)
    implementation(libs.appolydroid.toolbox.s3Uploader)
}
```

#### Without Version Catalog (BOM)

In your module's `build.gradle.kts`:

```gradle.kts
dependencies {
    // Import the BOM
    implementation(platform("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppolyDroid-Toolbox-bom:1.0.34"))

    // Now you can use AppolyDroid modules without specifying versions
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-S3Uploader")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-Paging")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:UiState")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppSnackBar")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppSnackBar-UiState")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil-Room")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil-Serialization")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:ComposeExtensions")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyListPagingExtensions")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyGridPagingExtensions")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:PagingExtensions")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:S3Uploader")
}
```

**Benefits of using the BOM:**

- Ensures all AppolyDroid modules use compatible versions
- Provides version management for shared 3rd party dependencies
- Simplifies dependency declarations
- Reduces version conflicts

### Individual Module Installation

In your `libs.versions.toml` file:

```toml
[versions]
appolydroidToolbox = "1.0.33" # Replace with the latest version

[libraries]
#AppolyDroid-Toolbox-for-AIM
appolydroid-toolbox-baseRepo = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "BaseRepo", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-baseRepo-s3 = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "BaseRepo-S3Uploader", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-baseRepo-paging = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "BaseRepo-Paging", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-dateHelper = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "DateHelperUtil", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-dateHelper-room = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "DateHelperUtil-Room", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-dateHelper-serialization = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "DateHelperUtil-Serialization", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "UiState", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-appSnackBar = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "AppSnackBar", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-appSnackBar-uiState = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "AppSnackBar-UiState", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-lazyListPagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "LazyListPagingExtensions", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-lazyGridPagingExtensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "LazyGridPagingExtensions", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-s3Uploader = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "S3Uploader", version.ref = "appolydroidToolbox_AIM" }
appolydroid-toolbox-compose-extensions = { group = "com.github.appoly.AppolyDroid-Toolbox-for-AIM", name = "ComposeExtensions", version.ref = "appolydroidToolbox_AIM" }
```

Then in your module's `build.gradle.kts`:

```gradle.kts
dependencies {
    // Add only the modules you need
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
    implementation(libs.appolydroid.toolbox.s3Uploader)
    implementation(libs.appolydroid.toolbox.compose.extensions)
}
```
### Without Version Catalog

In your module's `build.gradle.kts`:

```gradle.kts
dependencies {
    // Add only the modules you need
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-S3Uploader:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-Paging:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil-Room:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil-Serialization:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:UiState:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppSnackBar:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppSnackBar-UiState:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyListPagingExtensions:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyGridPagingExtensions:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:S3Uploader:1.0.32_rc01_bom")
    implementation("com.github.appoly.AppolyDroid-Toolbox-for-AIM:ComposeExtensions:1.0.32_rc01_bom")
}
```

## Modules
### BaseRepo
Foundation for repository pattern implementation with API call handling.
[Learn more](BaseRepo/README.md)
### BaseRepo-S3Uploader
Extension to BaseRepo adding S3 upload capabilities.
[Learn more](BaseRepo-S3Uploader/README.md)
### BaseRepo-Paging
Extends BaseRepo with Jetpack Paging capabilities.
[Learn more](BaseRepo-Paging/README.md)
### DateHelperUtil
Utilities for date and time operations.
[Learn more](DateHelperUtil/README.md)
### DateHelperUtil-Room
Room database integration for DateHelperUtil.
[Learn more](DateHelperUtil-Room/README.md)
### DateHelperUtil-Serialization
Kotlinx Serialization support for DateHelperUtil.
[Learn more](DateHelperUtil-Serialization/README.md)
### UiState
Simplified UI state management.
[Learn more](UiState/README.md)
### AppSnackBar
Enhanced Snackbar implementation.
[Learn more](AppSnackBar/README.md)
### AppSnackBar-UiState
Integration of AppSnackBar with UiState.
[Learn more](AppSnackBar-UiState/README.md)
### LazyListPagingExtensions
Extensions for Jetpack Compose LazyList with paging support.
[Learn more](LazyListPagingExtensions/README.md)
### LazyGridPagingExtensions
Extensions for Jetpack Compose LazyGrid with paging support.
[Learn more](LazyGridPagingExtensions/README.md)
### S3Uploader
Standalone S3 file upload utility.
[Learn more](S3Uploader/README.md)

## Dependencies

Some modules depend on [FlexiLogger](https://github.com/projectdelta6/FlexiLogger) for logging capabilities.

## License

```text
MIT License

Copyright (c) 2025 Appoly Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


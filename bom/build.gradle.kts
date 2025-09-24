plugins {
	`java-platform`
	`maven-publish`
}

group = "com.github.appoly"

javaPlatform {
	allowDependencies()
}

dependencies {
	// Define constraints for all AppolyDroid modules
	constraints {
		// Core modules
		api("com.github.appoly.AppolyDroid-Toolbox:BaseRepo:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-S3Uploader:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:BaseRepo-Paging:${libs.versions.toolboxVersion.get()}")

		// UI State modules
		api("com.github.appoly.AppolyDroid-Toolbox:UiState:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:AppSnackBar-UiState:${libs.versions.toolboxVersion.get()}")

		// Date/Time modules
		api("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Room:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:DateHelperUtil-Serialization:${libs.versions.toolboxVersion.get()}")

		// Compose & Pagination modules
		api("com.github.appoly.AppolyDroid-Toolbox:ComposeExtensions:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:LazyListPagingExtensions:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:LazyGridPagingExtensions:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox:PagingExtensions:${libs.versions.toolboxVersion.get()}")

		// S3 & Utility modules
		api("com.github.appoly.AppolyDroid-Toolbox:S3Uploader:${libs.versions.toolboxVersion.get()}")

		// Shared 3rd party dependencies (commonly used via api() in modules)
		// Room (used in DateHelperUtil-Room)
		api("androidx.room:room-runtime:${libs.versions.roomVersion.get()}")
		api("androidx.room:room-ktx:${libs.versions.roomVersion.get()}")

		// Paging (used in BaseRepo-Paging)
		api("androidx.paging:paging-runtime:${libs.versions.paging.get()}")

		// Serialization (used in BaseRepo, DateHelperUtil-Serialization, S3Uploader)
		api("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinxSerialization.get()}")

		// Sandwich (used in BaseRepo, S3Uploader)
		api("com.github.skydoves:sandwich:${libs.versions.sandwichVersion.get()}")
		api("com.github.skydoves:sandwich-retrofit:${libs.versions.sandwichVersion.get()}")

		// FlexiLogger (used in BaseRepo, DateHelperUtil, S3Uploader)
		api("com.github.projectdelta6:FlexiLogger:${libs.versions.flexiLoggerVersion.get()}")
		api("com.github.projectdelta6.FlexiLogger:FlexiHttpLogger:${libs.versions.flexiLoggerVersion.get()}")
	}
}

publishing {
	publications {
		create<MavenPublication>("bom") {
			from(components["javaPlatform"])
			groupId = "com.github.appoly"
			artifactId = "AppolyDroid-BOM"
			version = libs.versions.toolboxVersion.get()
		}
	}
}
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
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-S3Uploader:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:BaseRepo-Paging:${libs.versions.toolboxVersion.get()}")

		// UI State modules
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:UiState:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppSnackBar:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:AppSnackBar-UiState:${libs.versions.toolboxVersion.get()}")

		// Date/Time modules
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil-Room:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:DateHelperUtil-Serialization:${libs.versions.toolboxVersion.get()}")

		// Compose & Pagination modules
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:ComposeExtensions:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyListPagingExtensions:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:LazyGridPagingExtensions:${libs.versions.toolboxVersion.get()}")
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:PagingExtensions:${libs.versions.toolboxVersion.get()}")

		// S3 & Utility modules
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:S3Uploader:${libs.versions.toolboxVersion.get()}")

		// Connectivity Monitor
		api("com.github.appoly.AppolyDroid-Toolbox-for-AIM:ConnectivityMonitor:${libs.versions.toolboxVersion.get()}")
	}
}

publishing {
	publications {
		create<MavenPublication>("bom") {
			from(components["javaPlatform"])
			groupId = "com.github.appoly"
			artifactId = "AppolyDroid-Toolbox-bom"
			version = libs.versions.toolboxVersion.get()
		}
	}
}
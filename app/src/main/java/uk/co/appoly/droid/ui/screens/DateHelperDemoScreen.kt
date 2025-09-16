package uk.co.appoly.droid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import uk.co.appoly.droid.util.DateHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateHelperDemoScreen(navController: NavController) {
	var inputDateTime by remember { mutableStateOf("2025-09-16T14:30:00.000000Z") }
	var inputDate by remember { mutableStateOf("2025-09-16") }
	var parsedDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
	var parsedDate by remember { mutableStateOf<LocalDate?>(null) }
	var formattedDateTime by remember { mutableStateOf<String?>(null) }
	var formattedDate by remember { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("DateHelperUtil Demo") },
				navigationIcon = {
					IconButton(onClick = { navController.navigateUp() }) {
						Text("←")
					}
				}
			)
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = "DateHelperUtil Demo",
				style = MaterialTheme.typography.headlineMedium
			)

			Text(
				text = "Demonstrates date/time formatting, parsing, and utility functions",
				style = MaterialTheme.typography.bodyLarge
			)

			// Current time utilities
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Current Time Utilities",
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.height(8.dp))

					val currentTime = DateHelper.nowAsUTC()
					val currentLocal = LocalDateTime.now()

					Text("Current UTC: ${currentTime}")
					Text("Current Local: ${currentLocal}")
					Text("UTC as JSON: ${DateHelper.formatLocalDateTime(currentTime.toLocalDateTime())}")
					Text("Local as file: ${currentLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS"))}")
				}
			}

			// DateTime parsing and formatting
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "DateTime Parsing & Formatting",
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.height(8.dp))

					OutlinedTextField(
						value = inputDateTime,
						onValueChange = { inputDateTime = it },
						label = { Text("DateTime String") },
						modifier = Modifier.fillMaxWidth()
					)

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Button(
							onClick = {
								parsedDateTime = DateHelper.parseLocalDateTime(inputDateTime)
								formattedDateTime = DateHelper.formatLocalDateTime(parsedDateTime)
							},
							modifier = Modifier.weight(1f)
						) {
							Text("Parse & Format")
						}

						Button(
							onClick = {
								parsedDateTime = null
								formattedDateTime = null
							},
							modifier = Modifier.weight(1f)
						) {
							Text("Clear")
						}
					}

					if (parsedDateTime != null) {
						Text("Parsed: $parsedDateTime")
					}
					if (formattedDateTime != null) {
						Text("Formatted: $formattedDateTime")
					}
				}
			}

			// Date parsing and formatting
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Date Parsing & Formatting",
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.height(8.dp))

					OutlinedTextField(
						value = inputDate,
						onValueChange = { inputDate = it },
						label = { Text("Date String") },
						modifier = Modifier.fillMaxWidth()
					)

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Button(
							onClick = {
								parsedDate = DateHelper.parseLocalDate(inputDate)
								formattedDate = DateHelper.formatLocalDate(parsedDate)
							},
							modifier = Modifier.weight(1f)
						) {
							Text("Parse & Format")
						}

						Button(
							onClick = {
								parsedDate = null
								formattedDate = null
							},
							modifier = Modifier.weight(1f)
						) {
							Text("Clear")
						}
					}

					if (parsedDate != null) {
						Text("Parsed: $parsedDate")
					}
					if (formattedDate != null) {
						Text("Formatted: $formattedDate")
					}
				}
			}

			// Extension functions demo
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Direct Function Calls",
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.height(8.dp))

					val demoDateTime = LocalDateTime.now()
					val demoDate = LocalDate.now()

					Text("DateHelper.formatLocalDateTime(): ${DateHelper.formatLocalDateTime(demoDateTime)}")
					Text("DateTimeFormatter file format: ${demoDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS"))}")
					Text("DateHelper.formatLocalDate(): ${DateHelper.formatLocalDate(demoDate)}")

					Spacer(modifier = Modifier.height(8.dp))

					Text("DateHelper.parseLocalDateTime():")
					Text("DateHelper.parseLocalDateTime(\"2025-09-16T14:30:00.000000Z\") =")
					Text("${DateHelper.parseLocalDateTime("2025-09-16T14:30:00.000000Z")}")

					Text("DateHelper.parseLocalDate():")
					Text("DateHelper.parseLocalDate(\"2025-09-16\") =")
					Text("${DateHelper.parseLocalDate("2025-09-16")}")
				}
			}

			// Usage examples
			Card(modifier = Modifier.fillMaxWidth()) {
				Column(modifier = Modifier.padding(16.dp)) {
					Text(
						text = "Usage Examples",
						style = MaterialTheme.typography.titleMedium
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = """
                        // Basic parsing and formatting
                        val dateTime = DateHelper.parseLocalDateTime("2025-09-16T14:30:00.000000Z")
                        val jsonString = DateHelper.formatLocalDateTime(dateTime)
                        
                        // File-safe formatting
                        val fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS"))
                        
                        // Current time in UTC
                        val utcNow = DateHelper.nowAsUTC()
                        
                        // Direct function calls
                        val date = DateHelper.parseLocalDate("2025-09-16")
                        val formatted = DateHelper.formatLocalDate(date)
                        """.trimIndent(),
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.fillMaxWidth()
					)
				}
			}
		}
	}
}
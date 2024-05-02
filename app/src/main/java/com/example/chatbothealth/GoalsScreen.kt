package com.example.chatbothealth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatbothealth.ui.theme.AppTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var goalInput by remember { mutableStateOf("") }
    var daysInput by remember { mutableStateOf("") }
    var expandedGoal by remember { mutableStateOf(false) }
    var expandedDays by remember { mutableStateOf(false) }
    val goalsOptions = listOf("Walk 10,000 steps every day",
        "Run 5 kilometers three times a week",
        "Perform strength training four times a week",
        "Do 20 minutes of yoga every day",
        "Cycle 15 kilometers three times a week",
        "Drink at least 2 liters of water every day",
        "Consume at least 5 servings of fruits and vegetables every day",
        "Keep my daily calorie intake limited to 2000 calories",
        "Follow a completely vegan diet",
        "Meditate for 10 minutes every day",
        "Turn off all digital devices after 9 PM on weekdays",
        "Ensure to get 8 hours of sleep every night ",
        "Measure my blood sugar every day",
        "Check my blood pressure daily",
        "Take my prescribed medications on time every day")
    val daysOptions = List(12) { 30 + it * 30 }
    val totalDays = daysInput.toIntOrNull() ?: 1

    var goalsList by remember { mutableStateOf(listOf<Pair<String, Long>>()) }

    // Hedefleri veritabanından yükle
    LaunchedEffect(currentUser) {
        currentUser?.email?.let { email ->
            db.collection("users").document(email).collection("goals")
                .get()
                .addOnSuccessListener { documents ->
                    goalsList = documents.map { document ->
                        document.getString("goal") to document.getLong("daysRemaining")!!
                    } as List<Pair<String, Long>>
                }
        }
    }
    val scrollState = rememberScrollState()
    AppTheme {
        androidx.compose.material.Scaffold(
            bottomBar = { MyBottomNavigation(navController) }  // Alt navigasyon çubuğu olarak MyBottomNavigation'ı kullan
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues),
                    ) {
                    Text(
                        text = "Goals",
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier.weight(0.1f))
                }
                ExposedDropdownMenuBox(
                    expanded = expandedGoal,
                    onExpandedChange = { expandedGoal = !expandedGoal }
                ) {
                    TextField(
                        value = goalInput,
                        onValueChange = { goalInput = it },
                        label = { Text("Select your goal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.width(300.dp).menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGoal,
                        onDismissRequest = { expandedGoal = false }
                    ) {
                        val filteredMessages = goalsOptions.filter { it.contains(goalInput, ignoreCase = true) }
                        filteredMessages.forEach { goal ->
                            DropdownMenuItem(
                                onClick = {
                                    goalInput = goal
                                    expandedGoal = false
                                },
                                text = { Text(goal) }
                            )
                        }
                    }
                }
                // Gün sayısı için Dropdown Menu
                ExposedDropdownMenuBox(
                    expanded = expandedDays,
                    onExpandedChange = { expandedDays = !expandedDays }
                ) {
                    TextField(
                        value = daysInput,
                        onValueChange = { },
                        label = { Text("Select days") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDays) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.width(300.dp).menuAnchor(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDays,
                        onDismissRequest = { expandedDays = false }
                    ) {
                        daysOptions.forEach { days ->
                            DropdownMenuItem(
                                onClick = {
                                    daysInput = days.toString()
                                    expandedDays = false
                                },
                                text = { Text("$days days") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {
                    val targetDate = LocalDate.now().plusDays(daysInput.toLongOrNull() ?: 0L)
                    val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
                    goalsList = goalsList + (goalInput to remainingDays)

                    // Firestore'a hedefi kaydet
                    currentUser?.email?.let { email ->
                        db.collection("users").document(email).collection("goals").add(
                            mapOf(
                                "goal" to goalInput,
                                "daysRemaining" to remainingDays
                            )
                        )
                    }
                    goalInput = ""
                    daysInput = ""
                }) {
                    Text("Save The Goal")
                }
                Spacer(modifier = Modifier.weight(1f))
                LazyColumn (modifier = Modifier.padding(8.dp, bottom = 68.dp)){
                    items(goalsList) { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⦿ ${goal.first}",
                                modifier = Modifier.weight(1f),
                                fontSize = 18.sp,
                                color = Color(0xFF325334)
                            )
                            // Circular progress indicator showing the days remaining as a percentage
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .padding(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = {
                                        if (totalDays > 0) goal.second.toFloat() / totalDays else 0f
                                    },
                                    color = Color.Green,
                                    strokeWidth = 4.dp
                                )
                                Text(
                                    text = "${goal.second}",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))


            }

        }
    }
}
@Preview(showBackground = true)
@Composable
fun GoalScreenPreview() {
    val navController = rememberNavController()
    AppTheme {
        ChatScreen(navController)
    }
}
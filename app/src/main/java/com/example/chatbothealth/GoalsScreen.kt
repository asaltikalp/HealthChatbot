package com.example.chatbothealth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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

    val hourglassEmoji = "\u231B"
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
                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn {
                    items(goalsList) { goal ->
                        Text(
                            buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black,
                                        fontSize = 20.sp
                                    )
                                ) {
                                    append(" ")
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF325334)
                                    )
                                ) {
                                    append(goal.first)
                                }
                                append(" \nLAST ")
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF325334)
                                    )
                                ) {
                                    append(goal.second.toString())
                                }
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF325334),
                                        shadow = Shadow(
                                            Color.Gray,
                                            offset = Offset(2f, 2f),
                                            blurRadius = 5f
                                        )
                                    )
                                ) {
                                    append(hourglassEmoji + " DAYS")
                                }
                            },
                            modifier = Modifier.padding(8.dp),
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedGoal,
                    onExpandedChange = { expandedGoal = !expandedGoal }
                ) {
                    TextField(
                        value = goalInput,
                        onValueChange = { },
                        label = { Text("Select your goal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoal) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGoal,
                        onDismissRequest = { expandedGoal = false }
                    ) {
                        goalsOptions.forEach { goal ->
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
                        modifier = Modifier.menuAnchor(),
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
                Spacer(modifier = Modifier.height(16.dp))

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
                    // TextField'ları sıfırla
                    goalInput = ""
                    daysInput = ""
                }) {
                    Text("Save The Goal")
                }
                Spacer(modifier = Modifier.weight(1f))
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
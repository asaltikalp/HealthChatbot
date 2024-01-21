package com.example.chatbothealth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = Color.Black, fontSize = 20.sp)) {
                                append("GOAL: ")
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = Color(0xFF325334))) {
                                append(goal.first)
                            }
                            append(" \nLAST ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = Color(0xFF325334))) {
                                append(goal.second.toString())
                            }
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = Color(0xFF325334), shadow = Shadow(Color.Gray, offset = Offset(2f, 2f), blurRadius = 5f))) {
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

            OutlinedTextField(
                value = goalInput,
                onValueChange = { goalInput = it },
                label = { Text("Type your goal...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = daysInput,
                onValueChange = { daysInput = it },
                label = { Text("For how many days?") }
            )

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageButton(
                    imagePainter = painterResource(id = R.drawable.profilepage),
                    onClick = {
                        navController.navigate("profile")
                    }
                )

                ImageButton(
                    imagePainter = painterResource(id = R.drawable.logo),
                    onClick = {
                        navController.navigate("chat")
                    }
                )

                Spacer(modifier = Modifier.weight(1f))

                ImageButton(
                    imagePainter = painterResource(id = R.drawable.goalspage),
                    onClick = {
                        navController.navigate("goals")
                    },
                    modifier = Modifier.size(120.dp),

                    )
            }
        }

    }
}
@Preview(showBackground = true)
@Composable
fun GoalScreenPreview() {
    // Önizleme için geçici NavController oluştur
    val navController = rememberNavController()
    AppTheme {
        ChatScreen(navController)
    }
}
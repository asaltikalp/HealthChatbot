package com.example.chatbothealth

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatbothealth.ui.theme.AppTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(navController: NavController) {
    var goalInput by remember { mutableStateOf("") }
    var daysInput by remember { mutableStateOf("") }
    var goalsList by remember { mutableStateOf(listOf<Pair<String, Long>>()) }
    val hourglassEmoji = "\u231B"
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn {
                items(goalsList) { goal ->
                    Text(
                        "Hedef: ${goal.first}, Kalan Gün: ${goal.second}" + hourglassEmoji,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = goalInput,
                onValueChange = { goalInput = it },
                label = { Text("Hedefinizi Girin") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = daysInput,
                onValueChange = { daysInput = it },
                label = { Text("Kaç Gün Sürecek?") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                val targetDate = LocalDate.now().plusDays(daysInput.toLongOrNull() ?: 0L)
                val remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
                goalsList = goalsList + (goalInput to remainingDays)

                // TextField'ları sıfırla
                goalInput = ""
                daysInput = ""
            }) {
                Text("Hedefi Kaydet")
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
                    }                    ,
                    modifier = Modifier.size(120.dp)
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
package com.example.chatbothealth
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatbothealth.ui.theme.AppTheme
import java.time.format.TextStyle

data class Question(
    val id: Int,
    val text: String,
    val options: List<String> = listOf("Strongly Agree", "Agree", "Disagree", "Strongly Disagree")
)

val questions = listOf(
    Question(1, "1. I have nothing to look forward to."),
    Question(2, "2. When there is a problem, I can always look on the bright side."),
    Question(3, "3. Sometimes I get the shakes (such as hand tremors)."),
    Question(4, "4. I get irritated very easily."),
    Question(5, "5. I can do things just as well as other people."),
    Question(6, "6. I feel that my my heart rate is abnormal (too fast or slow."),
    Question(7, "7. I think that hardships have made me stronger."),
    Question(8, "8. I don't see any meaning in life."),
    Question(9, "9. I can't stand being interrupted when I'm working."),
    Question(10, "10. I feel happy with myself overall."),
    Question(11, "11. Sometimes I feel I am useless.."),
    Question(12, "12. I can achieve my goals even though there are obstacles."),
    )
val darkGreen = 0xFF325334
@Composable
fun QuestionItem(
    question: Question,
    selectedOption: MutableState<String>, // selectedOption artık bir MutableState
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            question.text,
            fontSize = 17.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        question.options.forEach { option ->
            val isSelected = selectedOption.value == option
            Button(
                onClick = {
                    selectedOption.value = option
                    onOptionSelected(option) },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isSelected) Color.Gray else Color(darkGreen)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(option, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}


@Composable
fun StressScreen(navController: NavController) {
    val showDialog = remember { mutableStateOf(false) }
    val userResponses = remember { mutableMapOf<Int, String>() }
    val stressResult = remember { mutableStateOf(Pair("", 0.0)) } // Pair to hold stress level and percentage

    Scaffold(
        bottomBar = { MyBottomNavigation(navController) }  // Alt navigasyon çubuğu olarak MyBottomNavigation'ı kullan
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(46.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start=16.dp, end=48.dp)
            ) {
                Text(
                    "How well does the following statements describe you?",
                    style = MaterialTheme.typography.body1.copy(color = Color(darkGreen))
                )
            }
            questions.forEach { question ->
                val selectedOption = remember { mutableStateOf(userResponses[question.id] ?: "") }
                QuestionItem(
                    question = question,
                    selectedOption = selectedOption,
                    onOptionSelected = { selectedOption ->
                        userResponses[question.id] = selectedOption
                    }
                )
            }

            Button(
                onClick = {
                    stressResult.value = calculateStressLevel(userResponses)
                    showDialog.value = true
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.DarkGray
                ),
                modifier = Modifier
                    .padding(top = 16.dp)
            ) {
                Text("Completed",
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Technical support provided by the Institute of Psychology, Chinese Academy of Science",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )

            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog.value = false
                    },
                    title = { Text("Stress Level Result") },
                    text = { Text("Your stress level is ${stressResult.value.first}\n \nStress Score: ${stressResult.value.second.toInt()} %",
                        fontSize = 18.sp) },
                    confirmButton = {
                        Button(
                            onClick = { showDialog.value = false },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor =  Color(darkGreen)
                            )) {
                            Text("OK",
                                color=Color.White)
                        }
                    }
                )
            }
        }
    }
}

fun calculateStressLevel(responses: Map<Int, String>): Pair<String, Double> {
    var totalScore = 0
    responses.forEach { (id, answer) ->
        totalScore += when (id) {
            1, 3, 4, 6, 8, 9, 11 -> getScoreForNegativeQuestion(answer)
            else -> getScoreForPositiveQuestion(answer)
        }
    }
    val maxPossibleScore = 36 // 12 soru x en yüksek 3 puan
    val stressPercent = (totalScore.toDouble() / maxPossibleScore) * 100.0
    val stressLevel = when {
        stressPercent <= 33 -> "Low"
        stressPercent <= 66 -> "Normal"
        else -> "High"
    }
    return Pair(stressLevel, stressPercent)
}

fun getScoreForPositiveQuestion(answer: String): Int = when (answer) {
    "Strongly Agree" -> 0
    "Agree" -> 1
    "Disagree" -> 2
    "Strongly Disagree" -> 3
    else -> 0
}

fun getScoreForNegativeQuestion(answer: String): Int = when (answer) {
    "Strongly Disagree" -> 0
    "Disagree" -> 1
    "Agree" -> 2
    "Strongly Agree" -> 3
    else -> 0
}

@Composable
fun MyBottomNavigation(navController: NavController) {
    BottomNavigation (
        modifier = Modifier.height(90.dp),
        backgroundColor = Color.White

    ) {
        BottomNavigationItem(
            icon = {
                val profilePainter: Painter = painterResource(id = R.drawable.profilepage)
                Image(
                    painter = profilePainter,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(60.dp)
                )
            },
            label = { Text("Profile") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
        BottomNavigationItem(
            icon = {
                val chatPainter: Painter = painterResource(id = R.drawable.logo)
                Image(
                    painter = chatPainter,
                    contentDescription = "Chat",
                    modifier = Modifier.size(60.dp)
                )
            },
            label = { Text("Chat") },
            selected = false,
            onClick = { navController.navigate("chat") }
        )
        BottomNavigationItem(
            icon = {
                val goalsPainter: Painter = painterResource(id = R.drawable.goalspage)
                Image(
                    painter = goalsPainter,
                    contentDescription = "Goals",
                    modifier = Modifier.size(60.dp)
                )
            },
            label = { Text("Goals") },
            selected = false,
            onClick = { navController.navigate("goals") }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewStressScreen() {
    val navController = rememberNavController()
    AppTheme {
        StressScreen(navController)
    }
}
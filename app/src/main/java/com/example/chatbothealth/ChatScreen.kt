package com.example.chatbothealth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.chatbothealth.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
data class UserProfile(
    val username: String,
    val profileImageUrl: String
)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatScreen(navController: NavController) {
    val messages = remember { mutableStateListOf<Message>() }
    val textState = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val pastAssistantMessages = remember {
        mutableStateListOf<String>()
    }
    val userProfile = remember { mutableStateOf(UserProfile("", "")) }
    var expandedMessages by remember { mutableStateOf(false) }
    val messageOptions = listOf("How many calories should I consume daily and how can I track them?",
        "What are the best exercises for someone with heart disease?",
        "What are the unexpected benefits of reducing screen time before bed?",
        "What types of foods should I eat to lower my cholesterol?",
        "Can you suggest daily mindfulness practices to reduce anxiety?",
        "How often should I check my blood pressure at home if I have hypertension?",
        "What are some effective strategies for losing weight?",
        "Can you recommend a healthy meal plan for someone with high cholesterol?",
        "How many calories should I eat per day to maintain my current weight?",
        "What are the benefits of regular physical activity?",
        "Can you provide tips for managing stress and anxiety?",
        "What are the symptoms of type 2 diabetes?",
        "How can I improve my sleep quality?",
        "What is a balanced diet for a vegetarian?",
        "Are there any exercises recommended for lower back pain?",
        "How often should I get a health check-up?",
        "What are some natural remedies for high blood pressure?",
        "How can I quit smoking effectively and permanently?",
        "What vaccinations are recommended for adults?",
        "How do I perform a breast self-exam?",
        "What are the signs of dehydration, and how can I prevent it?",
        "What should I do if I think I'm having an allergic reaction?",
        "How can I increase my daily intake of fiber?",
        "What are the best sources of omega-3 fatty acids?",
        "Can you explain how to read a nutrition label?",
        "What are the early signs of skin cancer?",
        "What type of exercise routine is best for someone just starting out?",
        "How many servings of fruits and vegetables should I eat each day?",
        "What are some effective techniques to help manage stress?",
        "Can you recommend strategies for improving heart health?",
        "How can I tell if I'm getting enough vitamins from my diet?",
        "What are the best ways to increase bone density?",
        "How should I prepare for a marathon to avoid injuries?",
        "What foods should I avoid if I have irritable bowel syndrome?",
        "Can you suggest any exercises that are safe during pregnancy?",
        "What are some signs that I might have a thyroid issue?",
        "How can I improve my posture when working at a desk all day?",
        "What are some low-impact exercises that are effective for weight loss?",
        "How can I make sure I'm drinking enough water throughout the day?",
        "What are the warning signs of diabetes that I should look out for?",
        "How can I effectively lower my cholesterol through diet?",
        "What steps can I take to prevent high blood pressure?",
        "What are the most effective ways to quit smoking?",
        "How can I help strengthen my immune system naturally?",
        "What are some methods to help ensure a good night's sleep?",
        "What type of fats are healthiest to cook with?",
        "Is it better to exercise before or after meals?",
        "What are the health benefits of switching to a vegetarian diet?",
        "How can I incorporate more physical activity into my busy day?",
        "What supplements should I consider to enhance my overall health?",
        "How often should I perform self-exams for breast cancer detection?",
        "What are the signs of dehydration, and how can I prevent it?",
        "What should I do if I think I'm having an allergic reaction?",
        "How do I manage pain during exercise without taking medication?",
        "What strategies can help me maintain a healthy diet while traveling?",
        "What are the best practices for calorie counting?",
        "How can I stay motivated to continue a physical activity regimen?",
        "What are the key nutrients I need if I'm considering a vegan diet?",
        "How can I safely increase the intensity of my workouts?",
        "What are some tips for eating well on a tight budget?",
        "How can I tell if I need more vitamins in my diet?",
        "What are the most effective methods to stop smoking?",
        "What are the best ways to reduce stress naturally?",
        "How often should adults receive vaccinations, and which are recommended?",
        "What are common symptoms of common viruses, and how can I protect myself?")

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Firestore'dan kullanıcı verilerini çekme
    LaunchedEffect(key1 = Unit) {
        currentUser?.email?.let { email ->
            db.collection("users").document(email).get().addOnSuccessListener { document ->
                val username = document.getString("username") ?: ""
                val profileImageUrl = document.getString("profileImageUrl") ?: ""
                userProfile.value = UserProfile(username, profileImageUrl)
            }
        }
    }
    AppTheme {
        androidx.compose.material.Scaffold(
            bottomBar = { MyBottomNavigation(navController) }  // Alt navigasyon çubuğu olarak MyBottomNavigation'ı kullan
        ) { paddingValues ->
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    )
                    Spacer(modifier = Modifier
                                .weight(1f)
                                .padding(paddingValues)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(messages) { message ->
                        MessageRow(
                            message = message,
                            isFromUser = message.isFromUser,
                            userProfileUrl = if (message.isFromUser) userProfile.value.profileImageUrl else "",
                            assistantImageId = R.drawable.logo,
                            username = if (message.isFromUser) userProfile.value.username else "Assistant"
                        )
                        Text(text = "\n")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    ExposedDropdownMenuBox(
                        expanded = expandedMessages,
                        onExpandedChange = { expandedMessages = !expandedMessages }
                    ) {
                        TextField(
                            value = textState.value,
                            onValueChange = { textState.value = it },
                            label = { Text("Select a message") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMessages) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.width(300.dp).menuAnchor()  // TextField'ın genişliğini sabitle
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMessages,
                            onDismissRequest = { expandedMessages = false }
                        ) {
                            // Filtrelenen mesajları göster
                            val filteredMessages = messageOptions.filter { it.contains(textState.value, ignoreCase = true) }
                            filteredMessages.forEach { message ->
                                DropdownMenuItem(
                                    onClick = {
                                        textState.value = message
                                        expandedMessages = false
                                    },
                                    text = { Text(message) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.1f))

                    Button(
                        modifier = Modifier.height(48.dp),
                        onClick = {
                            if (textState.value.isNotBlank()) {
                                messages.add(
                                    Message(
                                        textState.value,
                                        true, // Kullanıcıdan gelen mesaj
                                        "User",
                                        R.drawable.userprofile
                                    )
                                )
                                pastAssistantMessages.add(textState.value)  // Update the conversation history

                                scope.launch {
                                    val response = callChatOpenAI(
                                        ChatOpenAIOptions(
                                            userMessage = textState.value,
                                            systemMessage = "You are a health coach. Provide helpful and relevant advice with bullet points. Keep it short. You may ask for personal details for a better response if needed. Don't leave the response unfinished.",
                                            assistantMessage = pastAssistantMessages.joinToString(
                                                separator = "\n"
                                            ),
                                            maxToken = 100
                                        )
                                    )
                                    messages.add(
                                        Message(
                                            response,
                                            false, // Asistandan gelen mesaj
                                            "Assistant",
                                            R.drawable.logo // Asistan için profil resmi
                                        )
                                    )
                                    pastAssistantMessages.add(response)  // Update the conversation history with the assistant's response
                                }
                                textState.value = ""
                            }
                        }) {
                        Text("Send")
                    }
                }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ImageButton(
                            imagePainter = painterResource(id = R.drawable.logo),
                            onClick = {
                                navController.navigate("chat")
                            },
                            modifier = Modifier.size(65.dp),
                        )
                        Text(text = "Chat")
                    }
                }
            }
        }
    }
}
@Composable
fun ImageButton(
    imagePainter: Painter, // Resim için Painter nesnesi
    onClick: () -> Unit, // Tıklama işlevi
    modifier: Modifier = Modifier, // Boyut ve diğer ayarlar için Modifier
    buttonSize: Int = 98 // Butonun boyutu dp cinsinden
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
    ) {
        Image(
            painter = imagePainter,
            contentDescription = null,
        )
    }
}


@OptIn(BetaOpenAI::class)
suspend fun callChatOpenAI(openAIOptions: ChatOpenAIOptions): String {
    val openAI = OpenAI(
        token = "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")
    val begin = System.nanoTime()
    Log.v("TAG", "GPT PROCESS STARTED")

    val messages = arrayListOf<ChatMessage>()
    messages.add(
        ChatMessage(
            role = ChatRole.System,
            content = openAIOptions.systemMessage
        )
    )
    messages.add(
        ChatMessage(
            role = ChatRole.User,
            content = openAIOptions.userMessage
        )
    )
    if (openAIOptions.assistantMessage.isNotEmpty()) {
        messages.add(
            ChatMessage(
                role = ChatRole.Assistant,
                content = openAIOptions.assistantMessage
            )
        )
    }
    val chatCompletionRequest = ChatCompletionRequest(
        model = ModelId("gpt-3.5-turbo"),
        messages = messages,
        maxTokens = openAIOptions.maxToken,
        temperature = openAIOptions.temperature,
        topP = openAIOptions.top_p
    )
    val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
    Log.v("TAG", "GPT PROCESS FINISHED")

    val end = System.nanoTime()
    val result = completion.choices.firstOrNull()?.message?.content.orEmpty()
    Log.v("TAG", "OpenAI RAW Result: $result")
    Log.v("TAG", "Elapsed time in nanoseconds: ${end - begin}")

    return result
}

@Composable
fun MessageRow(
    message: Message,
    isFromUser: Boolean,
    userProfileUrl: String, // Kullanıcı profil resmi URL'si
    assistantImageId: Int, // Asistan profil resmi kaynak ID'si
    username: String // Kullanıcı adı
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isFromUser) Arrangement.Start else Arrangement.End
    ) {
        val imagePainter = if (isFromUser) {
            rememberAsyncImagePainter(userProfileUrl)
        } else {
            painterResource(id = assistantImageId)
        }

        Image(
            painter = imagePainter,
            contentDescription = "Profil Resmi",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = username,
                fontWeight = FontWeight.Bold
            )
            Text(text = message.content)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val navController = rememberNavController()
    AppTheme {
        ChatScreen(navController)
    }
}
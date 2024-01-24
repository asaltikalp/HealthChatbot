package com.example.chatbothealth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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

    // Firestore referansı
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
                Spacer(modifier = Modifier.weight(1f))
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
                OutlinedTextField(
                    value = textState.value,
                    onValueChange = { textState.value = it },
                    label = { Text("Ask something...") },
                    modifier = Modifier.weight(1f)
                )
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
                                        systemMessage = "You are a health coach. Provide helpful and relevant advice with bullet points. Keep it short. You may ask for personal details for a better response if needed",
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
                    horizontalAlignment = Alignment.CenterHorizontally // Bunu ekleyin
                ) {
                    ImageButton(
                        imagePainter = painterResource(id = R.drawable.profilepage),
                        onClick = {
                            navController.navigate("profile")
                        }
                    )
                    Text(text = "Profile")

                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally // Bunu ekleyin
                ) {
                    ImageButton(
                        imagePainter = painterResource(id = R.drawable.logo),
                        onClick = {
                            navController.navigate("chat")
                        },
                        modifier = Modifier.size(115.dp),
                    )
                    Text(text = "Chat")
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally // Bunu ekleyin
                ) {

                    ImageButton(
                        imagePainter = painterResource(id = R.drawable.goalspage),
                        onClick = {
                            navController.navigate("goals")
                        },
                    )
                    Text(text = "Goals")
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
    // Önizleme için geçici NavController oluştur
    val navController = rememberNavController()
    AppTheme {
        ChatScreen(navController)
    }
}
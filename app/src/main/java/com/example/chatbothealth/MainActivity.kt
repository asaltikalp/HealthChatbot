package com.example.chatbothealth

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.logging.Logger
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.*
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.model.ModelId

data class Message(val content: String, val isUser: Boolean, val userName: String, val userImageId: Int)

interface OpenAIOptions{
    var maxToken : Int
}
data class ChatOpenAIOptions(
    val userMessage: String = "",
    val systemMessage: String = "",
    val assistantMessage: String = "",
    var temperature: Double = 0.8,
    val top_p: Double = 1.0,
    override var maxToken: Int,
) : OpenAIOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    val scope = rememberCoroutineScope()
                    ChatScreen { message, onResponseReceived ->
                        scope.launch {
                            val response = callChatOpenAI(ChatOpenAIOptions(userMessage = message, systemMessage = "You are a Health professor who gives bullet point advices with maximum 500 characters", assistantMessage = "", maxToken = 500))
                            onResponseReceived(response) // Pass the result to the callback
                        }
                    }
                }
            }
        }
    }

    @OptIn(BetaOpenAI::class)
    suspend fun callChatOpenAI(openAIOptions: ChatOpenAIOptions): String {
        val openAI = OpenAI(
            token = "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") // Replace with your actual token
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
}


data class ChatCompletionRequestBody @OptIn(BetaOpenAI::class) constructor(
    val model: String,
    val messages: List<ChatMessage>
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val choices: List<ChatChoice>
)

data class ChatChoice @OptIn(BetaOpenAI::class) constructor(
    val message: ChatMessage
)

@Composable
fun PageSelection(){
    val navController = rememberNavController()
    
   // NavHost(navController = navController, graph = "First PAge")
}

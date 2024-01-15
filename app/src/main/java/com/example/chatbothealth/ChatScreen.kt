package com.example.chatbothealth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(onMessageSend: (String, (String) -> Unit) -> Unit) {
    val messages = remember { mutableStateListOf<Message>() }
    val textState = remember { mutableStateOf("") }
    val userName = "Kullanıcı"
    val userImageId = R.drawable.userprofile
    val pastAssistantMessages = remember {
        mutableStateListOf<String>()
    }


    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Healthbot",
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /* Menu click handling */ }) {
                Text(text = "☰")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                MessageRow(message, userName, userImageId)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                label = { Text("Mesajınızı yazın") },
                modifier = Modifier.weight(1f)
            )

            Button(onClick = {
                if (textState.value.isNotBlank()) {
                    messages.add(Message(textState.value, true, userName, userImageId))

                    // Call `onMessageSend` with the user message and a callback function
                    onMessageSend(textState.value) { response ->
                        messages.add(Message(response, false, "Assistant", R.drawable.userprofile))
                        // Other UI updates
                    }
                    textState.value = ""
                }
            }) {
                Text("Gönder")
            }
        }
    }
}

@Composable
fun MessageRow(message: Message, userName: String, userImageId: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = userImageId),
            contentDescription = "Profil Resmi",
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = userName, fontWeight = FontWeight.Bold)
            Text(text = message.content)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    ChatScreen { message, assistant ->
        "Preview response" // This should return a String as a dummy response
    }
}
package com.example.chatbothealth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (password == confirmPassword) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Kullanıcı başarıyla kaydedildi
                            Log.d("FirebaseAuth", "User registered successfully")
                            navController.navigate("chat")
                        } else {
                            // Kayıt başarısız
                            Log.w("FirebaseAuth", "User registration failed", task.exception)
                        }
                    }
            } else {
                // Şifreler eşleşmiyor
                Log.d("FirebaseAuth", "Passwords do not match")
            }
        },
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF345323),
                contentColor = Color.White )) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("login") },
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF345323),
                contentColor = Color.White )) {
            Text("Already have an account?")
        }

    }
}
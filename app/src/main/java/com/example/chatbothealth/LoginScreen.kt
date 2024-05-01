import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatbothealth.R
import com.example.chatbothealth.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun LoginScreen(navController: NavController) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                    Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if(email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, navController) { error ->
                    errorMessage = error
                    showErrorDialog = true
                }
            } else {
                errorMessage = "Please enter both email and password"
                showErrorDialog = true
            }
        },
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF345323),
                contentColor = Color.White
            )) {
            Text("Login")
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }


        Button(
            onClick = { navController.navigate("register") },
            modifier = Modifier
                .padding(start = 40.dp, end = 40.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF345323),
                contentColor = Color.White
            )
        ) {
            Text("Register")
        }
    }
}

fun loginUser(email: String, password: String, navController: NavController, onError: (String) -> Unit) {
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Login successful, navigate to chat screen
                navController.navigate("chat") {
                    popUpTo("login") { inclusive = true } // This clears the back stack preventing going back to login screen
                }
            } else {
                // Login failed, show error message
                onError(task.exception?.message ?: "Login failed due to an unknown error")
            }
        }
}


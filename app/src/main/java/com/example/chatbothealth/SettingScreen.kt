package com.example.chatbothealth
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var bloodType by remember { mutableStateOf("") }
    var waterIntake by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var profileImageUrl by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var existingUserData by remember { mutableStateOf<Map<String, Any>?>(null) }

    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other")

    val coroutineScope = rememberCoroutineScope()
    val storageRef = FirebaseStorage.getInstance().reference
    val snackbarHostState = remember { SnackbarHostState() }

    fun manageUserData(load: Boolean = true, update: Boolean = false) {
        currentUser?.email?.let { email ->
            if (load) {
                db.collection("users").document(email).get().addOnSuccessListener { document ->
                    document?.data?.let {
                        existingUserData = it
                        username = it["username"] as? String ?: ""
                        phone = it["phone"] as? String ?: ""
                        weight = it["weight"] as? String ?: ""
                        height = it["height"] as? String ?: ""
                        bloodType = it["bloodType"] as? String ?: ""
                        waterIntake = it["waterIntake"] as? String ?: ""
                        gender = it["gender"] as? String ?: ""
                        profileImageUrl = it["profileImageUrl"] as? String ?: ""
                    }
                }
            }

            if (update) {
                val updates = hashMapOf<String, Any>()
                existingUserData?.let { existingData ->
                    if (username != existingData["username"]) updates["username"] = username
                    if (phone != existingData["phone"]) updates["phone"] = phone
                    if (weight != existingData["weight"]) updates["weight"] = weight
                    if (height != existingData["height"]) updates["height"] = height
                    if (bloodType != existingData["bloodType"]) updates["bloodType"] = bloodType
                    if (waterIntake != existingData["waterIntake"]) updates["waterIntake"] = waterIntake
                    if (gender != existingData["gender"]) updates["gender"] = gender
                    if (profileImageUrl != existingData["profileImageUrl"]) updates["profileImageUrl"] = profileImageUrl
                }

                if (updates.isNotEmpty()) {
                    db.collection("users").document(email).update(updates)
                        .addOnSuccessListener { coroutineScope.launch { snackbarHostState.showSnackbar("Bilgiler başarıyla güncellendi") } }
                        .addOnFailureListener { error -> coroutineScope.launch { snackbarHostState.showSnackbar("Güncelleme başarısız: ${error.message}") } }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        manageUserData()
    }

    fun uploadImageToFirebaseStorage(imageUri: Uri, onUrlReady: (String) -> Unit) {
        val fileRef = storageRef.child("profileImages/${UUID.randomUUID()}")
        coroutineScope.launch {
            fileRef.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    onUrlReady(downloadUri.toString())
                } else {
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadImageToFirebaseStorage(it) { imageUrl ->
            profileImageUrl = imageUrl
        }
        }
    }
    // BMI Hesaplama Fonksiyonu
    fun calculateBMI(): String {
        val weightValue = weight.toFloatOrNull()
        val heightValue = height.toFloatOrNull()
        if (weightValue != null && heightValue != null && heightValue > 0) {
            val bmiValue = weightValue / ((heightValue / 100) * (heightValue / 100))
            return "%.2f".format(bmiValue)
        }
        return ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        IconButton(onClick = { navController.navigate("profile") }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "ArrowBack"
            )
        }
        Button(onClick = { imagePickerLauncher.launch("image/*") }) {
            Text("Select Profile Image")
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            isError = phone.length != 10
        )

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, "drop-down", Modifier.clickable { expanded = true })
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            genderOptions.forEach { option ->
                DropdownMenuItem(onClick = {
                    gender = option
                    expanded = false
                }) {
                    Text(option)
                }
            }
        }
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") }
        )
        OutlinedTextField(
            value = height,
            onValueChange = {
                height = it
                bmi = calculateBMI()
            },
            label = { Text("Height (cm)") }
        )
        OutlinedTextField(
            value = bloodType,
            onValueChange = { bloodType = it },
            label = { Text("Blood Type") }
        )
        OutlinedTextField(
            value = waterIntake,
            onValueChange = { waterIntake = it },
            label = { Text("Daily Water Intake (glasses)") }
        )
        Text("BMI: $bmi")


        Button(onClick = { manageUserData(update = true) }) {
            Text("Save All")
        }

        androidx.compose.material.Button(
            onClick = { navController.navigate("login") },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF345323),
                contentColor = Color.White
            )
        ) {
            androidx.compose.material.Text("Logout")
        }

    }
}


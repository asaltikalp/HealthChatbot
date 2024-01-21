package com.example.chatbothealth
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.rememberCoroutineScope
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


    val coroutineScope = rememberCoroutineScope()
    val storageRef = FirebaseStorage.getInstance().reference

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
                    // Hata işleme
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
//        Button(
//            onClick = {
//                // ... [Mevcut kaydetme işlemi]
//                currentUser?.email?.let { email ->
//                    db.collection("users").document(email).update(
//                        "profileImageUrl", profileImageUrl
//                    )
//                }
//            }
//        ) {
//            Text("Save Profile Image")
//        }
        // Mevcut alanlar
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            isError = phone.length != 10  // Basit telefon numarası doğrulaması
        )
        // Sağlık bilgileri için yeni alanlar
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") }
        )
        OutlinedTextField(
            value = height,
            onValueChange = {
                height = it
                bmi = calculateBMI()  // Boy girildiğinde BMI hesapla
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
        Text("BMI: $bmi")  // BMI gösterimi

        Button(
            onClick = {
                if (phone.length == 10) {  // Telefon numarası doğru uzunluktaysa
                    currentUser?.email?.let { email ->
                        val userMap = mapOf(
                            "username" to username,
                            "phone" to phone,
                            "weight" to weight,
                            "height" to height,
                            "bloodType" to bloodType,
                            "waterIntake" to waterIntake,
                            "bmi" to bmi
                        )
                        db.collection("users").document(email).set(userMap)
                            .addOnSuccessListener {
                                // Profil resmi URL'sini ayrı bir çağrıda güncelle
                                if (profileImageUrl.isNotEmpty()) {
                                    db.collection("users").document(email)
                                        .update("profileImageUrl", profileImageUrl)
                                }

                                // Tüm alanları sıfırla
                                username = ""
                                phone = ""
                                weight = ""
                                height = ""
                                bloodType = ""
                                waterIntake = ""
                                bmi = ""
                                profileImageUrl = ""
                            }
                            .addOnFailureListener {
                                // Hata işleme
                            }
                    }
                }
            }
        ) {
            Text("Save All")
        }

    }
}


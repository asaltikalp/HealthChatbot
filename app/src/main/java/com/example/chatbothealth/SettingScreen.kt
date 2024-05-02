package com.example.chatbothealth
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val ages = List(80) { it + 15 }
    val heights = List(100) { it + 110 }
    val genders = listOf("Male", "Female", "Other")
    val weights = List(100) { it  + 30  }
    val bloodTypes = listOf("ARh+", "ARh-", "BRh+", "BRh-", "ABRh+", "ABRh-", "ORh+", "ORh-")
    val waterIntakes = List(20) { it + 1 }
    val storageRef = FirebaseStorage.getInstance().reference
    var profileImageUrl by remember { mutableStateOf("") }
    var bmi by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var expandedAge by remember { mutableStateOf(false) }
    var height by remember { mutableStateOf("") }
    var expandedHeight by remember { mutableStateOf(false) }
    var gender by remember { mutableStateOf("") }
    var expandedGender by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("") }
    var expandedWeight by remember { mutableStateOf(false) }
    var bloodType by remember { mutableStateOf("") }
    var expandedBlood by remember { mutableStateOf(false) }
    var waterIntake by remember { mutableStateOf("") }  // Default değeri ilk eleman olarak ayarla
    var expandedWater by remember { mutableStateOf(false) }


    fun manageUserData(load: Boolean = true, update: Boolean = false) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        currentUser?.email?.let { email ->
            val userDocRef = db.collection("users").document(email)

            if (load) {
                // Veritabanından kullanıcı verilerini çekme
                userDocRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.data?.let { data ->
                            username = data["username"] as? String ?: ""
                            phone = data["phone"] as? String ?: ""
                            age = data["age"] as? String ?: ""
                            height = data["height"] as? String ?: ""
                            gender = data["gender"] as? String ?: ""
                            weight = data["weight"] as? String ?: ""
                            bloodType = data["bloodType"] as? String ?: ""
                            waterIntake = data["waterIntake"] as? String ?: ""
                            profileImageUrl = data["profileImageUrl"] as? String ?: ""
                        }
                    }
                }
            }

            if (update) {
                // Kullanıcı verilerini güncelleme
                val updates = mapOf(
                    "username" to username,
                    "phone" to phone,
                    "age" to age,
                    "height" to height,
                    "gender" to gender,
                    "weight" to weight,
                    "bloodType" to bloodType,
                    "waterIntake" to waterIntake,
                    "profileImageUrl" to profileImageUrl,
                    "bmi" to bmi
                )
                userDocRef.set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Firestore", "User data successfully updated.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating user data", e)
                    }
            }
        } ?: Log.e("Authentication", "No authenticated user found")
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
            val heightInMeters = heightValue / 100  // cm'den metreye çevir
            val bmiValue = weightValue / (heightInMeters * heightInMeters)
            return "%.2f".format(bmiValue)
        }
        return ""
    }
    LaunchedEffect(Unit) {
        manageUserData()
    }
    LaunchedEffect(weight, height) {
        bmi = calculateBMI()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

            IconButton(onClick = { navController.navigate("profile") }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "ArrowBack"
                )
            }
            Spacer(modifier = Modifier.height(18.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Select Profile Image")
            }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            isError = phone.length != 10
        )
        // Gender Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedGender,
            onExpandedChange = { expandedGender = !expandedGender }
        ) {
            TextField(
                value = gender,
                onValueChange = { },
                label = { Text("Select Gender") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedGender,
                onDismissRequest = { expandedGender = false }
            ) {
                genders.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            gender = item
                            expandedGender = false
                        },
                        text = { Text(item) }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = expandedAge,
            onExpandedChange = { expandedAge = !expandedAge }
        ) {
            TextField(
                value = age,
                onValueChange = { },
                label = { Text("Select Age") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAge) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedAge,
                onDismissRequest = { expandedAge = false }
            ) {
                ages.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            age = item.toString()
                            expandedAge = false
                        },
                        text = { Text("${item} years old") }
                    )
                }
            }
        }
        // Weight Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedWeight,
            onExpandedChange = { expandedWeight = !expandedWeight }
        ) {
            TextField(
                value = weight,
                onValueChange = { },
                label = { Text("Select Weight (kg)") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeight) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedWeight,
                onDismissRequest = { expandedWeight = false }
            ) {
                weights.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            weight = item.toString()
                            expandedWeight = false
                        },
                        text = { Text("${item} kg") }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expandedHeight,
            onExpandedChange = { expandedHeight = !expandedHeight }
        ) {
            TextField(
                value = height,
                onValueChange = { },
                label = { Text("Select Height (cm)") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHeight) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedHeight,
                onDismissRequest = { expandedHeight = false }
            ) {
                heights.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            height = item.toString()
                            expandedHeight = false
                        },
                        text = { Text("${item} cm") }
                    )
                }
            }
        }

    // Blood Type Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedBlood,
            onExpandedChange = { expandedBlood = !expandedBlood }
        ) {
            TextField(
                value = bloodType,
                onValueChange = { },
                label = { Text("Select Blood Type") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBlood) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedBlood,
                onDismissRequest = { expandedBlood = false }
            ) {
                bloodTypes.forEach { type ->
                    DropdownMenuItem(
                        onClick = {
                            bloodType = type
                            expandedBlood = false
                        },
                        text = { Text(type) }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expandedWater,
            onExpandedChange = { expandedWater = !expandedWater }
        ) {
            TextField(
                value = waterIntake,
                onValueChange = { },
                label = { Text("Select Water Intake") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWater) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedWater,
                onDismissRequest = { expandedWater = false }
            ) {
                waterIntakes.forEach { gls ->
                    DropdownMenuItem(
                        onClick = {
                            waterIntake = gls.toString()
                            expandedWater = false
                        },
                        text = { Text("${gls} glasses") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))


        Text("Your BMI is $bmi")


        Spacer(modifier = Modifier.height(12.dp))


        Button(onClick = {
            manageUserData(update = true)
            showDialog = true})
        {
            Text("Save All")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirmation") },
                text = { Text("Are you sure you want to save the data?") },
                confirmButton = {
                    Button(
                        onClick = {
                            manageUserData(update = true)
                            showDialog = false  // Close the dialog
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("No")
                    }
                }
            )
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


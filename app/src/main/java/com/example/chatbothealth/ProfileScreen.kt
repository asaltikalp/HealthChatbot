package com.example.chatbothealth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.chatbothealth.ui.theme.AppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.rememberAsyncImagePainter


@Composable
fun ProfileScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "No Email"
    var userName by remember { mutableStateOf("Loading...") }
    var userPhone by remember { mutableStateOf("Loading...") }
    var userWeight by remember { mutableStateOf("Loading...") }
    var userHeight by remember { mutableStateOf("Loading...") }
    var userBloodType by remember { mutableStateOf("Loading...") }
    var userWaterIntake by remember { mutableStateOf("Loading...") }
    var userBMI by remember { mutableStateOf("Loading...") }
    var userProfileImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(user.email!!)
            docRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    userName = document.getString("username") ?: "No Username"
                    userPhone = document.getString("phone") ?: "No Phone Number"
                    userWeight = document.getString("weight") ?: "No Weight"
                    userHeight = document.getString("height") ?: "No Height"
                    userBloodType = document.getString("bloodType") ?: "No Blood Type"
                    userWaterIntake = document.getString("waterIntake") ?: "No Water Intake"
                    userBMI = document.getString("bmi") ?: "No BMI"
                    userProfileImageUrl = document.getString("profileImageUrl") ?: ""
                }
            }
        }
    }

    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Profile Image and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.weight(2f))

                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (userProfileImageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(userProfileImageUrl),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
//                Image(
//                    painter = painterResource(id = R.drawable.userprofile),
//                    contentDescription = "Profile Photo",
//                    modifier = Modifier
//                        .size(80.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary)
//                )

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row {
                        ContactItem("Hello \uD83D\uDC4B ", userName)

                    }
                }
                   }
            ContactItem("Email ", userEmail)
            ContactItem("Phone ", userPhone)

            Spacer(modifier = Modifier.height(45.dp))
            // Contact Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Health Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )


                Spacer(modifier = Modifier.height(8.dp))

                data class HealthInfo(
                    val text: String,
                    val iconId: Int,
                    val backgroundColor: Color // Renk özelliğini ekleyin
                )

                val healthInfos = listOf(
                    HealthInfo("${userWeight} kg", R.drawable.weight, Color(0xFF8ad493)),
                    HealthInfo("${userHeight} cm", R.drawable.height, Color(0xFF8ad493)),
                    HealthInfo(userBloodType, R.drawable.blood, Color(0xFF8ad493)),
                    HealthInfo("$userWaterIntake Glass of Water", R.drawable.water, Color(0xFF8ad493)),
                    HealthInfo(userBMI, R.drawable.bmi, Color(0xFF8ad493))
                )
                Spacer(modifier = Modifier.height(32.dp))

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(healthInfos) { healthInfo ->
                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .height(100.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(healthInfo.backgroundColor)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = healthInfo.iconId),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = healthInfo.text,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Settings Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

            }
            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                ImageButton(
                    imagePainter = painterResource(id = R.drawable.profilepage),
                    onClick = {
                        navController.navigate("profile")
                    },
                    modifier = Modifier.size(130.dp),
                )

                ImageButton(
                    imagePainter = painterResource(id = R.drawable.logo),
                    onClick = {
                        navController.navigate("chat")
                    }

                )

                ImageButton(
                    imagePainter = painterResource(id = R.drawable.goalspage),
                    onClick = {
                        navController.navigate("goals")
                    }
                )
            }
        }
    }
}

@Composable
fun ContactItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            Text(text = label, fontWeight = FontWeight.Bold)
            Text(text = value)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
    AppTheme {
        ProfileScreen(navController)
    }
}

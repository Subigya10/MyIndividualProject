package com.example.kotlinnewproject

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

import com.example.kotlinnewproject.ui.theme.Pink80
import com.example.kotlinnewproject.ui.theme.Purple80

class LoginAct : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            loginScreen()
        }
    }
}

@Composable
fun loginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity=context as Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.iphone),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text("Welcome Back", fontSize = 22.sp, color = Color.White)
            Text("Login to your account", fontSize = 14.sp, color = Color.White)
            Spacer(modifier = Modifier.height(20.dp))

            // Email Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = Color.Black) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(R.drawable.baseline_alternate_email_24),
                            contentDescription = null

                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Purple80,
                        focusedIndicatorColor = Pink80,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.Black) },
                    leadingIcon = {
                        Image(
                            painter = painterResource(R.drawable.baseline_key_24),
                            contentDescription = null

                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Purple80,
                        focusedIndicatorColor = Pink80,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { visibility = !visibility }) {
                            Icon(
                                painter = if (visibility)
                                    painterResource(R.drawable.baseline_visibility_off_24)
                                else
                                    painterResource(R.drawable.baseline_visibility_24),
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    } else {
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                    val userId = auth.currentUser?.uid ?: ""
                                    val userEmail = auth.currentUser?.email ?: ""
                                    val db = com.google.firebase.database.FirebaseDatabase.getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com").getReference("Users").child(userId)
                                    db.get().addOnSuccessListener { snapshot ->
                                        val firstName = snapshot.child("firstName").value?.toString() ?: "Player"
                                        val lastName = snapshot.child("lastName").value?.toString() ?: ""
                                        val fullName = "$firstName $lastName".trim()
                                        Toast.makeText(context, "Got: $fullName", Toast.LENGTH_LONG).show()
                                        val intent = Intent(context, DashboardActivity::class.java)
                                        intent.putExtra("userName", fullName)
                                        intent.putExtra("userEmail", userEmail)
                                        context.startActivity(intent)
                                    }.addOnFailureListener {
                                        Toast.makeText(context, "DB Error: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f)  // Uses 95% of available width instead of full width
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(30.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))),
                            shape = RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Login", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Forgot password?",
                color = Color.White,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable {
                        val intent = Intent(context, ForgetPassword::class.java)
                        context.startActivity(intent)
                    }
            )

            Spacer(modifier = Modifier.height(24.dp))




            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                            )
                        )
                )


                Text(
                    "⚡ Continue with ⚡",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )


                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Facebook Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1877F2), Color(0xFF1565C0))
                            )
                        )
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.facebook.com")
                            )
                            context.startActivity(intent)
                        }
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.18f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.facebook),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Facebook", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }


                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White)
                        .clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://accounts.google.com")
                            )
                            context.startActivity(intent)
                        }
                        .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(30.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.LightGray.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ){
                Text(
                    text = "New User? ",
                    color = Color.Yellow,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign Up",
                    color = Purple80,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        // Navigate to Sign Up screen
                        activity.finish()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun loginPreview() {
    loginScreen()
}

package com.example.kotlinnewproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.ViewModel
import com.example.kotlinnewproject.model.UserModel
import com.example.kotlinnewproject.repository.UserRepoImpl



import com.example.kotlinnewproject.ui.theme.Pink80
import com.example.kotlinnewproject.ui.theme.Purple80
import com.example.kotlinnewproject.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            registerme()

        }
    }
}



@Composable
fun registerme() {

    var FirstName by remember { mutableStateOf("") }
    var LastName by remember { mutableStateOf("") }
    var Email by remember { mutableStateOf("") }
    var Password by remember { mutableStateOf("") }
    var ConfirmPassword by remember { mutableStateOf("") }
    val context= LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var visibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(
            modifier = Modifier.fillMaxSize()


        ) {
            Image(
                painter = painterResource(R.drawable.iphone),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )


            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(70.dp))
                Text("Create a fantasy app", fontSize = 20.sp, color = Color.White)

                Spacer(modifier = Modifier.height(10.dp))

                Text("Create your account and start building your team", color = Color.White)


//                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 30.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,


                    ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)

                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 8.dp)

                    ) {

                        OutlinedTextField(
                            value = FirstName,
                            onValueChange = { FirstName = it },
                            placeholder = { Text("First Name", color = Color.Black) },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.baseline_person_24),
                                    contentDescription = null

                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
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
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = LastName,
                            onValueChange = { LastName = it },
                            placeholder = { Text("Last Name", color = Color.Black) },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.baseline_short_text_24),
                                    contentDescription = null

                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
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
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = Email,
                            onValueChange = { Email = it },
                            placeholder = { Text("Email", color = Color.Black) },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.baseline_alternate_email_24),
                                    contentDescription = null

                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                                .height(56.dp),
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
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 28.dp,
                                vertical = 8.dp
                            ) // Remove fillMaxWidth from here
                    ) {

                        OutlinedTextField(
                            value = Password,
                            onValueChange = { Password = it },
                            placeholder = { Text("Password") },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.baseline_key_24),
                                    contentDescription = null,
//                                        modifier = Modifier.size(20.dp) // Make icon smaller
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                                .height(56.dp),
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
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp) // Make icon smaller
                                    )
                                }
                            },
                            singleLine = true // Keep it single line
                        )

                        val (strengthText, strengthColor) = when {
                            Password.isEmpty() -> "" to Color.Transparent
                            Password.length < 4 -> "Weak" to Color.Red
                            Password.length < 8 -> "Medium" to Color.Yellow
                            else -> "Strong" to Color.Green
                        }

                        if (strengthText.isNotEmpty()) {
                            Text(
                                text = "Strength: $strengthText",
                                color = strengthColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }





                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = ConfirmPassword,
                            onValueChange = { ConfirmPassword = it },
                            placeholder = { Text("Confirm Password", color = Color.Black) },
                            leadingIcon = {
                                Image(
                                    painter = painterResource(R.drawable.baseline_lock_24),
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp), // Same as Password field
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent, // Same as Password field
                                focusedContainerColor = Purple80,
                                focusedIndicatorColor = Pink80,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                    Icon(
                                        painter = if (confirmPasswordVisibility)
                                            painterResource(R.drawable.baseline_visibility_off_24)
                                        else
                                            painterResource(R.drawable.baseline_visibility_24),
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true // Added for consistency
                        )
                    }
                }



                // Row for "Already have an account? Login"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", color = Color.White.copy(alpha = 0.8f))
                    Text(
                        "Login",
                        color = Color.White,
                        modifier = Modifier.clickable {

                            context.startActivity(Intent(context, LoginAct::class.java))
                        }
                    )
                }
                Button(
                    onClick = {
                        if (Email.isEmpty() || Password.isEmpty() || FirstName.isEmpty() || LastName.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        if (Password != ConfirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT)
                                .show()
                            return@Button
                        }

                        // Register user with Firebase
                        userViewModel.register(Email, Password) { success, message, userId ->
                            if (success) {
                                // Create user model
                                val model = UserModel(
                                    id = userId,
                                    firstName = FirstName,
                                    lastName = LastName,
                                    email = Email,
                                    dob = "",
                                    gender = ""
                                )

                                // Add user to database
                                userViewModel.addUserToDatabase(
                                    userId,
                                    model
                                ) { dbSuccess, dbMessage ->
                                    Toast.makeText(context, dbMessage, Toast.LENGTH_SHORT).show()

                                    if (dbSuccess) {
                                        // Navigate to LoginAct on success
                                        val intent = Intent(context, LoginAct::class.java)
                                        context.startActivity(intent)
                                        if (context is ComponentActivity) context.finish()
                                    }
                                }
                            } else {
                                // Show error message
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(600.dp)
                        .height(65.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))),
                                        shape = RoundedCornerShape(25.dp)
                            ),

                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sign Up", color = Color.White, fontSize = 20.sp)
                    }
                }







                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f), color = Color.White
                    )
                    Text("Or", modifier = Modifier.padding(horizontal = 20.dp), color = Color.White)

                    HorizontalDivider(
                        modifier = Modifier.weight(1f), color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 15.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .height(60.dp)
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text("Facebook")
                        }
                    }
                    Spacer(
                        modifier = Modifier.width(20.dp)
                    )
                    Card(
                        modifier = Modifier
                            .height(60.dp)
                            .weight(1f),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.google),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text("Google")
                        }
                    }
                }

            }
        }

    }
}
@Preview
@Composable
fun registerpreview(){
    registerme()
}



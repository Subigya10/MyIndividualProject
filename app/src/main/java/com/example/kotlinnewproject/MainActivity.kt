package com.example.kotlinnewproject

import android.content.Intent
import android.database.Cursor
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.example.kotlinnewproject.model.UserModel
import com.example.kotlinnewproject.repository.UserRepoImpl
import com.example.kotlinnewproject.ui.theme.Pink80
import com.example.kotlinnewproject.ui.theme.Purple80
import com.example.kotlinnewproject.ui.theme.PurpleGrey80
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
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    var visibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    var checkbox by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(70.dp))
                Text("Create a fantasy app", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Create your account and start building your team", color = Color.White)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 30.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // First Name
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = FirstName,
                            onValueChange = { FirstName = it },
                            placeholder = { Text("First Name", color = Color.Black) },
                            leadingIcon = {
                                Image(painter = painterResource(R.drawable.baseline_person_24), contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("firstName"),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Purple80,
                                focusedIndicatorColor = Pink80,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Last Name
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = LastName,
                            onValueChange = { LastName = it },
                            placeholder = { Text("Last Name(optional)", color = Color.Black) },
                            leadingIcon = {
                                Image(painter = painterResource(R.drawable.baseline_short_text_24), contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("lastName"),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Purple80,
                                focusedIndicatorColor = Pink80,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = Email,
                            onValueChange = { Email = it },
                            placeholder = { Text("Email", color = Color.Black) },
                            leadingIcon = {
                                Image(painter = painterResource(R.drawable.baseline_alternate_email_24), contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("regEmail"),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Purple80,
                                focusedIndicatorColor = Pink80,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = Password,
                            onValueChange = { Password = it },
                            placeholder = { Text("Password", color = Color.Black) },
                            leadingIcon = {
                                Image(painter = painterResource(R.drawable.baseline_key_24), contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("regPassword"),
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
                                        painter = if (visibility) painterResource(R.drawable.baseline_visibility_off_24)
                                        else painterResource(R.drawable.baseline_visibility_24),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            singleLine = true
                        )

                        val hasUppercase = Password.any { it.isUpperCase() }
                        val hasSpecial = Password.any { !it.isLetterOrDigit() }
                        val isLongEnough = Password.length >= 6

                        val (strengthText, strengthColor) = when {
                            Password.isEmpty() -> "" to Color.Transparent
                            !isLongEnough -> "Min 6 characters required" to Color.Red
                            !hasUppercase -> "Add at least 1 uppercase letter" to Color.Red
                            !hasSpecial -> "Add at least 1 special character" to Color.Red
                            else -> "Strong ✓" to Color.Green
                        }
                        if (strengthText.isNotEmpty()) {
                            Text(text = "Strength: $strengthText", color = strengthColor, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Confirm Password
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp)
                    ) {
                        OutlinedTextField(
                            value = ConfirmPassword,
                            onValueChange = { ConfirmPassword = it },
                            placeholder = { Text("Confirm Password", color = Color.Black) },
                            leadingIcon = {
                                Image(painter = painterResource(R.drawable.baseline_lock_24), contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).testTag("confirmPassword"),
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Purple80,
                                focusedIndicatorColor = Pink80,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                    Icon(
                                        painter = if (confirmPasswordVisibility) painterResource(R.drawable.baseline_visibility_off_24)
                                        else painterResource(R.drawable.baseline_visibility_24),
                                        contentDescription = null
                                    )
                                }
                            },
                            singleLine = true
                        )
                    }

                    // Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checkbox,
                            onCheckedChange = { checkbox = it },
                            modifier = Modifier.testTag("checkbox"),
                            colors = CheckboxDefaults.colors(
                                uncheckedColor = Color.White,
                                checkmarkColor = Color.Blue,
                                checkedColor = Color.White
                            )
                        )
                        Text("I agree to terms & Conditions", color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = Color(0xFFFFE082), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(
                            "Login",
                            color = PurpleGrey80, fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                context.startActivity(Intent(context, LoginAct::class.java))
                            }
                        )
                    }
                }

                // Sign Up Button
                Button(
                    onClick = {
                        if (Email.isEmpty() || Password.isEmpty() || FirstName.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                            Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val hasUppercase = Password.any { it.isUpperCase() }
                        val hasSpecial = Password.any { !it.isLetterOrDigit() }
                        if (Password.length < 6 || !hasUppercase || !hasSpecial) {
                            Toast.makeText(context, "Password must be 6+ chars, 1 uppercase, 1 special character", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (Password != ConfirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!checkbox) {
                            Toast.makeText(context, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        userViewModel.register(Email, Password) { success, message, userId ->
                            if (success) {
                                val model = UserModel(
                                    id = userId,
                                    firstName = FirstName,
                                    lastName = LastName,
                                    email = Email,
                                    dob = "",
                                    gender = ""
                                )
                                userViewModel.addUserToDatabase(userId, model) { dbSuccess, dbMessage ->
                                    Toast.makeText(context, dbMessage, Toast.LENGTH_SHORT).show()
                                    if (dbSuccess) {
                                        val intent = Intent(context, LoginAct::class.java)
                                        context.startActivity(intent)
                                        if (context is ComponentActivity) context.finish()
                                    }
                                }
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.width(600.dp).height(65.dp).testTag("signUpBtn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
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
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(Brush.horizontalGradient(colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)))))
                    Text("⚡ Continue with ⚡", modifier = Modifier.padding(horizontal = 16.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box(modifier = Modifier.weight(1f).height(2.dp).background(Brush.horizontalGradient(colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)))))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f).height(58.dp).clip(RoundedCornerShape(30.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF1877F2), Color(0xFF1565C0))))
                            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com"))) }
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                                Image(painter = painterResource(R.drawable.facebook), contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Facebook", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f).height(58.dp).clip(RoundedCornerShape(30.dp))
                            .background(Color.White)
                            .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com"))) }
                            .border(1.dp, Color.Gray.copy(alpha = 0.25f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).background(Color.LightGray.copy(alpha = 0.3f), CircleShape), contentAlignment = Alignment.Center) {
                                Image(painter = painterResource(R.drawable.google), contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Google", color = Color.Black, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun registerpreview() {
    registerme()
}
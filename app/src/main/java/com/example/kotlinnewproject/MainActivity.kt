package com.example.kotlinnewproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
                    OutlinedTextField(

                        value = FirstName,
                        onValueChange = { data ->
                            FirstName = data

                        },
                        placeholder = { Text("FirstName", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),

                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(0.8f),
                            focusedContainerColor = Purple80,
                            focusedIndicatorColor = Pink80,
                            unfocusedIndicatorColor = Color.Transparent
                        )

                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(

                        value = LastName,
                        onValueChange = { data ->
                            LastName = data

                        },
                        placeholder = { Text("LastName", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),

                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(0.8f),
                            focusedContainerColor = Purple80,
                            focusedIndicatorColor = Pink80,
                            unfocusedIndicatorColor = Color.Transparent
                        )

                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(

                        value = Email,
                        onValueChange = { data ->
                            Email = data

                        },
                        placeholder = { Text("Email", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),

                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(0.8f),
                            focusedContainerColor = Purple80,
                            focusedIndicatorColor = Pink80,
                            unfocusedIndicatorColor = Color.Transparent
                        )

                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = Password,
                        onValueChange = { data ->
                            Password = data
                        },
                        placeholder = {
                            Text("*******")
                        },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(0.8f),
                            focusedContainerColor = Purple80,
                            focusedIndicatorColor = Pink80,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp),
                        visualTransformation = if (visibility)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                visibility = !visibility
                            }) {
                                Icon(
                                    painter = if (visibility)
                                        painterResource(R.drawable.baseline_visibility_off_24)
                                    else
                                        painterResource(R.drawable.baseline_visibility_24),
                                    contentDescription = null
                                )
                            }
                        }
                    )

                    val (strengthText, strengthColor) = when {
                        Password.isEmpty() -> "" to Color.Transparent  // nothing shown when empty
                        Password.length < 4 -> "Weak" to Color.Red
                        Password.length < 8 -> "Medium" to Color.Yellow
                        else -> "Strong" to Color.Green
                    }


                    Text(
                        text = "Password strength: $strengthText",
                        color = strengthColor,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 32.dp, top = 4.dp)
                    )



                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = ConfirmPassword,
                        onValueChange = { data ->
                            ConfirmPassword = data
                        },
                        placeholder = {
                            Text("*******")
                        },

                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(0.8f),
                            focusedContainerColor = Purple80,
                            focusedIndicatorColor = Pink80,
                            unfocusedIndicatorColor = Color.Transparent
                        ),

                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                        visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = {
                                visibility = !visibility
                            }) {
                                Icon(
                                    painter = if (visibility)
                                        painterResource(R.drawable.baseline_visibility_off_24)
                                    else painterResource(
                                        R.drawable.baseline_visibility_24
                                    ),


                                    contentDescription = null
                                )
                            }
                        }
                    )

                }

                Button(
                    onClick = {
                        if (Email.isEmpty() || Password.isEmpty() || FirstName.isEmpty() || LastName.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (Password != ConfirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
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
                                userViewModel.addUserToDatabase(userId, model) { dbSuccess, dbMessage ->
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
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp)
                ) {
                    Text("Sign Up")
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



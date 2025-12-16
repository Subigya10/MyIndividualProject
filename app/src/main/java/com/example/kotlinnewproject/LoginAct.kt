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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
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


import com.example.kotlinnewproject.ui.theme.KotlinnewprojectTheme
import com.example.kotlinnewproject.ui.theme.Pink80
import com.example.kotlinnewproject.ui.theme.Purple80

class LoginAct : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            loginme()

        }
    }
}



@Composable
fun loginme() {


    var Email by remember { mutableStateOf("") }
    var Password by remember { mutableStateOf("") }
    val context= LocalContext.current


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
                        placeholder = { Text("Password", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),

                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White.copy(0.8f),
                            focusedContainerColor = Purple80,
                            focusedIndicatorColor = Pink80,
                            unfocusedIndicatorColor = Color.Transparent
                        )

                    )


                }

                Button(
                    onClick = {
                        if (Email.isNotEmpty() && Password.isNotEmpty()) {
                            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                            auth.signInWithEmailAndPassword(Email, Password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                                        // Navigate to dashboard or main screen
//                                        val intent = Intent(context, DashboardActivity::class.java)
//                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Login failed: ${task.exception?.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp)
                ) {
                    Text("Login")
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {

                    Text(
                        text = "Forgot password?",
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 26.dp)
                            .clickable(

                            ){
                                val intent = Intent(context, ForgetPassword::class.java)
                                context.startActivity(intent)

                                // Handle forgot password click here
                            },
                    )



                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically

                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f), color = Color.White
                        )
                        Text(
                            "Or",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Color.White
                        )

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
}

@Preview
@Composable
fun loginpreview(){
    loginme()
}


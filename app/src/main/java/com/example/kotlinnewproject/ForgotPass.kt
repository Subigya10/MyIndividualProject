package com.example.kotlinnewproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinnewproject.ui.theme.Pink80
import com.example.kotlinnewproject.ui.theme.Purple80


class ForgetPassword : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ForgotPasswordScreen()
        }
    }
}

@Composable
fun ForgotPasswordScreen() {

    var Email by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(R.drawable.iphone),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        // Content Column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 150.dp, start = 16.dp, end = 16.dp), // push down
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Forgot password", color = Color.White, fontSize = 21.sp)
            Spacer(modifier = Modifier.height(30.dp))
            OutlinedTextField(
                value = Email,
                onValueChange = { Email = it },
                placeholder = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(0.8f),
                    focusedContainerColor = Purple80,
                    focusedIndicatorColor = Pink80,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            val context = LocalContext.current

            Button(
                onClick = {

                    if (Email.isNotEmpty()) {
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        auth.sendPasswordResetEmail(Email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Reset link sent to $Email",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Error: ${task.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Reset Link")
            }
        }
    }
}


            @Preview
@Composable
fun ForgotPasswordPreview() {
    ForgotPasswordScreen()
}

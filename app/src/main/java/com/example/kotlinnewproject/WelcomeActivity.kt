// WelcomeActivity.kt
package com.example.kotlinnewproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.kotlinnewproject.ui.theme.Purple80

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WelcomeTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun WelcomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}

@Composable
fun SplashScreen() {
    val context= LocalContext.current
    val activity = context as? Activity
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image - football field
        Image(
            painter = painterResource(id = R.drawable.field),
            contentDescription = "Football field background",
            modifier = Modifier
                .fillMaxSize()
                .matchParentSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent dark overlay so text and players are clearly visible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // All your original content (players + text) - alignment preserved exactly
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))  // Was 40.dp → now 80.dp


            Text(
                text = "Welcome to ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "SquadXI",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFc94b8f)  // Beautiful purple from your original gradient
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // 1. Top: Messi
                Image(
                    painter = painterResource(id = R.drawable.messii),
                    contentDescription = "Messi",
                    modifier = Modifier
                        .width(250.dp)
                        .height(250.dp)
                        .offset(y = (-120).dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.TopCenter
                )

                // 2. Second row: Neymar & Haaland
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 70.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.neymar),
                        contentDescription = "Neymar",
                        modifier = Modifier
                            .width(140.dp)
                            .height(190.dp)
                            .offset(x = (-15).dp, y = -70.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.TopCenter
                    )

                    Image(
                        painter = painterResource(id = R.drawable.haa),
                        contentDescription = "Haaland",
                        modifier = Modifier
                            .width(140.dp)
                            .height(190.dp)
                            .offset(x = 15.dp, y = -70.dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.TopCenter
                    )
                }

                // 3. Ronaldo - big in center
                Image(
                    painter = painterResource(id = R.drawable.roney),
                    contentDescription = "Ronaldo",
                    modifier = Modifier
                        .width(430.dp)
                        .height(460.dp)
                        .offset(y = 40.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.TopCenter
                )

                // 4. Bottom row: Yamal & Mbappé - independent positioning
                Box(modifier = Modifier.fillMaxSize()) {
                    // Yamal
                    Image(
                        painter = painterResource(id = R.drawable.yamal),
                        contentDescription = "Yamal",
                        modifier = Modifier
                            .width(220.dp)
                            .height(260.dp)
                            .align(Alignment.BottomCenter)
                            .offset(x = (-100).dp, y = (-60).dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.TopCenter
                    )

                    // Mbappé
                    Image(
                        painter = painterResource(id = R.drawable.mbbb),
                        contentDescription = "Mbappe",
                        modifier = Modifier
                            .width(220.dp)
                            .height(200.dp)
                            .align(Alignment.BottomCenter)
                            .offset(x = 100.dp, y = (-58).dp),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.TopCenter
                    )
                }
            }

            // Bottom section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 50.dp)
            ) {
                Text(
                    text = "Your Fantasy, Your Team",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFc94b8f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                IconButton(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        context.startActivity(intent)
                        activity?.finish()


                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFff1493), Color(0xFFff69b4))
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_media_play),
                        contentDescription = "Continue",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Preview()
@Composable
fun SplashScreenPreview() {
    WelcomeTheme {
        SplashScreen()
    }
}
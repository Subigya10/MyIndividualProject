package com.example.kotlinnewproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.kotlinnewproject.R

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SquadXISplash()
        }
    }
}

@Composable

fun SquadXISplash(preview: Boolean = false) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (!preview) { // only run on real device
        LaunchedEffect(Unit) {
            delay(2500)
            activity?.let {
                val prefs = context.getSharedPreferences("squadxi_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("onboarding_done", false).apply()
                val onboardingDone = prefs.getBoolean("onboarding_done", false)
                val nextScreen = if (onboardingDone) WelcomeActivity::class.java else OnboardingActivity::class.java
                context.startActivity(Intent(context, nextScreen))
                it.finish()
            }
        }
    }

    // Logo scale animation
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(1200, easing = EaseOutBack))
    }

    // Slogan fade-in
    var sloganAlpha by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        delay(1200)
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(800)
        ) { value, _ -> sloganAlpha = value }
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition()
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize().padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.blue_player),
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp)
                    .offset(y = 60.dp, x = (-30).dp)   // push a little down and left
                    .graphicsLayer(alpha = 0.15f, rotationZ = -15f)  // rotated slightly
                    .blur(radius = 4.dp)
            )


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(R.drawable.newlogo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            shadowElevation = 20f
                        }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Pick Your 11. Own the Game.",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.graphicsLayer(alpha = sloganAlpha)
                )

                Spacer(modifier = Modifier.height(40.dp))

                BouncingDots(modifier = Modifier.size(50.dp))


            }
        }
    }
}
@Composable
fun BouncingDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 150, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, delayMillis = 300, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer(translationY = dot1)
                .background(Color.White, shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer(translationY = dot2)
                .background(Color.White, shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer(translationY = dot3)
                .background(Color.White, shape = CircleShape)
        )
    }
}



@Preview
@Composable
fun previewSquadXI() {
    SquadXISplash()
}


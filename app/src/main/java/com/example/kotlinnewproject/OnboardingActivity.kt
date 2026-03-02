package com.example.kotlinnewproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnboardingScreen()
        }
    }
}

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val gradient: List<Color>
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            emoji = "⚽",
            title = "Live Scores.\nReal Time.",
            subtitle = "Follow every match as it happens. Goals, cards, and minute-by-minute updates — all live.",
            gradient = listOf(Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722))
        ),
        OnboardingPage(
            emoji = "🏆",
            title = "Build Your\nFantasy Squad",
            subtitle = "Pick your best 11 from real players. Outsmart the competition with the perfect lineup.",
            gradient = listOf(Color(0xFFc94b8f), Color(0xFF8E2DE2), Color(0xFF4A00E0))
        ),
        OnboardingPage(
            emoji = "💰",
            title = "Win Coins.\nTop the Board.",
            subtitle = "Compete in contests, earn coins, and climb the leaderboard. Your squad. Your glory.",
            gradient = listOf(Color(0xFF11998e), Color(0xFF38ef7d))
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    // Animate emoji scale
    val emojiScale = remember { Animatable(0f) }
    LaunchedEffect(pagerState.currentPage) {
        emojiScale.snapTo(0f)
        emojiScale.animateTo(1f, animationSpec = tween(500, easing = EaseOutBack))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val p = pages[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(p.gradient)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    // Big emoji
                    Text(
                        text = p.emoji,
                        fontSize = 100.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = emojiScale.value
                            scaleY = emojiScale.value
                        }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // Title
                    Text(
                        text = p.title,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Subtitle
                    Text(
                        text = p.subtitle,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dot indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Button
            val isLastPage = pagerState.currentPage == pages.size - 1

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White)
                    .clickable {
                        if (isLastPage) {
                            // Mark onboarding as done
                            val prefs = context.getSharedPreferences("squadxi_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("onboarding_done", true).apply()
                            context.startActivity(Intent(context, WelcomeActivity::class.java))
                            (context as? android.app.Activity)?.finish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLastPage) "Get Started 🚀" else "Next →",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A00E0)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Skip button (not on last page)
            if (!isLastPage) {
                Text(
                    text = "Skip",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        val prefs = context.getSharedPreferences("squadxi_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        context.startActivity(Intent(context, WelcomeActivity::class.java))
                        (context as? android.app.Activity)?.finish()
                    }
                )
            }
        }
    }
}
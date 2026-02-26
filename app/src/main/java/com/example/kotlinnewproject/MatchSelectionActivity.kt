package com.example.kotlinnewproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Match(
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val date: String,
    val competition: String,
    val sport: String = "football"
)

class MatchSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatchSelectionScreen()
        }
    }
}

@Composable
fun MatchSelectionScreen() {
    val context = LocalContext.current
    var selectedSport by remember { mutableStateOf(0) } // 0 = Football, 1 = Cricket

    var footballMatches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var cricketMatches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoadingFootball by remember { mutableStateOf(true) }
    var isLoadingCricket by remember { mutableStateOf(true) }
    var footballError by remember { mutableStateOf("") }
    var cricketError by remember { mutableStateOf("") }

    val apiKey = "ab525c6736ef4253a78343b517589979"

    // Fetch football matches from API
    LaunchedEffect(Unit) {
        isLoadingFootball = true
        try {
            val response = withContext(Dispatchers.IO) {
                FootballApi.service.getPLMatches(apiKey)
            }
            footballMatches = response.matches
                .filter { it.status == "SCHEDULED" || it.status == "TIMED" }
                .take(10)
                .map { m ->
                    Match(
                        homeTeam = m.homeTeam.shortName ?: m.homeTeam.name,
                        awayTeam = m.awayTeam.shortName ?: m.awayTeam.name,
                        homeTeamId = m.homeTeam.id,
                        awayTeamId = m.awayTeam.id,
                        date = m.utcDate.take(10),
                        competition = "Premier League",
                        sport = "football"
                    )
                }
        } catch (e: Exception) {
            footballError = "Failed to load matches"
        }
        isLoadingFootball = false
    }

    // Fetch cricket matches from Firebase — runs ONCE only
    LaunchedEffect(Unit) {
        isLoadingCricket = true
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("cricket").child("matches")

        db.get().addOnSuccessListener { snapshot ->
            val seen = mutableSetOf<String>() // track unique matches
            val matches = mutableListOf<Match>()

            snapshot.children.forEach { child ->
                val homeTeam = child.child("homeTeam").value?.toString() ?: ""
                val awayTeam = child.child("awayTeam").value?.toString() ?: ""
                val date = child.child("date").value?.toString() ?: ""
                val competition = child.child("competition").value?.toString() ?: "T20 World Cup"

                // Create a unique key for this match
                val key = "$homeTeam-$awayTeam-$date"

                // Only add if we haven't seen this match before
                if (key !in seen && homeTeam.isNotEmpty() && awayTeam.isNotEmpty()) {
                    seen.add(key)
                    matches.add(
                        Match(
                            homeTeam = homeTeam,
                            awayTeam = awayTeam,
                            homeTeamId = 0,
                            awayTeamId = 0,
                            date = date,
                            competition = competition,
                            sport = "cricket"
                        )
                    )
                }
            }
            cricketMatches = matches
            isLoadingCricket = false
        }.addOnFailureListener {
            cricketError = "Failed to load cricket matches"
            isLoadingCricket = false
        }
    }

    val currentMatches = if (selectedSport == 0) footballMatches else cricketMatches
    val isLoading = if (selectedSport == 0) isLoadingFootball else isLoadingCricket
    val errorMessage = if (selectedSport == 0) footballError else cricketError

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.iphone),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                Color.Black.copy(alpha = 0.55f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Darken
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { (context as? MatchSelectionActivity)?.finish() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = Color.White, fontSize = 18.sp)
                }
                Text(
                    "Select Match",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Box(modifier = Modifier.size(36.dp))
            }

            // SPORT TOGGLE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(4.dp)
            ) {
                listOf("⚽  Football", "🏏  Cricket").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (selectedSport == index)
                                    Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                else
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedSport = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                if (selectedSport == 0) "Premier League • Pick a match to create your team"
                else "T20 World Cup • Pick a match to create your team",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading matches...", color = Color.White, fontSize = 14.sp)
                    }
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        errorMessage,
                        color = Color(0xFFFF5F6D),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(currentMatches) { match ->
                        MatchCard2(match = match) {
                            val intent = Intent(context, CreateTeamActivity::class.java).apply {
                                putExtra("sport", match.sport)
                                putExtra("homeTeamId", match.homeTeamId)
                                putExtra("awayTeamId", match.awayTeamId)
                                putExtra("homeTeamName", match.homeTeam)
                                putExtra("awayTeamName", match.awayTeam)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MatchCard2(match: Match, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.12f))))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(match.competition, color = Color(0xFF8E2DE2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(match.date, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF8E2DE2)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(match.homeTeam.take(3).uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(match.homeTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VS", color = Color(0xFFFFE082), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Pick Team →", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(Color(0xFFE65100), Color(0xFFFF5F6D)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(match.awayTeam.take(3).uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(match.awayTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
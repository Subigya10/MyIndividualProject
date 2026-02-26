package com.example.kotlinnewproject

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

data class Player(
    val name: String,
    val club: String,
    val position: String,
    val credits: Double,
    val points: Int
)

class CreateTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sport = intent.getStringExtra("sport") ?: "football"
        val homeTeamId = intent.getIntExtra("homeTeamId", 0)
        val awayTeamId = intent.getIntExtra("awayTeamId", 0)
        val homeTeamName = intent.getStringExtra("homeTeamName") ?: ""
        val awayTeamName = intent.getStringExtra("awayTeamName") ?: ""
        setContent {
            CreateTeamScreen(
                initialSport = if (sport == "cricket") 1 else 0,
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName
            )
        }
    }
}

@Composable
fun CreateTeamScreen(
    initialSport: Int = 0,
    homeTeamId: Int = 0,
    awayTeamId: Int = 0,
    homeTeamName: String = "",
    awayTeamName: String = ""
) {
    val context = LocalContext.current
    val selectedSport = initialSport  // locked to what was passed from match selection
    var selectedPlayers by remember { mutableStateOf(setOf<String>()) }
    var selectedPosition by remember { mutableStateOf("ALL") }

    // Football API state
    var apiPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoadingFootball by remember { mutableStateOf(false) }
    var footballError by remember { mutableStateOf("") }

    // Cricket Firebase state
    var cricketPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoadingCricket by remember { mutableStateOf(false) }
    var cricketError by remember { mutableStateOf("") }

    val apiKey = "ab525c6736ef4253a78343b517589979"

    // Fetch football players
    LaunchedEffect(homeTeamId, awayTeamId) {
        if (selectedSport == 0 && (homeTeamId != 0 || awayTeamId != 0)) {
            isLoadingFootball = true
            footballError = ""
            try {
                val allPlayers = mutableListOf<Player>()
                listOf(homeTeamId, awayTeamId).forEach { teamId ->
                    if (teamId != 0) {
                        try {
                            val squad = withContext(Dispatchers.IO) {
                                FootballApi.service.getTeamSquad(apiKey, teamId)
                            }
                            val teamName = if (teamId == homeTeamId) homeTeamName else awayTeamName
                            squad.squad.forEach { p ->
                                val position = when (p.position) {
                                    "Goalkeeper" -> "GK"
                                    "Defence" -> "DEF"
                                    "Midfield" -> "MID"
                                    "Offence" -> "FWD"
                                    else -> "MID"
                                }
                                allPlayers.add(
                                    Player(
                                        name = p.name,
                                        club = teamName,
                                        position = position,
                                        credits = when (position) {
                                            "GK" -> 8.0
                                            "DEF" -> 8.5
                                            "MID" -> 10.0
                                            "FWD" -> 11.0
                                            else -> 9.0
                                        },
                                        points = (100..220).random()
                                    )
                                )
                            }
                        } catch (e: Exception) { }
                    }
                }
                apiPlayers = allPlayers
            } catch (e: Exception) {
                footballError = "Failed to load players: ${e.message}"
            }
            isLoadingFootball = false
        } else if (selectedSport == 0) {
            // Fallback: load from all PL teams
            isLoadingFootball = true
            footballError = ""
            try {
                val teams = withContext(Dispatchers.IO) {
                    FootballApi.service.getPLTeams(apiKey)
                }
                val allPlayers = mutableListOf<Player>()
                teams.teams.take(5).forEach { team ->
                    try {
                        val squad = withContext(Dispatchers.IO) {
                            FootballApi.service.getTeamSquad(apiKey, team.id)
                        }
                        squad.squad.forEach { p ->
                            val position = when (p.position) {
                                "Goalkeeper" -> "GK"
                                "Defence" -> "DEF"
                                "Midfield" -> "MID"
                                "Offence" -> "FWD"
                                else -> "MID"
                            }
                            allPlayers.add(
                                Player(
                                    name = p.name,
                                    club = team.shortName,
                                    position = position,
                                    credits = when (position) {
                                        "GK" -> 8.0
                                        "DEF" -> 8.5
                                        "MID" -> 10.0
                                        "FWD" -> 11.0
                                        else -> 9.0
                                    },
                                    points = (100..220).random()
                                )
                            )
                        }
                    } catch (e: Exception) { }
                }
                apiPlayers = allPlayers
            } catch (e: Exception) {
                footballError = "Failed to load players: ${e.message}"
            }
            isLoadingFootball = false
        }
    }

    // Fetch cricket players from Firebase for both teams
    LaunchedEffect(homeTeamName, awayTeamName) {
        if (selectedSport == 1 && homeTeamName.isNotEmpty() && awayTeamName.isNotEmpty()) {
            isLoadingCricket = true
            cricketError = ""
            try {
                val db = com.google.firebase.database.FirebaseDatabase
                    .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                    .getReference("cricket").child("squads")

                val players = mutableListOf<Player>()

                // Fetch home team
                db.child(homeTeamName).get().addOnSuccessListener { snapshot ->
                    snapshot.children.forEach { child ->
                        val raw = child.value?.toString() ?: return@forEach
                        val parts = raw.split("|")
                        if (parts.size >= 4) {
                            players.add(
                                Player(
                                    name = parts[0],
                                    club = homeTeamName,
                                    position = parts[1],
                                    credits = parts[2].toDoubleOrNull() ?: 9.0,
                                    points = parts[3].toIntOrNull() ?: 150
                                )
                            )
                        }
                    }

                    // Fetch away team
                    db.child(awayTeamName).get().addOnSuccessListener { snapshot2 ->
                        snapshot2.children.forEach { child ->
                            val raw = child.value?.toString() ?: return@forEach
                            val parts = raw.split("|")
                            if (parts.size >= 4) {
                                players.add(
                                    Player(
                                        name = parts[0],
                                        club = awayTeamName,
                                        position = parts[1],
                                        credits = parts[2].toDoubleOrNull() ?: 9.0,
                                        points = parts[3].toIntOrNull() ?: 150
                                    )
                                )
                            }
                        }
                        cricketPlayers = players
                        isLoadingCricket = false
                    }.addOnFailureListener {
                        cricketError = "Failed to load ${awayTeamName} squad"
                        isLoadingCricket = false
                    }
                }.addOnFailureListener {
                    cricketError = "Failed to load ${homeTeamName} squad"
                    isLoadingCricket = false
                }
            } catch (e: Exception) {
                cricketError = "Failed to load cricket players"
                isLoadingCricket = false
            }
        }
    }

    val footballPositions = listOf("ALL", "GK", "DEF", "MID", "FWD")
    val cricketPositions = listOf("ALL", "WK", "BAT", "AR", "BOWL")

    val currentPlayers = if (selectedSport == 0) apiPlayers else cricketPlayers
    val currentPositions = if (selectedSport == 0) footballPositions else cricketPositions
    val isLoading = if (selectedSport == 0) isLoadingFootball else isLoadingCricket
    val errorMessage = if (selectedSport == 0) footballError else cricketError

    val maxPlayers = 11
    val totalBudget = 170.0

    val selectedPlayerObjects = currentPlayers.filter { it.name in selectedPlayers }
    val usedBudget = selectedPlayerObjects.sumOf { it.credits }
    val remainingBudget = totalBudget - usedBudget
    val filteredPlayers = if (selectedPosition == "ALL") currentPlayers
    else currentPlayers.filter { it.position == selectedPosition }

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
                        .clickable { (context as? CreateTeamActivity)?.finish() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("←", color = Color.White, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Create Team",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    if (homeTeamName.isNotEmpty() && awayTeamName.isNotEmpty()) {
                        Text(
                            "$homeTeamName vs $awayTeamName",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }
                Box(modifier = Modifier.size(36.dp))
            }

            // SPORT BADGE (read-only, not a toggle)
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    if (selectedSport == 0) "⚽  Football" else "🏏  Cricket",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BUDGET & PLAYERS COUNT BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${selectedPlayers.size}/$maxPlayers",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text("Players", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${String.format("%.1f", remainingBudget)} cr",
                        color = if (remainingBudget < 10) Color(0xFFFF5F6D) else Color(0xFF38ef7d),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text("Remaining", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${String.format("%.1f", usedBudget)} cr",
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text("Used", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // POSITION FILTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentPositions.forEach { pos ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selectedPosition == pos)
                                    Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                else
                                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f)))
                            )
                            .clickable { selectedPosition = pos }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(pos, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PLAYER LIST or LOADING
            if (isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading players...", color = Color.White, fontSize = 14.sp)
                    }
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage, color = Color(0xFFFF5F6D), fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredPlayers) { player ->
                        val isSelected = player.name in selectedPlayers
                        val canAdd = !isSelected &&
                                selectedPlayers.size < maxPlayers &&
                                remainingBudget >= player.credits

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected)
                                        Brush.horizontalGradient(listOf(Color(0xFF8E2DE2).copy(alpha = 0.4f), Color(0xFF4A00E0).copy(alpha = 0.4f)))
                                    else
                                        Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.07f)))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF8E2DE2) else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        selectedPlayers = selectedPlayers - player.name
                                    } else if (canAdd) {
                                        selectedPlayers = selectedPlayers + player.name
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(positionColor(player.position))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(player.position, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(player.club, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${player.credits} cr", color = Color(0xFFFFE082), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${player.points} pts", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0xFF38ef7d)
                                            else if (canAdd) Color(0xFF8E2DE2)
                                            else Color.Gray.copy(alpha = 0.4f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (isSelected) "✓" else "+",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SAVE TEAM BUTTON
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (selectedPlayers.size == maxPlayers)
                        Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                    else
                        Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                )
                .clickable(enabled = selectedPlayers.size == maxPlayers) {
                    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val db = com.google.firebase.database.FirebaseDatabase
                        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                        .getReference("Users").child(userId).child("team")

                    val teamData = mapOf(
                        "sport" to if (selectedSport == 0) "Football" else "Cricket",
                        "players" to selectedPlayers.toList(),
                        "totalCredits" to usedBudget,
                        "savedAt" to System.currentTimeMillis()
                    )

                    db.setValue(teamData)
                        .addOnSuccessListener {
                            android.widget.Toast.makeText(context, "✅ Team saved!", android.widget.Toast.LENGTH_SHORT).show()
                            (context as? CreateTeamActivity)?.finish()
                        }
                        .addOnFailureListener {
                            android.widget.Toast.makeText(context, "❌ Failed: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (selectedPlayers.size == maxPlayers) "✅ Save Team"
                else "Select ${maxPlayers - selectedPlayers.size} more players",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

fun positionColor(position: String): Color {
    return when (position) {
        "GK", "WK" -> Color(0xFF1565C0)
        "DEF", "BAT" -> Color(0xFF2E7D32)
        "MID", "AR" -> Color(0xFF8E2DE2)
        "FWD", "BOWL" -> Color(0xFFE65100)
        else -> Color.Gray
    }
}
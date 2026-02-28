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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
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

// ─── Position limits ────────────────────────────────────────────────────────
// Football: 1 GK, 3-5 DEF, 3-5 MID, 1-3 FWD (must total 11)
// Cricket:  1 WK, 3-5 BAT, 1-4 AR, 3-5 BOWL (must total 11)
val footballLimits = mapOf("GK" to 1, "DEF" to 5, "MID" to 5, "FWD" to 3)
val footballMinimums = mapOf("GK" to 1, "DEF" to 3, "MID" to 3, "FWD" to 1)
val cricketLimits = mapOf("WK" to 1, "BAT" to 5, "AR" to 4, "BOWL" to 5)
val cricketMinimums = mapOf("WK" to 1, "BAT" to 3, "AR" to 1, "BOWL" to 3)

fun getLimit(position: String, sport: Int): Int {
    return if (sport == 0) footballLimits[position] ?: 5
    else cricketLimits[position] ?: 5
}

fun getMinimum(position: String, sport: Int): Int {
    return if (sport == 0) footballMinimums[position] ?: 1
    else cricketMinimums[position] ?: 1
}

// Check if all position minimums are met
fun meetsMinimums(selected: Set<String>, players: List<Player>, sport: Int): Boolean {
    val selectedPlayers = players.filter { it.name in selected }
    val mins = if (sport == 0) footballMinimums else cricketMinimums
    return mins.all { (pos, min) ->
        selectedPlayers.count { it.position == pos } >= min
    }
}

class CreateTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sport = intent.getStringExtra("sport") ?: "football"
        val homeTeamId = intent.getIntExtra("homeTeamId", 0)
        val awayTeamId = intent.getIntExtra("awayTeamId", 0)
        val homeTeamName = intent.getStringExtra("homeTeamName") ?: ""
        val awayTeamName = intent.getStringExtra("awayTeamName") ?: ""
        val editMode = intent.getBooleanExtra("editMode", false)
        val editSport = intent.getStringExtra("editSport") ?: ""
        setContent {
            CreateTeamScreen(
                initialSport = when {
                    editSport == "Cricket" -> 1
                    editSport == "Football" -> 0
                    sport == "cricket" -> 1
                    else -> 0
                },
                homeTeamId = homeTeamId,
                awayTeamId = awayTeamId,
                homeTeamName = homeTeamName,
                awayTeamName = awayTeamName,
                editMode = editMode,
                editSport = editSport
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
    awayTeamName: String = "",
    editMode: Boolean = false,
    editSport: String = ""
) {
    val context = LocalContext.current
    val selectedSport = initialSport
    var selectedPosition by remember { mutableStateOf("ALL") }

    var preSelectedPlayers by remember { mutableStateOf(setOf<String>()) }
    var selectedPlayers by remember { mutableStateOf(setOf<String>()) }
    var preLoadDone by remember { mutableStateOf(!editMode) }

    // ── Captain & Vice-Captain ──────────────────────────────────────────────
    var captainName by remember { mutableStateOf("") }       // 2x points
    var viceCaptainName by remember { mutableStateOf("") }   // 1.5x points
    var showCaptainPicker by remember { mutableStateOf(false) }  // show C/VC picker step

    // Football API state
    var apiPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoadingFootball by remember { mutableStateOf(false) }
    var footballError by remember { mutableStateOf("") }

    // Cricket Firebase state
    var cricketPlayers by remember { mutableStateOf<List<Player>>(emptyList()) }
    var isLoadingCricket by remember { mutableStateOf(false) }
    var cricketError by remember { mutableStateOf("") }

    val apiKey = "ab525c6736ef4253a78343b517589979"

    // Step 1: Load previously saved players (edit mode)
    LaunchedEffect(editMode) {
        if (editMode && editSport.isNotEmpty()) {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val sportNode = if (editSport == "Cricket") "cricketTeam" else "footballTeam"
            com.google.firebase.database.FirebaseDatabase
                .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                .getReference("Users").child(userId).child(sportNode)
                .get().addOnSuccessListener { snapshot ->
                    val existing = mutableSetOf<String>()
                    snapshot.child("players").children.forEach {
                        existing.add(it.value?.toString() ?: "")
                    }
                    preSelectedPlayers = existing
                    selectedPlayers = existing
                    captainName = snapshot.child("captain").value?.toString() ?: ""
                    viceCaptainName = snapshot.child("viceCaptain").value?.toString() ?: ""
                    preLoadDone = true
                }
        }
    }

    // Step 2: Fetch football players
    LaunchedEffect(homeTeamId, awayTeamId, preLoadDone) {
        if (!preLoadDone) return@LaunchedEffect
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
                                allPlayers.add(Player(
                                    name = p.name, club = teamName, position = position,
                                    credits = when (position) { "GK" -> 8.0; "DEF" -> 8.5; "MID" -> 10.0; "FWD" -> 11.0; else -> 9.0 },
                                    points = (100..220).random()
                                ))
                            }
                        } catch (e: Exception) {}
                    }
                }
                apiPlayers = allPlayers
            } catch (e: Exception) { footballError = "Failed to load players: ${e.message}" }
            isLoadingFootball = false
        } else if (selectedSport == 0) {
            isLoadingFootball = true
            footballError = ""
            try {
                val teams = withContext(Dispatchers.IO) { FootballApi.service.getPLTeams(apiKey) }
                val allPlayers = mutableListOf<Player>()
                teams.teams.take(5).forEach { team ->
                    try {
                        val squad = withContext(Dispatchers.IO) { FootballApi.service.getTeamSquad(apiKey, team.id) }
                        squad.squad.forEach { p ->
                            val position = when (p.position) {
                                "Goalkeeper" -> "GK"; "Defence" -> "DEF"; "Midfield" -> "MID"; "Offence" -> "FWD"; else -> "MID"
                            }
                            allPlayers.add(Player(
                                name = p.name, club = team.shortName, position = position,
                                credits = when (position) { "GK" -> 8.0; "DEF" -> 8.5; "MID" -> 10.0; "FWD" -> 11.0; else -> 9.0 },
                                points = (100..220).random()
                            ))
                        }
                    } catch (e: Exception) {}
                }
                apiPlayers = allPlayers
            } catch (e: Exception) { footballError = "Failed to load players: ${e.message}" }
            isLoadingFootball = false
        }
    }

    // Step 2 (cricket): Fetch cricket players
    LaunchedEffect(homeTeamName, awayTeamName, preLoadDone) {
        if (!preLoadDone) return@LaunchedEffect
        if (selectedSport == 1 && homeTeamName.isNotEmpty() && awayTeamName.isNotEmpty()) {
            isLoadingCricket = true
            cricketError = ""
            try {
                val db = com.google.firebase.database.FirebaseDatabase
                    .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                    .getReference("cricket").child("squads")
                val players = mutableListOf<Player>()
                db.child(homeTeamName).get().addOnSuccessListener { snapshot ->
                    snapshot.children.forEach { child ->
                        val raw = child.value?.toString() ?: return@forEach
                        val parts = raw.split("|")
                        if (parts.size >= 4) {
                            players.add(Player(name = parts[0], club = homeTeamName, position = parts[1], credits = parts[2].toDoubleOrNull() ?: 9.0, points = parts[3].toIntOrNull() ?: 150))
                        }
                    }
                    db.child(awayTeamName).get().addOnSuccessListener { snapshot2 ->
                        snapshot2.children.forEach { child ->
                            val raw = child.value?.toString() ?: return@forEach
                            val parts = raw.split("|")
                            if (parts.size >= 4) {
                                players.add(Player(name = parts[0], club = awayTeamName, position = parts[1], credits = parts[2].toDoubleOrNull() ?: 9.0, points = parts[3].toIntOrNull() ?: 150))
                            }
                        }
                        cricketPlayers = players
                        isLoadingCricket = false
                    }.addOnFailureListener { cricketError = "Failed to load $awayTeamName squad"; isLoadingCricket = false }
                }.addOnFailureListener { cricketError = "Failed to load $homeTeamName squad"; isLoadingCricket = false }
            } catch (e: Exception) { cricketError = "Failed to load cricket players"; isLoadingCricket = false }
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

    // Position count helpers
    fun countByPosition(pos: String) = selectedPlayerObjects.count { it.position == pos }
    fun canAddPosition(pos: String) = countByPosition(pos) < getLimit(pos, selectedSport)

    val teamComplete = selectedPlayers.size == maxPlayers && meetsMinimums(selectedPlayers, currentPlayers, selectedSport)
    val captainSet = captainName.isNotEmpty() && viceCaptainName.isNotEmpty() && captainName != viceCaptainName

    // ── Captain Picker Screen ───────────────────────────────────────────────
    if (showCaptainPicker) {
        CaptainPickerScreen(
            selectedPlayers = selectedPlayerObjects,
            captainName = captainName,
            viceCaptainName = viceCaptainName,
            onCaptainSelected = { captainName = it },
            onViceCaptainSelected = { viceCaptainName = it },
            onConfirm = {
                showCaptainPicker = false
                // ── SAVE TEAM ──────────────────────────────────────────
                val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val sportNode = if (selectedSport == 0) "footballTeam" else "cricketTeam"
                val db = com.google.firebase.database.FirebaseDatabase
                    .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                    .getReference("Users").child(userId).child(sportNode)

                // Build player data map for scoring: name -> "position|points"
                val playerDataMap = selectedPlayerObjects.associate { p ->
                    p.name to "${p.position}|${p.points}"
                }

                val teamData = mapOf(
                    "sport" to if (selectedSport == 0) "Football" else "Cricket",
                    "players" to selectedPlayers.toList(),
                    "playerData" to playerDataMap,   // ← NEW: position+points for real scoring
                    "captain" to captainName,         // ← NEW: captain (2x points)
                    "viceCaptain" to viceCaptainName, // ← NEW: vice-captain (1.5x points)
                    "totalCredits" to usedBudget,
                    "savedAt" to System.currentTimeMillis(),
                    "homeTeamName" to homeTeamName,
                    "awayTeamName" to awayTeamName,
                    "homeTeamId" to homeTeamId,
                    "awayTeamId" to awayTeamId
                )

                db.setValue(teamData)
                    .addOnSuccessListener {
                        android.widget.Toast.makeText(context, "✅ Team saved!", android.widget.Toast.LENGTH_SHORT).show()
                        (context as? CreateTeamActivity)?.finish()
                    }
                    .addOnFailureListener {
                        android.widget.Toast.makeText(context, "❌ Failed: ${it.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
            },
            onBack = { showCaptainPicker = false }
        )
        return
    }

    // ── Main Team Builder UI ────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.iphone), contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black.copy(alpha = 0.55f), blendMode = androidx.compose.ui.graphics.BlendMode.Darken)
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                        .clickable { (context as? CreateTeamActivity)?.finish() },
                    contentAlignment = Alignment.Center
                ) { Text("←", color = Color.White, fontSize = 18.sp) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (editMode) "Edit Team" else "Create Team", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (homeTeamName.isNotEmpty() && awayTeamName.isNotEmpty())
                        Text("$homeTeamName vs $awayTeamName", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.size(36.dp))
            }

            // SPORT BADGE
            Box(
                modifier = Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) { Text(if (selectedSport == 0) "⚽  Football" else "🏏  Cricket", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }

            Spacer(modifier = Modifier.height(12.dp))

            // BUDGET + PLAYER COUNT BAR
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.1f)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${selectedPlayers.size}/$maxPlayers", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Players", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${String.format("%.1f", remainingBudget)} cr",
                        color = if (remainingBudget < 10) Color(0xFFFF5F6D) else Color(0xFF38ef7d),
                        fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Remaining", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.2f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${String.format("%.1f", usedBudget)} cr", color = Color(0xFFFFE082), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Used", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // POSITION LIMIT INDICATORS
            val positions = if (selectedSport == 0) listOf("GK", "DEF", "MID", "FWD") else listOf("WK", "BAT", "AR", "BOWL")
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(positions) { pos ->
                    val count = countByPosition(pos)
                    val limit = getLimit(pos, selectedSport)
                    val min = getMinimum(pos, selectedSport)
                    val meetsMin = count >= min
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    count == limit -> Color(0xFF8E2DE2).copy(alpha = 0.3f)
                                    meetsMin -> Color(0xFF38ef7d).copy(alpha = 0.15f)
                                    else -> Color.White.copy(alpha = 0.08f)
                                }
                            )
                            .border(1.dp,
                                when {
                                    count == limit -> Color(0xFF8E2DE2).copy(alpha = 0.6f)
                                    meetsMin -> Color(0xFF38ef7d).copy(alpha = 0.4f)
                                    else -> Color.White.copy(alpha = 0.15f)
                                }, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(pos, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$count/$limit", color = when {
                                count == limit -> Color(0xFF8E2DE2)
                                meetsMin -> Color(0xFF38ef7d)
                                else -> Color.White.copy(alpha = 0.5f)
                            }, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            Text("min $min", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // POSITION FILTER TABS
            LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentPositions) { pos ->
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (selectedPosition == pos) Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))) else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.1f))))
                            .clickable { selectedPosition = pos }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) { Text(pos, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading || !preLoadDone) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading players...", color = Color.White, fontSize = 14.sp)
                    }
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = Color(0xFFFF5F6D), fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(filteredPlayers) { player ->
                        val isSelected = player.name in selectedPlayers
                        val positionFull = !canAddPosition(player.position) && !isSelected
                        val canAdd = !isSelected && selectedPlayers.size < maxPlayers
                                && remainingBudget >= player.credits && !positionFull
                        val isCapt = player.name == captainName
                        val isVC = player.name == viceCaptainName

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isCapt -> Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.25f), Color(0xFFFFC371).copy(alpha = 0.25f)))
                                        isVC -> Brush.horizontalGradient(listOf(Color(0xFFB0BEC5).copy(alpha = 0.25f), Color(0xFF78909C).copy(alpha = 0.25f)))
                                        isSelected -> Brush.horizontalGradient(listOf(Color(0xFF8E2DE2).copy(alpha = 0.35f), Color(0xFF4A00E0).copy(alpha = 0.35f)))
                                        else -> Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.07f)))
                                    }
                                )
                                .border(1.dp,
                                    when {
                                        isCapt -> Color(0xFFFFD700).copy(alpha = 0.7f)
                                        isVC -> Color(0xFFB0BEC5).copy(alpha = 0.7f)
                                        isSelected -> Color(0xFF8E2DE2)
                                        positionFull -> Color(0xFFFF5F6D).copy(alpha = 0.3f)
                                        else -> Color.White.copy(alpha = 0.1f)
                                    }, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isSelected) {
                                        selectedPlayers = selectedPlayers - player.name
                                        if (captainName == player.name) captainName = ""
                                        if (viceCaptainName == player.name) viceCaptainName = ""
                                    } else if (canAdd) {
                                        selectedPlayers = selectedPlayers + player.name
                                    }
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Position badge
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(positionColor(player.position))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) { Text(player.position, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }

                                // C / VC badge
                                if (isCapt || isVC) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier.clip(CircleShape)
                                            .background(if (isCapt) Color(0xFFFFD700) else Color(0xFFB0BEC5))
                                            .size(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) { Text(if (isCapt) "C" else "V", color = Color(0xFF1A1A2E), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold) }
                                }

                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(player.club, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                        if (positionFull && !isSelected) {
                                            Text("• Max reached", color = Color(0xFFFF5F6D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${player.credits} cr", color = Color(0xFFFFE082), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("${player.points} pts", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                }
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(
                                        when {
                                            isSelected -> Color(0xFF38ef7d)
                                            canAdd -> Color(0xFF8E2DE2)
                                            else -> Color.Gray.copy(alpha = 0.4f)
                                        }
                                    ),
                                    contentAlignment = Alignment.Center
                                ) { Text(if (isSelected) "✓" else "+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
                            }
                        }
                    }
                }
            }
        }

        // BOTTOM BUTTON — shows validation hints, then proceeds to C/VC picker
        val mins = if (selectedSport == 0) footballMinimums else cricketMinimums
        val missingPositions = mins.filter { (pos, min) ->
            val count = selectedPlayerObjects.count { it.position == pos }
            count < min
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .navigationBarsPadding().padding(16.dp).clip(RoundedCornerShape(14.dp))
                .background(
                    if (teamComplete) Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                    else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                )
                .clickable(enabled = teamComplete) { showCaptainPicker = true }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when {
                    selectedPlayers.size < maxPlayers && missingPositions.isEmpty() ->
                        "Select ${maxPlayers - selectedPlayers.size} more players"
                    missingPositions.isNotEmpty() -> {
                        val missing = missingPositions.entries.joinToString(", ") { (pos, min) ->
                            val have = selectedPlayerObjects.count { it.position == pos }
                            "min $min $pos (have $have)"
                        }
                        "Need: $missing"
                    }
                    teamComplete -> "✅ Pick Captain & Vice-Captain →"
                    else -> "Select ${maxPlayers - selectedPlayers.size} more players"
                },
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Captain Picker Screen ──────────────────────────────────────────────────
@Composable
fun CaptainPickerScreen(
    selectedPlayers: List<Player>,
    captainName: String,
    viceCaptainName: String,
    onCaptainSelected: (String) -> Unit,
    onViceCaptainSelected: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val bothPicked = captainName.isNotEmpty() && viceCaptainName.isNotEmpty() && captainName != viceCaptainName

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.iphone), contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black.copy(alpha = 0.55f), blendMode = androidx.compose.ui.graphics.BlendMode.Darken)
        )

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) { Text("←", color = Color.White, fontSize = 18.sp) }
                Text("Pick Captain & VC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Box(modifier = Modifier.size(36.dp))
            }

            // Info box
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8E2DE2).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF8E2DE2).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("👑 Captain (C) — earns 2x points", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("⭐ Vice-Captain (VC) — earns 1.5x points", color = Color(0xFFB0BEC5), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Tap once to set Captain, tap again to set Vice-Captain", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            // Current picks
            if (captainName.isNotEmpty() || viceCaptainName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (captainName.isNotEmpty()) {
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)) {
                            Column {
                                Text("👑 Captain", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(captainName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (viceCaptainName.isNotEmpty()) {
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFB0BEC5).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFB0BEC5).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(10.dp)) {
                            Column {
                                Text("⭐ Vice-Captain", color = Color(0xFFB0BEC5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(viceCaptainName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player list — sorted by points descending (best picks first)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    bottom = 90.dp,
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp
                )
            ) {
                items(selectedPlayers.sortedByDescending { it.points }) { player ->
                    val isCapt = player.name == captainName
                    val isVC = player.name == viceCaptainName

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(when {
                                isCapt -> Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.25f), Color(0xFFFFC371).copy(alpha = 0.2f)))
                                isVC -> Brush.horizontalGradient(listOf(Color(0xFFB0BEC5).copy(alpha = 0.2f), Color(0xFF78909C).copy(alpha = 0.15f)))
                                else -> Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.07f)))
                            })
                            .border(1.dp, when {
                                isCapt -> Color(0xFFFFD700).copy(alpha = 0.7f)
                                isVC -> Color(0xFFB0BEC5).copy(alpha = 0.7f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }, RoundedCornerShape(12.dp))
                            .clickable {
                                when {
                                    // If already captain → deselect
                                    isCapt -> onCaptainSelected("")
                                    // If already VC → deselect
                                    isVC -> onViceCaptainSelected("")
                                    // No captain set → set as captain
                                    captainName.isEmpty() -> onCaptainSelected(player.name)
                                    // Captain set, no VC → set as VC
                                    viceCaptainName.isEmpty() -> onViceCaptainSelected(player.name)
                                    // Both set → replace captain
                                    else -> onCaptainSelected(player.name)
                                }
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(positionColor(player.position)).padding(horizontal = 6.dp, vertical = 3.dp)
                            ) { Text(player.position, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                            Column {
                                Text(player.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(player.club, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${player.points} pts", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    when { isCapt -> "2x = ${(player.points * 2)}"; isVC -> "1.5x = ${(player.points * 1.5).toInt()}"; else -> "" },
                                    color = if (isCapt) Color(0xFFFFD700) else Color(0xFFB0BEC5), fontSize = 10.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            // C / VC badge
                            Box(
                                modifier = Modifier.size(32.dp).clip(CircleShape).background(
                                    when { isCapt -> Color(0xFFFFD700); isVC -> Color(0xFFB0BEC5); else -> Color.White.copy(alpha = 0.15f) }
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when { isCapt -> "C"; isVC -> "V"; else -> "+" },
                                    color = when { isCapt || isVC -> Color(0xFF1A1A2E); else -> Color.White },
                                    fontSize = 14.sp, fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirm button
        Box(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .navigationBarsPadding().padding(16.dp).clip(RoundedCornerShape(14.dp))
                .background(
                    if (bothPicked) Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
                    else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                )
                .clickable(enabled = bothPicked) { onConfirm() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (bothPicked) "✅ Save Team" else "Pick both Captain & Vice-Captain",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
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
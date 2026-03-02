package com.example.kotlinnewproject

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
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

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userName = intent.getStringExtra("userName") ?: "Player"
        val userEmail = intent.getStringExtra("userEmail") ?: ""

        setContent {
            DashboardScreen(userName = userName, userEmail = userEmail)
        }
    }
}

@Composable
fun DashboardScreen(userName: String = "Player", userEmail: String = "") {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var footballTeamPlayers by remember { mutableStateOf<List<String>>(emptyList()) }
    var cricketTeamPlayers by remember { mutableStateOf<List<String>>(emptyList()) }
    var userCoins by remember { mutableStateOf(1250) }

    // Start live score polling
    LaunchedEffect(Unit) {
        LiveScoreService.startPolling(scope)
    }

    DisposableEffect(Unit) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userRef = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("Users").child(userId)

        val footballListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val players = mutableListOf<String>()
                snapshot.child("players").children.forEach { players.add(it.value?.toString() ?: "") }
                footballTeamPlayers = players
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        val cricketListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val players = mutableListOf<String>()
                snapshot.child("players").children.forEach { players.add(it.value?.toString() ?: "") }
                cricketTeamPlayers = players
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        val coinsListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                userCoins = (snapshot.value as? Long)?.toInt() ?: 1250
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        userRef.child("footballTeam").addValueEventListener(footballListener)
        userRef.child("cricketTeam").addValueEventListener(cricketListener)
        userRef.child("coins").addValueEventListener(coinsListener)
        userRef.child("coins").get().addOnSuccessListener { snap ->
            if (!snap.exists()) userRef.child("coins").setValue(1250)
        }

        onDispose {
            userRef.child("footballTeam").removeEventListener(footballListener)
            userRef.child("cricketTeam").removeEventListener(cricketListener)
            userRef.child("coins").removeEventListener(coinsListener)
            LiveScoreService.stopPolling()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1A1A2E), modifier = Modifier.height(80.dp)) {
                val tabs = listOf("Home", "Leagues", "Alerts", "Profile")
                val icons = listOf(
                    R.drawable.outline_add_home_24,
                    R.drawable.baseline_search_24,
                    R.drawable.baseline_notifications_24,
                    R.drawable.baseline_person_24
                )
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(painter = painterResource(icons[index]), contentDescription = title, modifier = Modifier.size(26.dp)) },
                        label = { Text(title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF8E2DE2), selectedTextColor = Color(0xFF8E2DE2),
                            unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF8E2DE2).copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.iphone), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    Color.Black.copy(alpha = 0.55f),
                    blendMode = androidx.compose.ui.graphics.BlendMode.Darken
                )
            )
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (selectedTab) {
                    0 -> HomeContent(
                        footballTeamPlayers = footballTeamPlayers,
                        cricketTeamPlayers = cricketTeamPlayers,
                        userCoins = userCoins
                    )
                    1 -> LeaguesContent()
                    2 -> AlertsContent()
                    3 -> ProfileContent(userName = userName, userEmail = userEmail, userCoins = userCoins)
                }
            }
        }
    }
}

// ─── HOME ─────────────────────────────────────────────────────────────────────

@Composable
fun HomeContent(
    footballTeamPlayers: List<String>,
    cricketTeamPlayers: List<String>,
    userCoins: Int = 1250
) {
    val context = LocalContext.current
    var selectedSport by remember { mutableStateOf(0) }

    var footballMatches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var cricketMatches by remember { mutableStateOf<List<Match>>(emptyList()) }
    var isLoadingFootball by remember { mutableStateOf(true) }
    var isLoadingCricket by remember { mutableStateOf(true) }

    // ── LIVE SCORES from Firebase ─────────────────────────────────────────────
    var liveMatches by remember { mutableStateOf<Map<String, Map<String, Any>>>(
        mapOf("9999" to mapOf(
            "homeTeam" to "Bourne",
            "awayTeam" to "Brentf",
            "homeScore" to 2,
            "awayScore" to 1,
            "minute" to 67,
            "status" to "IN_PLAY"
        ))
    ) }

    DisposableEffect(Unit) {
        val liveRef = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("liveMatches")

        val liveListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (!snapshot.hasChildren()) return  // ADD THIS - don't overwrite if Firebase is empty
                val map = mutableMapOf<String, Map<String, Any>>()
                snapshot.children.forEach { child ->
                    map[child.key ?: ""] = mapOf(
                        "homeTeam"  to (child.child("homeTeam").value?.toString() ?: ""),
                        "awayTeam"  to (child.child("awayTeam").value?.toString() ?: ""),
                        "homeScore" to ((child.child("homeScore").value as? Long)?.toInt() ?: 0),
                        "awayScore" to ((child.child("awayScore").value as? Long)?.toInt() ?: 0),
                        "minute"    to ((child.child("minute").value as? Long)?.toInt() ?: 0),
                        "status"    to (child.child("status").value?.toString() ?: "")
                    )
                }
                liveMatches = map
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        liveRef.addValueEventListener(liveListener)
        onDispose { liveRef.removeEventListener(liveListener) }
    }

    val apiKey = "ab525c6736ef4253a78343b517589979"

    LaunchedEffect(Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                FootballApi.service.getPLMatches(apiKey)
            }
            footballMatches = response.matches
                .filter { it.status == "SCHEDULED" || it.status == "TIMED" }
                .take(8)
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
            footballMatches = emptyList()
        }
        isLoadingFootball = false
    }

    LaunchedEffect(Unit) {
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("cricket").child("matches")
        db.get().addOnSuccessListener { snapshot ->
            val seen = mutableSetOf<String>()
            val matches = mutableListOf<Match>()
            snapshot.children.forEach { child ->
                val homeTeam = child.child("homeTeam").value?.toString() ?: ""
                val awayTeam = child.child("awayTeam").value?.toString() ?: ""
                val date = child.child("date").value?.toString() ?: ""
                val competition = child.child("competition").value?.toString() ?: "T20 World Cup"
                val key = "$homeTeam-$awayTeam-$date"
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
        }.addOnFailureListener { isLoadingCricket = false }
    }

    val currentTeamPlayers = if (selectedSport == 0) footballTeamPlayers else cricketTeamPlayers
    val currentSportLabel = if (selectedSport == 0) "Football" else "Cricket"
    val currentMatches = if (selectedSport == 0) footballMatches else cricketMatches
    val isLoading = if (selectedSport == 0) isLoadingFootball else isLoadingCricket

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }

        // ── Header ────────────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_key_24), "trophy", tint = Color(0xFFFFD700), modifier = Modifier.size(28.dp))
                    Text(" Fantasy Sports", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(painterResource(R.drawable.baseline_key_24), null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Text(" $userCoins", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray))
                }
            }
        }

        // ── Sport Toggle ──────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
        }

        // ── My Team Card ──────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF8E2DE2))))
                    .padding(16.dp)
            ) {
                Column {
                    Text("My $currentSportLabel Team", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (currentTeamPlayers.isEmpty()) {
                        Text("No team saved yet!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tap Create Team to get started", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    } else {
                        Text(
                            "$currentSportLabel • ${currentTeamPlayers.size} Players",
                            color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentTeamPlayers.joinToString(", "), color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable {
                                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        val sportNode = if (selectedSport == 0) "footballTeam" else "cricketTeam"
                                        com.google.firebase.database.FirebaseDatabase
                                            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                            .getReference("Users").child(userId).child(sportNode)
                                            .get().addOnSuccessListener { snapshot ->
                                                val intent = Intent(context, CreateTeamActivity::class.java).apply {
                                                    putExtra("sport", if (selectedSport == 0) "football" else "cricket")
                                                    putExtra("homeTeamName", snapshot.child("homeTeamName").value?.toString() ?: "")
                                                    putExtra("awayTeamName", snapshot.child("awayTeamName").value?.toString() ?: "")
                                                    putExtra("homeTeamId", (snapshot.child("homeTeamId").value as? Long)?.toInt() ?: 0)
                                                    putExtra("awayTeamId", (snapshot.child("awayTeamId").value as? Long)?.toInt() ?: 0)
                                                    putExtra("editMode", true)
                                                    putExtra("editSport", currentSportLabel)
                                                }
                                                context.startActivity(intent)
                                            }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("✏️ Edit Team", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF5F6D).copy(alpha = 0.7f))
                                    .clickable {
                                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        val sportNode = if (selectedSport == 0) "footballTeam" else "cricketTeam"
                                        com.google.firebase.database.FirebaseDatabase
                                            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                            .getReference("Users").child(userId).child(sportNode).removeValue()
                                        android.widget.Toast.makeText(context, "🗑️ Team deleted!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("🗑️ Delete", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ── Action Cards ──────────────────────────────────────────────────────
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BigActionCard(
                    "Create Team", "Build Your Squad",
                    listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)),
                    Modifier.weight(1f)
                ) { context.startActivity(Intent(context, MatchSelectionActivity::class.java)) }

                BigActionCard(
                    "Join Contest", "Enter & Compete",
                    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                    Modifier.weight(1f)
                ) {
                    context.startActivity(
                        Intent(context, ContestActivity::class.java).apply {
                            putExtra("sport", currentSportLabel)
                        }
                    )
                }
            }
        }

        // ── Upcoming / Live Matches ───────────────────────────────────────────
        item {
            val liveCount = if (selectedSport == 0) liveMatches.size else 0
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (selectedSport == 0) "⚽ Upcoming Matches" else "🏏 Upcoming Matches",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
                if (selectedSport == 0 && liveCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF3D00))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("$liveCount LIVE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        item {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(color = Color(0xFF8E2DE2), modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Loading matches...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                }
            } else if (currentMatches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.07f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matches available", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(currentMatches) { match ->

                        // ── Check if this match is LIVE ───────────────────────
                        val liveData = if (selectedSport == 0) {
                            liveMatches.values.firstOrNull { data ->
                                val lh = (data["homeTeam"] as? String)?.lowercase() ?: ""
                                val la = (data["awayTeam"] as? String)?.lowercase() ?: ""
                                val mh = match.homeTeam.lowercase()
                                val ma = match.awayTeam.lowercase()
                                lh == mh || la == ma || mh.contains(lh) || lh.contains(mh)
                            }
                        } else null
                        val isLive = liveData != null

                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .height(if (isLive) 140.dp else 120.dp)
                                .clickable {
                                    context.startActivity(
                                        Intent(context, CreateTeamActivity::class.java).apply {
                                            putExtra("sport", match.sport)
                                            putExtra("homeTeamId", match.homeTeamId)
                                            putExtra("awayTeamId", match.awayTeamId)
                                            putExtra("homeTeamName", match.homeTeam)
                                            putExtra("awayTeamName", match.awayTeam)
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isLive)
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFF1B5E20).copy(alpha = 0.5f),
                                                    Color(0xFF388E3C).copy(alpha = 0.4f)
                                                )
                                            )
                                        else
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.08f),
                                                    Color.White.copy(alpha = 0.13f)
                                                )
                                            )
                                    )
                                    .border(
                                        1.dp,
                                        if (isLive) Color(0xFF38ef7d).copy(alpha = 0.6f)
                                        else Color.White.copy(alpha = 0.18f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Top row — competition + live badge or date
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            match.competition,
                                            color = Color(0xFF8E2DE2),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isLive) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFFF3D00))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "● LIVE ${liveData?.get("minute")}′",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        } else {
                                            Text(
                                                match.date,
                                                color = Color.White.copy(alpha = 0.4f),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    // Middle row — teams + score or VS
                                    if (isLive) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    match.homeTeam.take(6),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    "${liveData?.get("homeScore")} - ${liveData?.get("awayScore")}",
                                                    color = Color(0xFF38ef7d),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 20.sp
                                                )
                                                Text(
                                                    match.awayTeam.take(6),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(match.homeTeam.take(6), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                            Text("VS", color = Color(0xFFFFE082), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                            Text(match.awayTeam.take(6), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                        }
                                    }

                                    // Bottom CTA button
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                                                )
                                            )
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (isLive) "🔥 Live — Pick Team →" else "Tap to Pick Team →",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ─── LEAGUES ──────────────────────────────────────────────────────────────────

data class JoinedContest(
    val contestId: String, val name: String, val sport: String, val matchName: String,
    val myScore: Int, val opponentScore: Int, val prizeCoins: Int, val entryCoins: Int,
    val isResolved: Boolean, val iWon: Boolean, val waitingForOpponent: Boolean
)

@Composable
fun LeaguesContent() {
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var joinedContests by remember { mutableStateOf<List<JoinedContest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedSport by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("contests")
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<JoinedContest>()
                snapshot.children.forEach { child ->
                    val joinedUsers = child.child("joinedUsers")
                    if (joinedUsers.child(userId).exists()) {
                        val isResolved = child.child("result").child("resolved").value as? Boolean ?: false
                        val winnerId = child.child("result").child("winnerId").value?.toString() ?: ""
                        val myScore = (child.child("result").child("scores").child(userId).value as? Long)?.toInt() ?: 0
                        val opponentId = joinedUsers.children.firstOrNull { it.key != userId }?.key ?: ""
                        val opponentScore = (child.child("result").child("scores").child(opponentId).value as? Long)?.toInt() ?: 0
                        list.add(
                            JoinedContest(
                                child.key ?: "",
                                child.child("name").value?.toString() ?: "",
                                child.child("sport").value?.toString() ?: "",
                                child.child("matchName").value?.toString() ?: "",
                                myScore, opponentScore,
                                (child.child("prizeCoins").value as? Long)?.toInt() ?: 0,
                                (child.child("entryCoins").value as? Long)?.toInt() ?: 0,
                                isResolved,
                                isResolved && winnerId == userId,
                                !isResolved && joinedUsers.childrenCount < 2
                            )
                        )
                    }
                }
                joinedContests = list.sortedByDescending { it.isResolved }
                isLoading = false
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) { isLoading = false }
        }
        db.addValueEventListener(listener)
        onDispose { db.removeEventListener(listener) }
    }

    val filtered = joinedContests.filter {
        if (selectedSport == 0) it.sport == "Football" else it.sport == "Cricket"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🏆 My Contests", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                if (!isLoading) Text("${joinedContests.size} total", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50.dp))
                    .background(Color.White.copy(alpha = 0.1f)).padding(4.dp)
            ) {
                listOf("⚽  Football", "🏏  Cricket").forEachIndexed { index, label ->
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(50.dp))
                            .background(
                                if (selectedSport == index)
                                    Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                else
                                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            )
                            .clickable { selectedSport = index }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8E2DE2))
                }
            }
        } else if (filtered.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No contests joined yet!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Go to Home and join a contest!", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(filtered) { contest -> JoinedContestCard(contest = contest) }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun JoinedContestCard(contest: JoinedContest) {
    val statusColor = when {
        contest.waitingForOpponent -> Color(0xFFFFE082)
        contest.iWon -> Color(0xFF38ef7d)
        contest.isResolved -> Color(0xFFFF5F6D)
        else -> Color(0xFFFFE082)
    }
    val statusText = when {
        contest.waitingForOpponent -> "⏳ Waiting for opponent"
        contest.iWon -> "🏆 You Won!"
        contest.isResolved -> "😔 You Lost"
        else -> "🎮 In Progress"
    }

    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.12f))))
            .border(
                1.dp,
                if (contest.iWon) Color(0xFF38ef7d).copy(alpha = 0.4f)
                else if (contest.isResolved && !contest.iWon) Color(0xFFFF5F6D).copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.15f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(contest.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(contest.matchName, color = Color(0xFF8E2DE2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            if (contest.isResolved) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Score", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("${contest.myScore}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VS", color = Color(0xFFFFE082), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Opponent", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text("${contest.opponentScore}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (contest.iWon) Color(0xFF38ef7d).copy(alpha = 0.15f)
                            else Color(0xFFFF5F6D).copy(alpha = 0.15f)
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (contest.iWon) "🎉 Won +${contest.prizeCoins} coins!"
                        else "Entry: ${contest.entryCoins} coins",
                        color = if (contest.iWon) Color(0xFF38ef7d) else Color(0xFFFF5F6D),
                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                    )
                }
            } else if (contest.waitingForOpponent) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Entry paid: 🔑 ${contest.entryCoins} • Prize pool: 🏆 ${contest.prizeCoins}",
                    color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp
                )
            }
        }
    }
}

// ─── PROFILE ──────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(userName: String = "Player", userEmail: String = "", userCoins: Int = 1250) {
    val context = LocalContext.current
    val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var contestsPlayed by remember { mutableStateOf(0) }
    var contestsWon by remember { mutableStateOf(0) }
    var bestScore by remember { mutableStateOf(0) }
    var footballContestsPlayed by remember { mutableStateOf(0) }
    var cricketContestsPlayed by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var activeSheet by remember { mutableStateOf<String?>(null) }
    var notifContests by remember { mutableStateOf(true) }
    var notifResults by remember { mutableStateOf(true) }
    var notifPromos by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf("Dark") }
    var analyticsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("contests")
        db.get().addOnSuccessListener { snapshot ->
            var played = 0; var won = 0; var best = 0
            var footballPlayed = 0; var cricketPlayed = 0
            snapshot.children.forEach { child ->
                val joinedUsers = child.child("joinedUsers")
                if (joinedUsers.child(userId).exists()) {
                    played++
                    val sport = child.child("sport").value?.toString() ?: ""
                    if (sport == "Football") footballPlayed++ else cricketPlayed++
                    val isResolved = child.child("result").child("resolved").value as? Boolean ?: false
                    val winnerId = child.child("result").child("winnerId").value?.toString() ?: ""
                    if (isResolved && winnerId == userId) won++
                    val myScore = (child.child("result").child("scores").child(userId).value as? Long)?.toInt() ?: 0
                    if (myScore > best) best = myScore
                }
            }
            contestsPlayed = played; contestsWon = won; bestScore = best
            footballContestsPlayed = footballPlayed; cricketContestsPlayed = cricketPlayed
            isLoading = false
        }

        val userRef = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("Users").child(userId).child("settings")
        userRef.get().addOnSuccessListener { snap ->
            notifContests = snap.child("notifContests").value as? Boolean ?: true
            notifResults = snap.child("notifResults").value as? Boolean ?: true
            notifPromos = snap.child("notifPromos").value as? Boolean ?: false
            selectedTheme = snap.child("theme").value?.toString() ?: "Dark"
            analyticsEnabled = snap.child("analyticsEnabled").value as? Boolean ?: true
        }
    }

    val winRate = if (contestsPlayed > 0) (contestsWon * 100 / contestsPlayed) else 0
    val rank = when { winRate >= 70 -> "Elite"; winRate >= 50 -> "Pro"; contestsPlayed > 0 -> "Rookie"; else -> "New Player" }
    val rankEmoji = when (rank) { "Elite" -> "👑"; "Pro" -> "⭐"; "Rookie" -> "🎮"; else -> "🆕" }

    // ── Bottom Sheets ──────────────────────────────────────────────────────────
    if (activeSheet == "Notifications") {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("🔔 Notifications", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                val toggles = listOf(
                    Triple("Contest Alerts", "Notify when new contests open", notifContests),
                    Triple("Match Results", "Notify when your match result is out", notifResults),
                    Triple("Promotions", "Bonus coins & special offers", notifPromos)
                )
                toggles.forEachIndexed { i, _ ->
                    val current = when (i) { 0 -> notifContests; 1 -> notifResults; else -> notifPromos }
                    val (title, subtitle, _) = toggles[i]
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.07f)).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                        Switch(
                            checked = current,
                            onCheckedChange = { value ->
                                when (i) { 0 -> notifContests = value; 1 -> notifResults = value; 2 -> notifPromos = value }
                                com.google.firebase.database.FirebaseDatabase
                                    .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                    .getReference("Users").child(userId).child("settings")
                                    .child(when (i) { 0 -> "notifContests"; 1 -> "notifResults"; else -> "notifPromos" })
                                    .setValue(value)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8E2DE2),
                                uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { activeSheet = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Save Preferences", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (activeSheet == "Privacy") {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("🔒 Privacy", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                listOf(
                    "🛡️  Your data is stored securely on Firebase" to "End-to-end encrypted user data",
                    "👤  Profile visibility" to "Only contest participants can see your score"
                ).forEach { (title, desc) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.07f)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(desc, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.07f)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("📊  Analytics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Anonymous usage data helps improve the app", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Switch(
                        checked = analyticsEnabled,
                        onCheckedChange = { value ->
                            analyticsEnabled = value
                            com.google.firebase.database.FirebaseDatabase
                                .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                .getReference("Users").child(userId).child("settings")
                                .child("analyticsEnabled").setValue(value)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8E2DE2),
                            uncheckedThumbColor = Color.Gray, uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF5F6D).copy(alpha = 0.1f))
                        .clickable {
                            android.app.AlertDialog.Builder(context)
                                .setTitle("Delete Account")
                                .setMessage("This will permanently delete your account and all data. Are you sure?")
                                .setPositiveButton("Delete") { _, _ ->
                                    com.google.firebase.database.FirebaseDatabase
                                        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                        .getReference("Users").child(userId).removeValue()
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.delete()
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    context.startActivity(Intent(context, LoginAct::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                }
                                .setNegativeButton("Cancel", null).show()
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🗑️  Delete Account", color = Color(0xFFFF5F6D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Permanently remove all your data", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Text("›", color = Color(0xFFFF5F6D), fontSize = 20.sp)
                }
            }
        }
    }

    if (activeSheet == "Appearance") {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("🎨 Appearance", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                Text("Theme", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                listOf("Dark" to "🌑  Dark Mode", "Purple" to "💜  Purple Glow", "Blue" to "💙  Ocean Blue").forEach { (key, label) ->
                    val isSelected = selectedTheme == key
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF8E2DE2).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.07f))
                            .border(1.dp, if (isSelected) Color(0xFF8E2DE2) else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable {
                                selectedTheme = key
                                com.google.firebase.database.FirebaseDatabase
                                    .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                    .getReference("Users").child(userId).child("settings").child("theme").setValue(key)
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (isSelected) Text("✓", color = Color(0xFF8E2DE2), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.07f)).padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ℹ️", fontSize = 18.sp)
                        Text("Theme changes will apply on next app restart.", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (activeSheet == "Help") {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1A2E),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("❓ Help & Support", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
                Divider(color = Color.White.copy(alpha = 0.1f))
                listOf(
                    "How do I create a team?" to "Tap 'Create Team' on the Home screen, select a match, then pick your players.",
                    "How are scores calculated?" to "Players earn points based on real match performance — goals, assists, wickets, and more.",
                    "When do I get my coins?" to "Coins are awarded automatically after a contest is resolved.",
                    "How do I join a contest?" to "Tap 'Join Contest' on Home, select your sport, and enter with your coins.",
                    "My coins are missing?" to "Coins update in real-time. Pull to refresh or restart the app."
                ).forEach { (question, answer) ->
                    var expanded by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .clickable { expanded = !expanded }.padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(if (expanded) "▲" else "▼", color = Color(0xFF8E2DE2), fontSize = 14.sp)
                        }
                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(answer, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                        .clickable {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:support@fantasysports.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Support Request - Fantasy Sports App")
                                putExtra(Intent.EXTRA_TEXT, "Hi Support Team,\n\nUser ID: $userId\n\nIssue: ")
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "No email app found", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📧 Email Support", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }

    // ── Main Profile UI ────────────────────────────────────────────────────────
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(90.dp).clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userName.take(1).uppercase(), fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(userEmail, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(color = Color(0xFF8E2DE2).copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp)) {
                    Text("$rankEmoji $rank", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.08f)).padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8E2DE2), modifier = Modifier.size(24.dp))
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f)).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("🔑 $userCoins", "Coins")
                    VerticalDivider()
                    StatItem("$contestsPlayed", "Played")
                    VerticalDivider()
                    StatItem("$contestsWon", "Won")
                }
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (winRate >= 50) listOf(Color(0xFF11998e), Color(0xFF38ef7d))
                            else listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                        )
                    ).padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Win Rate", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                        Text("$winRate%", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                        Text("$contestsWon wins from $contestsPlayed contests", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    }
                    Text(if (winRate >= 50) "🔥" else "💪", fontSize = 40.sp)
                }
            }
        }
        item { Text("⚽ Football Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(if (bestScore > 0) "$bestScore pts" else "—", "Best Score", Color(0xFFFF5F6D), Color(0xFFFFC371), Modifier.weight(1f))
                MiniStatCard("$footballContestsPlayed", "Contests", Color(0xFF1565C0), Color(0xFF8E2DE2), Modifier.weight(1f))
            }
        }
        item { Text("🏏 Cricket Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard(if (bestScore > 0) "$bestScore pts" else "—", "Best Score", Color(0xFF11998e), Color(0xFF38ef7d), Modifier.weight(1f))
                MiniStatCard("$cricketContestsPlayed", "Contests", Color(0xFFf7971e), Color(0xFFffd200), Modifier.weight(1f))
            }
        }
        item { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data class SettingsOption(val label: String, val sheetKey: String?)
                val options = listOf(
                    SettingsOption("🔔  Notifications", "Notifications"),
                    SettingsOption("🔒  Privacy", "Privacy"),
                    SettingsOption("🎨  Appearance", "Appearance"),
                    SettingsOption("❓  Help & Support", "Help"),
                    SettingsOption("🚪  Logout", null)
                )
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(if (option.label.contains("Logout")) Color(0xFFFF5F6D).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.07f))
                            .clickable {
                                if (option.sheetKey == null) {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    context.startActivity(Intent(context, LoginAct::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    })
                                } else {
                                    activeSheet = option.sheetKey
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option.label,
                            color = if (option.label.contains("Logout")) Color(0xFFFF5F6D) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (option.label.contains("Logout")) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            "›",
                            color = if (option.label.contains("Logout")) Color(0xFFFF5F6D).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ─── SHARED COMPONENTS ────────────────────────────────────────────────────────

@Composable
fun BigActionCard(title: String, sub: String, gradient: List<Color>, modifier: Modifier, onClick: () -> Unit = {}) {
    Card(modifier = modifier.height(180.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradient)).padding(16.dp)) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(sub, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp), modifier = Modifier.clickable { onClick() }) {
                    Text("Join Now", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun MatchCard(league: String = "IPL T20", teams: String = "IND vs AUS", time: String = "03:15:20") {
    Card(
        modifier = Modifier.width(180.dp).height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(league, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(teams, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1.0f))
            Text("Starts in: $time", color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable fun CenterText(text: String) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text, fontSize = 24.sp, color = Color.White) } }
@Composable fun StatItem(value: String, label: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp); Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp) } }
@Composable fun VerticalDivider() { Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.15f))) }
@Composable fun MiniStatCard(value: String, label: String, colorStart: Color, colorEnd: Color, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Brush.horizontalGradient(listOf(colorStart, colorEnd))).padding(16.dp)) {
        Column { Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp); Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp) }
    }
}

@Composable
fun AlertsContent() {
    val context = LocalContext.current
    var contests by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val db = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("contests")
        db.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<Triple<String, String, String>>()
            snapshot.children.forEach { child ->
                val name = child.child("name").value?.toString() ?: ""
                val sport = child.child("sport").value?.toString() ?: ""
                val matchName = child.child("matchName").value?.toString() ?: ""
                val entryCoins = (child.child("entryCoins").value as? Long)?.toInt() ?: 0
                val prizeCoins = (child.child("prizeCoins").value as? Long)?.toInt() ?: 0
                val joinedCount = child.child("joinedUsers").childrenCount
                val sportEmoji = if (sport == "Football") "⚽ Football" else "🏏 Cricket"
                val description = "$matchName — Entry: $entryCoins coins | Prize: $prizeCoins coins | ${joinedCount} joined"
                if (name.isNotEmpty()) list.add(Triple(sportEmoji, "$name\n$description", "Live"))
            }
            contests = list
            isLoading = false
        }.addOnFailureListener { isLoading = false }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("🔔 Alerts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                if (!isLoading) Text("${contests.size} active", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        }
        if (isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8E2DE2))
                }
            }
        } else if (contests.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔔", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No alerts yet!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("New contests will appear here", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(contests) { alert ->
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(42.dp).clip(CircleShape)
                            .background(Brush.verticalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))),
                        contentAlignment = Alignment.Center
                    ) { Text("🎯", fontSize = 20.sp) }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(alert.first, color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF38ef7d).copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) { Text("🟢 Live", color = Color(0xFF38ef7d), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(alert.second, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                                .clickable {
                                    context.startActivity(Intent(context, ContestActivity::class.java).apply {
                                        putExtra("sport", if (alert.first.contains("Football")) "Football" else "Cricket")
                                    })
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) { Text("Join Now", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
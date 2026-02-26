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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

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
    var selectedTab by remember { mutableStateOf(0) }

    // Separate football and cricket team data
    var footballTeamPlayers by remember { mutableStateOf<List<String>>(emptyList()) }
    var cricketTeamPlayers by remember { mutableStateOf<List<String>>(emptyList()) }

    // Real coins from Firebase
    var userCoins by remember { mutableStateOf(1250) }

    // Real-time listeners — updates instantly when team is saved
    DisposableEffect(Unit) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userRef = com.google.firebase.database.FirebaseDatabase
            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
            .getReference("Users").child(userId)

        val footballListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val players = mutableListOf<String>()
                snapshot.child("players").children.forEach {
                    players.add(it.value?.toString() ?: "")
                }
                footballTeamPlayers = players
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        val cricketListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val players = mutableListOf<String>()
                snapshot.child("players").children.forEach {
                    players.add(it.value?.toString() ?: "")
                }
                cricketTeamPlayers = players
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        // Real-time coins listener
        val coinsListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                userCoins = (snapshot.value as? Long)?.toInt() ?: 1250
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }

        userRef.child("footballTeam").addValueEventListener(footballListener)
        userRef.child("cricketTeam").addValueEventListener(cricketListener)
        userRef.child("coins").addValueEventListener(coinsListener)

        // Initialize coins in Firebase if not set
        userRef.child("coins").get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                userRef.child("coins").setValue(1250)
            }
        }

        onDispose {
            userRef.child("footballTeam").removeEventListener(footballListener)
            userRef.child("cricketTeam").removeEventListener(cricketListener)
            userRef.child("coins").removeEventListener(coinsListener)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1A1A2E),
                modifier = Modifier.height(80.dp)
            ) {
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
                        icon = {
                            Icon(
                                painter = painterResource(icons[index]),
                                contentDescription = title,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = { Text(title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF8E2DE2),
                            selectedTextColor = Color(0xFF8E2DE2),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF8E2DE2).copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
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
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeContent(
                        footballTeamPlayers = footballTeamPlayers,
                        cricketTeamPlayers = cricketTeamPlayers,
                        userCoins = userCoins
                    )
                    1 -> LeaguesContent()
                    2 -> AlertsContent()
                    3 -> ProfileContent(userName = userName, userEmail = userEmail)
                }
            }
        }
    }
}

@Composable
fun HomeContent(
    footballTeamPlayers: List<String>,
    cricketTeamPlayers: List<String>,
    userCoins: Int = 1250
) {
    val context = LocalContext.current
    var selectedSport by remember { mutableStateOf(0) } // 0 = Football, 1 = Cricket

    val footballMatches = listOf(
        Triple("Premier League", "MAN UTD vs ARS", "02:30:00"),
        Triple("La Liga", "BAR vs RMA", "05:00:00"),
        Triple("Serie A", "JUV vs MIL", "08:15:00"),
        Triple("Bundesliga", "BAY vs DOR", "11:00:00")
    )

    val cricketMatches = listOf(
        Triple("IPL T20", "IND vs AUS", "03:15:20"),
        Triple("Test Match", "ENG vs PAK", "06:00:00"),
        Triple("ODI Series", "SA vs NZ", "09:30:00"),
        Triple("T20 WC", "WI vs SL", "12:45:00")
    )

    // Show correct team based on selected sport
    val currentTeamPlayers = if (selectedSport == 0) footballTeamPlayers else cricketTeamPlayers
    val currentSportLabel = if (selectedSport == 0) "Football" else "Cricket"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }

        // TOP BAR
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(R.drawable.baseline_key_24),
                        "trophy",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        " Fantasy Sports",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
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
                            Icon(
                                painterResource(R.drawable.baseline_key_24),
                                null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            // REAL COINS from Firebase
                            Text(
                                " $userCoins",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
            }
        }

        // SPORT TOGGLE
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

        // MY TEAM CARD — shows football or cricket team based on toggle
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF8E2DE2)))
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        "My $currentSportLabel Team",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (currentTeamPlayers.isEmpty()) {
                        Text(
                            "No team saved yet!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tap Create Team to get started",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            "$currentSportLabel • ${currentTeamPlayers.size} Players",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            currentTeamPlayers.joinToString(", "),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // EDIT & DELETE BUTTONS
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // EDIT button — opens CreateTeam with same match + players pre-selected
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
                                                val homeTeamName = snapshot.child("homeTeamName").value?.toString() ?: ""
                                                val awayTeamName = snapshot.child("awayTeamName").value?.toString() ?: ""
                                                val homeTeamId = (snapshot.child("homeTeamId").value as? Long)?.toInt() ?: 0
                                                val awayTeamId = (snapshot.child("awayTeamId").value as? Long)?.toInt() ?: 0
                                                val intent = Intent(context, CreateTeamActivity::class.java).apply {
                                                    putExtra("sport", if (selectedSport == 0) "football" else "cricket")
                                                    putExtra("homeTeamName", homeTeamName)
                                                    putExtra("awayTeamName", awayTeamName)
                                                    putExtra("homeTeamId", homeTeamId)
                                                    putExtra("awayTeamId", awayTeamId)
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
                            // DELETE button — removes team from Firebase
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFF5F6D).copy(alpha = 0.7f))
                                    .clickable {
                                        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        val sportNode = if (selectedSport == 0) "footballTeam" else "cricketTeam"
                                        com.google.firebase.database.FirebaseDatabase
                                            .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
                                            .getReference("Users").child(userId).child(sportNode)
                                            .removeValue()
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

        // ACTION CARDS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BigActionCard(
                    title = "Create Team",
                    sub = "Build Your Squad",
                    gradient = listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.startActivity(Intent(context, MatchSelectionActivity::class.java))
                    }
                )
                // JOIN CONTEST — now opens ContestActivity with current sport
                BigActionCard(
                    title = "Join Contest",
                    sub = "Enter & Compete",
                    gradient = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val intent = Intent(context, ContestActivity::class.java).apply {
                            putExtra("sport", currentSportLabel)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        // UPCOMING MATCHES
        item {
            Text(
                if (selectedSport == 0) "⚽ Upcoming Matches" else "🏏 Upcoming Matches",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val matchList = if (selectedSport == 0) footballMatches else cricketMatches
                items(matchList) { match ->
                    MatchCard(league = match.first, teams = match.second, time = match.third)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun BigActionCard(title: String, sub: String, gradient: List<Color>, modifier: Modifier, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.height(180.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradient))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.BottomStart)) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(sub, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        "Join Now",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MatchCard(
    league: String = "IPL T20",
    teams: String = "IND vs AUS",
    time: String = "03:15:20"
) {
    Card(
        modifier = Modifier.width(180.dp).height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(league, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(teams, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1.0f))
            Text("Starts in: $time", color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CenterText(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 24.sp, color = Color.White)
    }
}

@Composable
fun LeaguesContent() {
    val footballLeagues = listOf(
        Triple("Premier Fantasy League", "14 players", "#2  •  342 pts"),
        Triple("Champions League Cup", "8 players", "#1  •  410 pts"),
        Triple("La Liga Fantasy", "10 players", "#5  •  280 pts")
    )
    val cricketLeagues = listOf(
        Triple("IPL Fantasy League", "12 players", "#3  •  310 pts"),
        Triple("T20 World Cup", "20 players", "#7  •  255 pts"),
        Triple("Test Match Fantasy", "6 players", "#2  •  390 pts")
    )

    var selectedSport by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { Text("🏆 My Leagues", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) }
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
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371))))
                        .clickable { }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Create League", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                        .clickable { }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔗 Join League", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
        val currentLeagues = if (selectedSport == 0) footballLeagues else cricketLeagues
        items(currentLeagues) { league ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(league.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(league.second, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Rank", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                        Text(league.third, color = Color(0xFFFFE082), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun ProfileContent(userName: String = "Player", userEmail: String = "") {
    val context = LocalContext.current
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
                ) { Text("🏆", fontSize = 36.sp) }
                Spacer(modifier = Modifier.height(12.dp))
                Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(userEmail, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(color = Color(0xFF8E2DE2).copy(alpha = 0.3f), shape = RoundedCornerShape(20.dp)) {
                    Text("⭐ Pro Player", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f)).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("1,250", "Total Pts")
                VerticalDivider()
                StatItem("6", "Leagues")
                VerticalDivider()
                StatItem("#2", "Best Rank")
            }
        }
        item { Text("⚽ Football Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard("410 pts", "Best Score", Color(0xFFFF5F6D), Color(0xFFFFC371), Modifier.weight(1f))
                MiniStatCard("28", "Gameweeks", Color(0xFF1565C0), Color(0xFF8E2DE2), Modifier.weight(1f))
            }
        }
        item { Text("🏏 Cricket Stats", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStatCard("390 pts", "Best Score", Color(0xFF11998e), Color(0xFF38ef7d), Modifier.weight(1f))
                MiniStatCard("14", "Matches", Color(0xFFf7971e), Color(0xFFffd200), Modifier.weight(1f))
            }
        }
        item { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("🔔  Notifications", "🔒  Privacy", "🎨  Appearance", "❓  Help & Support", "🚪  Logout").forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.07f))
                            .clickable {
                                if (option == "🚪  Logout") {
                                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                    val intent = Intent(context, LoginAct::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                            }.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option, color = Color.White, fontSize = 14.sp)
                        Text("›", color = Color.White.copy(alpha = 0.4f), fontSize = 20.sp)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
    }
}

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.15f)))
}

@Composable
fun MiniStatCard(value: String, label: String, colorStart: Color, colorEnd: Color, modifier: Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(colorStart, colorEnd))).padding(16.dp)
    ) {
        Column {
            Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
fun AlertsContent() {
    val alerts = listOf(
        Triple("⚽ Football", "Premier League Fantasy Cup is now open! Join before kickoff.", "2 mins ago"),
        Triple("🏏 Cricket", "IPL Mega Contest — ₹50 Lakh prize pool. Limited spots!", "15 mins ago"),
        Triple("⚽ Football", "Champions League Weekly Contest just dropped. 500 players max.", "1 hr ago"),
        Triple("🏏 Cricket", "T20 World Cup Special Contest — Free entry today only!", "3 hrs ago"),
        Triple("⚽ Football", "La Liga Fantasy Contest open. Top 3 win cash prizes.", "5 hrs ago"),
        Triple("🏏 Cricket", "Test Match Contest now live. Build your best XI!", "Yesterday")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(20.dp)) }
        item { Text("🔔 Alerts", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) }
        items(alerts) { alert ->
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
                        Text(alert.third, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(alert.second, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))))
                            .clickable { }.padding(horizontal = 12.dp, vertical = 5.dp)
                    ) { Text("Join Now", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
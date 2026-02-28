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
import java.text.SimpleDateFormat
import java.util.*

// ─── Data model for a contest stored in Firebase ───────────────────────────
data class Contest(
    val id: String = "",
    val name: String = "",
    val sport: String = "",
    val matchName: String = "",
    val prizeCoins: Int = 0,
    val entryCoins: Int = 0,
    val maxPlayers: Int = 2,
    val joinedUsers: Map<String, Any> = emptyMap(),
    val matchTime: Long = 0L  // ← NEW: Unix timestamp of match kickoff
)

class ContestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sport = intent.getStringExtra("sport") ?: "Football"
        setContent {
            ContestScreen(sport = sport)
        }
    }
}

// ─── Deadline helpers ───────────────────────────────────────────────────────

// Returns true if the contest is locked (within 1 hour of match or past it)
fun isContestLocked(matchTime: Long): Boolean {
    if (matchTime == 0L) return false // no deadline set = open
    val now = System.currentTimeMillis()
    val oneHourBefore = matchTime - (60 * 60 * 1000L)
    return now >= oneHourBefore
}

// Returns a human-readable countdown string e.g. "Locks in 2h 30m"
fun lockCountdown(matchTime: Long): String {
    if (matchTime == 0L) return ""
    val now = System.currentTimeMillis()
    val oneHourBefore = matchTime - (60 * 60 * 1000L)
    val diff = oneHourBefore - now
    if (diff <= 0) return "🔒 Locked"
    val hours = diff / (1000 * 60 * 60)
    val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
    return when {
        hours > 0 -> "⏰ Locks in ${hours}h ${minutes}m"
        else -> "⏰ Locks in ${minutes}m"
    }
}

// ─── Real score calculation ─────────────────────────────────────────────────
//
// Instead of random numbers, we score each player based on:
//   - Their saved `points` value from Firebase (real player rating)
//   - A position multiplier (captains/key roles score more)
//   - A small performance variance (±15%) to simulate match day
//
// Position multipliers:
//   GK/WK  → 1.0x  (baseline)
//   DEF/BAT → 1.1x
//   MID/AR  → 1.2x
//   FWD/BOWL → 1.3x  (attackers score most in fantasy)
//
fun positionMultiplier(position: String): Double {
    return when (position) {
        "GK", "WK" -> 1.0
        "DEF", "BAT" -> 1.1
        "MID", "AR" -> 1.2
        "FWD", "BOWL" -> 1.3
        else -> 1.0
    }
}

// Calculate a team's fantasy score from their saved player list + position data
// playerData: map of playerName -> Pair(position, basePoints)
fun calculateTeamScore(playerNames: List<String>, playerData: Map<String, Pair<String, Int>>): Int {
    var total = 0.0
    playerNames.forEach { name ->
        val (position, basePoints) = playerData[name] ?: Pair("MID", 150)
        val multiplier = positionMultiplier(position)
        // ±15% variance to simulate match performance
        val variance = 0.85 + (Math.random() * 0.30) // 0.85 to 1.15
        total += basePoints * multiplier * variance
    }
    return total.toInt()
}

@Composable
fun ContestScreen(sport: String = "Football") {
    val context = LocalContext.current
    val db = com.google.firebase.database.FirebaseDatabase
        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
        .reference

    val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""

    var contests by remember { mutableStateOf<List<Contest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userCoins by remember { mutableStateOf(0) }
    var hasSavedTeam by remember { mutableStateOf(false) }
    var joiningContestId by remember { mutableStateOf<String?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }

    // ── Ticker — refreshes every minute so countdown updates live ──────────
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val sportNode = if (sport == "Football") "footballTeam" else "cricketTeam"
    val sportEmoji = if (sport == "Football") "⚽" else "🏏"

    // Load user's coins + check saved team + load player position data
    // We store playerData as name -> (position, points) for scoring
    var playerData by remember { mutableStateOf<Map<String, Pair<String, Int>>>(emptyMap()) }

    LaunchedEffect(Unit) {
        db.child("Users").child(userId).get().addOnSuccessListener { snapshot ->
            userCoins = (snapshot.child("coins").value as? Long)?.toInt() ?: 1250
            hasSavedTeam = snapshot.child(sportNode).exists()
        }
    }

    // Load contests from Firebase in real-time
    DisposableEffect(Unit) {
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<Contest>()
                snapshot.children.forEach { child ->
                    val contestSport = child.child("sport").value?.toString() ?: ""
                    if (contestSport == sport) {
                        val joined = mutableMapOf<String, Any>()
                        child.child("joinedUsers").children.forEach { u ->
                            joined[u.key ?: ""] = u.value ?: ""
                        }
                        list.add(Contest(
                            id = child.key ?: "",
                            name = child.child("name").value?.toString() ?: "",
                            sport = contestSport,
                            matchName = child.child("matchName").value?.toString() ?: "",
                            prizeCoins = (child.child("prizeCoins").value as? Long)?.toInt() ?: 0,
                            entryCoins = (child.child("entryCoins").value as? Long)?.toInt() ?: 0,
                            maxPlayers = (child.child("maxPlayers").value as? Long)?.toInt() ?: 2,
                            joinedUsers = joined,
                            matchTime = (child.child("matchTime").value as? Long) ?: 0L
                        ))
                    }
                }
                contests = list
                isLoading = false
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                isLoading = false
            }
        }
        db.child("contests").addValueEventListener(listener)
        onDispose { db.child("contests").removeEventListener(listener) }
    }

    // Seed default contests if none exist — now with matchTime set to 48h from now
    LaunchedEffect(Unit) {
        db.child("contests").get().addOnSuccessListener { snapshot ->
            val hasSportContests = snapshot.children.any {
                it.child("sport").value?.toString() == sport
            }
            if (!hasSportContests) {
                // Set match kickoff 48 hours from now so there's plenty of time to join
                val kickoff48h = System.currentTimeMillis() + (48 * 60 * 60 * 1000L)
                val kickoff72h = System.currentTimeMillis() + (72 * 60 * 60 * 1000L)

                val defaults = if (sport == "Football") listOf(
                    mapOf("name" to "⚽ Head-to-Head Clash", "sport" to "Football", "matchName" to "Premier League", "prizeCoins" to 500, "entryCoins" to 100, "maxPlayers" to 2, "matchTime" to kickoff48h),
                    mapOf("name" to "🏆 Mini League", "sport" to "Football", "matchName" to "Premier League", "prizeCoins" to 1000, "entryCoins" to 200, "maxPlayers" to 2, "matchTime" to kickoff48h),
                    mapOf("name" to "💎 Big Win Contest", "sport" to "Football", "matchName" to "Champions League", "prizeCoins" to 2000, "entryCoins" to 300, "maxPlayers" to 2, "matchTime" to kickoff72h)
                ) else listOf(
                    mapOf("name" to "🏏 Cricket Duel", "sport" to "Cricket", "matchName" to "T20 World Cup", "prizeCoins" to 500, "entryCoins" to 100, "maxPlayers" to 2, "matchTime" to kickoff48h),
                    mapOf("name" to "🏆 T20 League", "sport" to "Cricket", "matchName" to "IPL T20", "prizeCoins" to 1000, "entryCoins" to 200, "maxPlayers" to 2, "matchTime" to kickoff48h),
                    mapOf("name" to "💎 Cricket Grand Prix", "sport" to "Cricket", "matchName" to "International T20", "prizeCoins" to 2000, "entryCoins" to 300, "maxPlayers" to 2, "matchTime" to kickoff72h)
                )
                defaults.forEach { contestData ->
                    db.child("contests").push().setValue(contestData)
                }
            }
        }
    }

    // ── Join contest with REAL scoring ─────────────────────────────────────
    fun joinContest(contest: Contest) {
        if (!hasSavedTeam) {
            resultMessage = "❌ You need to save a $sport team first!"
            return
        }
        if (userId in contest.joinedUsers) {
            resultMessage = "⚠️ You already joined this contest!"
            return
        }
        if (contest.joinedUsers.size >= contest.maxPlayers) {
            resultMessage = "❌ Contest is full!"
            return
        }
        if (userCoins < contest.entryCoins) {
            resultMessage = "❌ Not enough coins! Need ${contest.entryCoins}"
            return
        }
        // ── DEADLINE CHECK ──────────────────────────────────────────────────
        if (isContestLocked(contest.matchTime)) {
            resultMessage = "🔒 Contest is locked — match starts soon!"
            return
        }

        joiningContestId = contest.id
        val displayName = currentUser?.email?.substringBefore("@") ?: "Player"
        val contestRef = db.child("contests").child(contest.id)

        // Deduct entry coins
        db.child("Users").child(userId).child("coins").setValue(userCoins - contest.entryCoins)
        userCoins -= contest.entryCoins

        // Add user to contest
        contestRef.child("joinedUsers").child(userId).setValue(displayName)
            .addOnSuccessListener {
                val updatedJoined = contest.joinedUsers.toMutableMap()
                updatedJoined[userId] = displayName

                if (updatedJoined.size >= contest.maxPlayers) {
                    val otherUserId = updatedJoined.keys.first { it != userId }

                    // ── REAL SCORE CALCULATION ──────────────────────────────
                    // Step 1: Load MY saved team's player names
                    db.child("Users").child(userId).child(sportNode).child("players")
                        .get().addOnSuccessListener { mySnap ->
                            val myPlayerNames = mySnap.children.map { it.value?.toString() ?: "" }

                            // Step 2: Load OPPONENT's saved team's player names
                            db.child("Users").child(otherUserId).child(sportNode).child("players")
                                .get().addOnSuccessListener { theirSnap ->
                                    val theirPlayerNames = theirSnap.children.map { it.value?.toString() ?: "" }

                                    // Step 3: Load player position+points data from Firebase
                                    // We look up each player's stats from the squads node
                                    val allPlayerNames = (myPlayerNames + theirPlayerNames).distinct()
                                    val resolvedData = mutableMapOf<String, Pair<String, Int>>()

                                    // For football: look up from football API cache or use position defaults
                                    // For cricket: look up from cricket/squads in Firebase
                                    // We fetch the squad data to get position & points for each player
                                    val squadRef = if (sport == "Cricket")
                                        db.child("cricket").child("squads")
                                    else
                                        db.child("footballSquads") // cache node (may not exist, fallback below)

                                    // Try to load cached squad data, fallback to position-based defaults
                                    squadRef.get().addOnCompleteListener { task ->
                                        if (task.isSuccessful && task.result.exists()) {
                                            // Parse squad data: format "name|position|credits|points"
                                            task.result.children.forEach { teamSnap ->
                                                teamSnap.children.forEach { playerSnap ->
                                                    val raw = playerSnap.value?.toString() ?: return@forEach
                                                    val parts = raw.split("|")
                                                    if (parts.size >= 4) {
                                                        val name = parts[0]
                                                        val pos = parts[1]
                                                        val pts = parts[3].toIntOrNull() ?: 150
                                                        if (name in allPlayerNames) {
                                                            resolvedData[name] = Pair(pos, pts)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Fill in any missing players with position-based defaults
                                        // (football players may not be in the cache node)
                                        allPlayerNames.forEach { name ->
                                            if (name !in resolvedData) {
                                                // Default: MID position, 150 base points
                                                resolvedData[name] = Pair("MID", 150)
                                            }
                                        }

                                        // Step 4: Calculate scores using real position weights
                                        val myScore = calculateTeamScore(myPlayerNames, resolvedData)
                                        val theirScore = calculateTeamScore(theirPlayerNames, resolvedData)

                                        val winnerId = if (myScore >= theirScore) userId else otherUserId
                                        val iWon = winnerId == userId

                                        // Step 5: Save result to Firebase
                                        val result = mapOf(
                                            "winnerId" to winnerId,
                                            "scores" to mapOf(
                                                userId to myScore,
                                                otherUserId to theirScore
                                            ),
                                            "resolved" to true
                                        )
                                        contestRef.child("result").setValue(result)

                                        // Step 6: Award prize coins to winner
                                        db.child("Users").child(winnerId).child("coins").get()
                                            .addOnSuccessListener { coinSnap ->
                                                val currentCoins = (coinSnap.value as? Long)?.toInt() ?: 1250
                                                db.child("Users").child(winnerId).child("coins")
                                                    .setValue(currentCoins + contest.prizeCoins)
                                            }

                                        // Step 7: Show result
                                        val myTopPlayer = myPlayerNames.maxByOrNull {
                                            val (pos, pts) = resolvedData[it] ?: Pair("MID", 150)
                                            (pts * positionMultiplier(pos)).toInt()
                                        } ?: ""

                                        if (iWon) {
                                            userCoins += contest.prizeCoins
                                            resultMessage = "🎉 You WON! +${contest.prizeCoins} coins!\n" +
                                                    "Your score: $myScore vs their score: $theirScore\n" +
                                                    "⭐ Best player: $myTopPlayer"
                                        } else {
                                            resultMessage = "😔 So close! You lost by ${theirScore - myScore} pts\n" +
                                                    "Your score: $myScore vs their score: $theirScore\n" +
                                                    "💪 Better luck next time!"
                                        }
                                        joiningContestId = null
                                    }
                                }
                        }
                } else {
                    resultMessage = "✅ Joined! Waiting for opponent..."
                    joiningContestId = null
                }
            }
    }

    // ── UI ──────────────────────────────────────────────────────────────────
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

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { (context as? ContestActivity)?.finish() },
                    contentAlignment = Alignment.Center
                ) { Text("←", color = Color.White, fontSize = 18.sp) }

                Text(
                    "$sportEmoji $sport Contests",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp
                )

                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("🔑 $userCoins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // No team warning
            if (!hasSavedTeam) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF5F6D).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF5F6D).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "⚠️ You need to save a $sport team first before joining contests!",
                        color = Color(0xFFFF5F6D), fontSize = 13.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Result message
            resultMessage?.let { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                msg.contains("WON") -> Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
                                msg.contains("Waiting") -> Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                else -> Brush.horizontalGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)))
                            }
                        )
                        .padding(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Text(msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("✕", color = Color.White, fontSize = 16.sp, modifier = Modifier.clickable { resultMessage = null })
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF8E2DE2))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(contests) { contest ->
                        val alreadyJoined = userId in contest.joinedUsers
                        val isFull = contest.joinedUsers.size >= contest.maxPlayers
                        val isJoining = joiningContestId == contest.id
                        val locked = isContestLocked(contest.matchTime)
                        val countdown = lockCountdown(contest.matchTime)

                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.12f))
                                    )
                                )
                                .border(
                                    1.dp,
                                    when {
                                        locked && !alreadyJoined -> Color(0xFFFF5F6D).copy(alpha = 0.4f)
                                        alreadyJoined -> Color(0xFF38ef7d).copy(alpha = 0.5f)
                                        else -> Color.White.copy(alpha = 0.15f)
                                    },
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                // Contest name + prize
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(contest.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(contest.matchName, color = Color(0xFF8E2DE2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.2f), Color(0xFFFFC371).copy(alpha = 0.2f))))
                                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("🏆 ${contest.prizeCoins}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                // ── Deadline countdown badge ──────────────────
                                if (countdown.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (locked) Color(0xFFFF5F6D).copy(alpha = 0.15f)
                                                else Color(0xFFFFE082).copy(alpha = 0.15f)
                                            )
                                            .border(
                                                1.dp,
                                                if (locked) Color(0xFFFF5F6D).copy(alpha = 0.4f)
                                                else Color(0xFFFFE082).copy(alpha = 0.4f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            countdown,
                                            color = if (locked) Color(0xFFFF5F6D) else Color(0xFFFFE082),
                                            fontSize = 11.sp, fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Stats row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Entry", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                        Text("🔑 ${contest.entryCoins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.15f)))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Players", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                        Text("${contest.joinedUsers.size}/${contest.maxPlayers}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                    Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.15f)))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Prize", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                                        Text("🔑 ${contest.prizeCoins}", color = Color(0xFF38ef7d), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Join button
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                alreadyJoined -> Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
                                                locked || isFull -> Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                                                else -> Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                            }
                                        )
                                        .clickable(enabled = !alreadyJoined && !isFull && !isJoining && !locked) {
                                            joinContest(contest)
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isJoining) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text(
                                            when {
                                                alreadyJoined && isFull -> "✅ Played"
                                                alreadyJoined -> "✅ Joined — Waiting for opponent"
                                                locked -> "🔒 Joining Closed"
                                                isFull -> "Contest Full"
                                                else -> "Join Contest  •  🔑 ${contest.entryCoins}"
                                            },
                                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}
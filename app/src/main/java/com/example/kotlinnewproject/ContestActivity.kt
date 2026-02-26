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

// ─── Data model for a contest stored in Firebase ───────────────────────────
data class Contest(
    val id: String = "",
    val name: String = "",
    val sport: String = "",          // "Football" or "Cricket"
    val matchName: String = "",
    val prizeCoins: Int = 0,
    val entryCoins: Int = 0,
    val maxPlayers: Int = 2,
    val joinedUsers: Map<String, Any> = emptyMap()  // userId -> displayName
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

    val sportNode = if (sport == "Football") "footballTeam" else "cricketTeam"
    val sportEmoji = if (sport == "Football") "⚽" else "🏏"

    // Load user's coins + check if they have a saved team
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
                            joinedUsers = joined
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

    // Seed default contests if none exist for this sport
    LaunchedEffect(Unit) {
        db.child("contests").get().addOnSuccessListener { snapshot ->
            val hasSportContests = snapshot.children.any {
                it.child("sport").value?.toString() == sport
            }
            if (!hasSportContests) {
                val defaults = if (sport == "Football") listOf(
                    mapOf("name" to "⚽ Head-to-Head Clash", "sport" to "Football", "matchName" to "Premier League", "prizeCoins" to 500, "entryCoins" to 100, "maxPlayers" to 2),
                    mapOf("name" to "🏆 Mini League", "sport" to "Football", "matchName" to "Premier League", "prizeCoins" to 1000, "entryCoins" to 200, "maxPlayers" to 2),
                    mapOf("name" to "💎 Big Win Contest", "sport" to "Football", "matchName" to "Champions League", "prizeCoins" to 2000, "entryCoins" to 300, "maxPlayers" to 2)
                ) else listOf(
                    mapOf("name" to "🏏 Cricket Duel", "sport" to "Cricket", "matchName" to "T20 World Cup", "prizeCoins" to 500, "entryCoins" to 100, "maxPlayers" to 2),
                    mapOf("name" to "🏆 T20 League", "sport" to "Cricket", "matchName" to "IPL T20", "prizeCoins" to 1000, "entryCoins" to 200, "maxPlayers" to 2),
                    mapOf("name" to "💎 Cricket Grand Prix", "sport" to "Cricket", "matchName" to "International T20", "prizeCoins" to 2000, "entryCoins" to 300, "maxPlayers" to 2)
                )
                defaults.forEach { contestData ->
                    db.child("contests").push().setValue(contestData)
                }
            }
        }
    }

    // Join a contest & calculate winner if full
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

        joiningContestId = contest.id
        val displayName = currentUser?.email?.substringBefore("@") ?: "Player"
        val contestRef = db.child("contests").child(contest.id)

        // Deduct entry coins from user
        db.child("Users").child(userId).child("coins")
            .setValue(userCoins - contest.entryCoins)
        userCoins -= contest.entryCoins

        // Add user to contest
        contestRef.child("joinedUsers").child(userId).setValue(displayName)
            .addOnSuccessListener {
                // Check if contest is now full → calculate winner
                val updatedJoined = contest.joinedUsers.toMutableMap()
                updatedJoined[userId] = displayName

                if (updatedJoined.size >= contest.maxPlayers) {
                    // Calculate scores for all joined users
                    val otherUserId = updatedJoined.keys.first { it != userId }

                    // Get both teams' player points from Firebase
                    db.child("Users").child(userId).child(sportNode).child("players")
                        .get().addOnSuccessListener { mySnap ->
                            val myPlayers = mySnap.children.map { it.value?.toString() ?: "" }

                            db.child("Users").child(otherUserId).child(sportNode).child("players")
                                .get().addOnSuccessListener { theirSnap ->
                                    val theirPlayers = theirSnap.children.map { it.value?.toString() ?: "" }

                                    // Simulate points: each player gets random score 50-200
                                    val myScore = myPlayers.sumOf { (50..200).random() }
                                    val theirScore = theirPlayers.sumOf { (50..200).random() }

                                    val winnerId = if (myScore >= theirScore) userId else otherUserId
                                    val iWon = winnerId == userId

                                    // Save result to Firebase
                                    val result = mapOf(
                                        "winnerId" to winnerId,
                                        "scores" to mapOf(
                                            userId to myScore,
                                            otherUserId to theirScore
                                        ),
                                        "resolved" to true
                                    )
                                    contestRef.child("result").setValue(result)

                                    // Award prize to winner
                                    db.child("Users").child(winnerId).child("coins").get()
                                        .addOnSuccessListener { coinSnap ->
                                            val currentCoins = (coinSnap.value as? Long)?.toInt() ?: 1250
                                            db.child("Users").child(winnerId).child("coins")
                                                .setValue(currentCoins + contest.prizeCoins)
                                        }

                                    if (iWon) {
                                        userCoins += contest.prizeCoins
                                        resultMessage = "🎉 You WON! +${contest.prizeCoins} coins!\nYour score: $myScore vs their score: $theirScore"
                                    } else {
                                        resultMessage = "😔 You lost this time!\nYour score: $myScore vs their score: $theirScore\nBetter luck next time!"
                                    }
                                    joiningContestId = null
                                }
                        }
                } else {
                    resultMessage = "✅ Joined! Waiting for opponent..."
                    joiningContestId = null
                }
            }
    }

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

                // Coins display
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

            // Result message popup
            resultMessage?.let { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (msg.contains("WON"))
                                Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
                            else if (msg.contains("Waiting"))
                                Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                            else
                                Brush.horizontalGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371)))
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(msg, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        Text("✕", color = Color.White, fontSize = 16.sp,
                            modifier = Modifier.clickable { resultMessage = null })
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
                        val isResolved = contest.joinedUsers.size >= contest.maxPlayers && alreadyJoined

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
                                    if (alreadyJoined) Color(0xFF38ef7d).copy(alpha = 0.5f)
                                    else Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                // Contest name + match
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(contest.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text(contest.matchName, color = Color(0xFF8E2DE2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    // Prize badge
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.2f), Color(0xFFFFC371).copy(alpha = 0.2f))))
                                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("🏆 ${contest.prizeCoins}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Stats row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
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
                                                isFull -> Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                                                else -> Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                                            }
                                        )
                                        .clickable(enabled = !alreadyJoined && !isFull && !isJoining) {
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
package com.example.kotlinnewproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AdminScreen() }
    }
}

data class AdminUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val coins: Int,
    val isAdmin: Boolean
)

data class AdminMatch(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val minute: Int,
    val status: String
)

data class AdminContest(
    val id: String,
    val name: String,
    val sport: String,
    val matchName: String,
    val entryCoins: Int,
    val prizeCoins: Int,
    val joinedCount: Long
)

@Composable
fun AdminScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("👥 Users", "⚽ Matches", "🏆 Contests")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)))
                )
                .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            Column {
                Text("🛡️ Admin Panel", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Text("Manage your app", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
        }

        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A2E))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selectedTab == index)
                                Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                            else
                                Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.07f)))
                        )
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> AdminUsersTab()
            1 -> AdminMatchesTab()
            2 -> AdminContestsTab()
        }
    }
}

// ─── USERS TAB ─────────────────────────────────────────────────────────────────

@Composable
fun AdminUsersTab() {
    var users by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var editingUser by remember { mutableStateOf<AdminUser?>(null) }
    var editCoins by remember { mutableStateOf("") }

    val db = com.google.firebase.database.FirebaseDatabase
        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
        .getReference("Users")

    LaunchedEffect(Unit) {
        db.get().addOnSuccessListener { snapshot ->
            val list = mutableListOf<AdminUser>()
            snapshot.children.forEach { child ->
                list.add(
                    AdminUser(
                        uid = child.key ?: "",
                        firstName = child.child("firstName").value?.toString() ?: "",
                        lastName = child.child("lastName").value?.toString() ?: "",
                        email = child.child("email").value?.toString() ?: "",
                        coins = (child.child("coins").value as? Long)?.toInt() ?: 0,
                        isAdmin = child.child("isAdmin").value as? Boolean ?: false
                    )
                )
            }
            users = list
            isLoading = false
        }
    }

    // Edit coins dialog
    if (editingUser != null) {
        AlertDialog(
            onDismissRequest = { editingUser = null },
            containerColor = Color(0xFF1A1A2E),
            title = { Text("Edit Coins — ${editingUser!!.firstName}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editCoins,
                    onValueChange = { editCoins = it },
                    label = { Text("New coin amount", color = Color.White.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF8E2DE2),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newCoins = editCoins.toIntOrNull()
                        if (newCoins != null && editingUser != null) {
                            db.child(editingUser!!.uid).child("coins").setValue(newCoins)
                            users = users.map { if (it.uid == editingUser!!.uid) it.copy(coins = newCoins) else it }
                        }
                        editingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2))
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    val filtered = users.filter {
        searchQuery.isEmpty() ||
                it.firstName.contains(searchQuery, ignoreCase = true) ||
                it.lastName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search users...", color = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8E2DE2),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f)
                )
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${filtered.size} users", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                if (isLoading) Text("Loading...", color = Color(0xFF8E2DE2), fontSize = 12.sp)
            }
        }
        items(filtered) { user ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(
                        1.dp,
                        if (user.isAdmin) Color(0xFFFFD700).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.firstName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "${user.firstName} ${user.lastName}".trim().ifEmpty { "Unknown" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (user.isAdmin) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text("👑 Admin", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(user.email.ifEmpty { "No email" }, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text("🔑 ${user.coins} coins", color = Color(0xFFFFE082), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Edit coins button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8E2DE2).copy(alpha = 0.3f))
                                .clickable {
                                    editingUser = user
                                    editCoins = user.coins.toString()
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Edit Coins", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Toggle admin
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (user.isAdmin) Color(0xFFFF5F6D).copy(alpha = 0.2f)
                                    else Color(0xFFFFD700).copy(alpha = 0.2f)
                                )
                                .clickable {
                                    val newVal = !user.isAdmin
                                    db.child(user.uid).child("isAdmin").setValue(newVal)
                                    users = users.map { if (it.uid == user.uid) it.copy(isAdmin = newVal) else it }
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                if (user.isAdmin) "Remove Admin" else "Make Admin",
                                color = if (user.isAdmin) Color(0xFFFF5F6D) else Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ─── MATCHES TAB ───────────────────────────────────────────────────────────────

@Composable
fun AdminMatchesTab() {
    var liveMatches by remember { mutableStateOf<List<AdminMatch>>(emptyList()) }
    var homeTeam by remember { mutableStateOf("") }
    var awayTeam by remember { mutableStateOf("") }
    var homeScore by remember { mutableStateOf("0") }
    var awayScore by remember { mutableStateOf("0") }
    var minute by remember { mutableStateOf("1") }
    var editingMatch by remember { mutableStateOf<AdminMatch?>(null) }

    val db = com.google.firebase.database.FirebaseDatabase
        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
        .getReference("liveMatches")

    DisposableEffect(Unit) {
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<AdminMatch>()
                snapshot.children.forEach { child ->
                    list.add(
                        AdminMatch(
                            id = child.key ?: "",
                            homeTeam = child.child("homeTeam").value?.toString() ?: "",
                            awayTeam = child.child("awayTeam").value?.toString() ?: "",
                            homeScore = (child.child("homeScore").value as? Long)?.toInt() ?: 0,
                            awayScore = (child.child("awayScore").value as? Long)?.toInt() ?: 0,
                            minute = (child.child("minute").value as? Long)?.toInt() ?: 0,
                            status = child.child("status").value?.toString() ?: ""
                        )
                    )
                }
                liveMatches = list
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        db.addValueEventListener(listener)
        onDispose { db.removeEventListener(listener) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Add / Edit Match Form
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color(0xFF8E2DE2).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (editingMatch != null) "✏️ Edit Live Match" else "➕ Push Live Match",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdminTextField("Home Team", homeTeam, { homeTeam = it }, Modifier.weight(1f))
                        AdminTextField("Away Team", awayTeam, { awayTeam = it }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdminTextField("Home Score", homeScore, { homeScore = it }, Modifier.weight(1f), KeyboardType.Number)
                        AdminTextField("Away Score", awayScore, { awayScore = it }, Modifier.weight(1f), KeyboardType.Number)
                        AdminTextField("Minute", minute, { minute = it }, Modifier.weight(1f), KeyboardType.Number)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (homeTeam.isNotEmpty() && awayTeam.isNotEmpty()) {
                                    val matchId = editingMatch?.id ?: db.push().key ?: return@Button
                                    db.child(matchId).setValue(
                                        mapOf(
                                            "homeTeam" to homeTeam,
                                            "awayTeam" to awayTeam,
                                            "homeScore" to (homeScore.toIntOrNull() ?: 0),
                                            "awayScore" to (awayScore.toIntOrNull() ?: 0),
                                            "minute" to (minute.toIntOrNull() ?: 1),
                                            "status" to "IN_PLAY"
                                        )
                                    )
                                    homeTeam = ""; awayTeam = ""; homeScore = "0"; awayScore = "0"; minute = "1"
                                    editingMatch = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (editingMatch != null) "Update" else "Push Live", fontWeight = FontWeight.Bold)
                        }

                        if (editingMatch != null) {
                            OutlinedButton(
                                onClick = {
                                    homeTeam = ""; awayTeam = ""; homeScore = "0"; awayScore = "0"; minute = "1"
                                    editingMatch = null
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) { Text("Cancel") }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "${liveMatches.size} live match(es)",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }

        items(liveMatches) { match ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1B5E20).copy(alpha = 0.4f), Color(0xFF388E3C).copy(alpha = 0.3f))
                        )
                    )
                    .border(1.dp, Color(0xFF38ef7d).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF3D00))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("● LIVE ${match.minute}′", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Text("ID: ${match.id}", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Edit button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF8E2DE2).copy(alpha = 0.4f))
                                    .clickable {
                                        editingMatch = match
                                        homeTeam = match.homeTeam
                                        awayTeam = match.awayTeam
                                        homeScore = match.homeScore.toString()
                                        awayScore = match.awayScore.toString()
                                        minute = match.minute.toString()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("✏️ Edit", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            // Delete button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFF5F6D).copy(alpha = 0.3f))
                                    .clickable { db.child(match.id).removeValue() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("🗑️ Del", color = Color(0xFFFF5F6D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(match.homeTeam, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(
                            "${match.homeScore} - ${match.awayScore}",
                            color = Color(0xFF38ef7d),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                        Text(match.awayTeam, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ─── CONTESTS TAB ──────────────────────────────────────────────────────────────

@Composable
fun AdminContestsTab() {
    var contests by remember { mutableStateOf<List<AdminContest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var contestName by remember { mutableStateOf("") }
    var contestSport by remember { mutableStateOf("Football") }
    var contestMatch by remember { mutableStateOf("") }
    var entryCoins by remember { mutableStateOf("100") }
    var prizeCoins by remember { mutableStateOf("180") }

    val db = com.google.firebase.database.FirebaseDatabase
        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
        .getReference("contests")

    DisposableEffect(Unit) {
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<AdminContest>()
                snapshot.children.forEach { child ->
                    list.add(
                        AdminContest(
                            id = child.key ?: "",
                            name = child.child("name").value?.toString() ?: "",
                            sport = child.child("sport").value?.toString() ?: "",
                            matchName = child.child("matchName").value?.toString() ?: "",
                            entryCoins = (child.child("entryCoins").value as? Long)?.toInt() ?: 0,
                            prizeCoins = (child.child("prizeCoins").value as? Long)?.toInt() ?: 0,
                            joinedCount = child.child("joinedUsers").childrenCount
                        )
                    )
                }
                contests = list
                isLoading = false
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) { isLoading = false }
        }
        db.addValueEventListener(listener)
        onDispose { db.removeEventListener(listener) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Create Contest Form
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color(0xFF8E2DE2).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("➕ Create Contest", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)

                    AdminTextField("Contest Name", contestName, { contestName = it }, Modifier.fillMaxWidth())
                    AdminTextField("Match Name (e.g. Bourne vs Brentf)", contestMatch, { contestMatch = it }, Modifier.fillMaxWidth())

                    // Sport selector
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Football", "Cricket").forEach { sport ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (contestSport == sport) Color(0xFF8E2DE2)
                                        else Color.White.copy(alpha = 0.1f)
                                    )
                                    .clickable { contestSport = sport }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (sport == "Football") "⚽ Football" else "🏏 Cricket",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdminTextField("Entry Coins", entryCoins, { entryCoins = it }, Modifier.weight(1f), KeyboardType.Number)
                        AdminTextField("Prize Coins", prizeCoins, { prizeCoins = it }, Modifier.weight(1f), KeyboardType.Number)
                    }

                    Button(
                        onClick = {
                            if (contestName.isNotEmpty() && contestMatch.isNotEmpty()) {
                                val newRef = db.push()
                                newRef.setValue(
                                    mapOf(
                                        "name" to contestName,
                                        "sport" to contestSport,
                                        "matchName" to contestMatch,
                                        "entryCoins" to (entryCoins.toIntOrNull() ?: 100),
                                        "prizeCoins" to (prizeCoins.toIntOrNull() ?: 180),
                                        "createdAt" to System.currentTimeMillis(),
                                        "result" to mapOf("resolved" to false)
                                    )
                                )
                                contestName = ""; contestMatch = ""; entryCoins = "100"; prizeCoins = "180"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E2DE2)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Contest", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${contests.size} contest(s)", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                if (isLoading) Text("Loading...", color = Color(0xFF8E2DE2), fontSize = 12.sp)
            }
        }

        items(contests) { contest ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(contest.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (contest.sport == "Football") Color(0xFF1565C0).copy(alpha = 0.4f)
                                        else Color(0xFF11998e).copy(alpha = 0.4f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    if (contest.sport == "Football") "⚽" else "🏏",
                                    color = Color.White, fontSize = 9.sp
                                )
                            }
                        }
                        Text(contest.matchName, color = Color(0xFF8E2DE2), fontSize = 11.sp)
                        Text(
                            "Entry: 🔑${contest.entryCoins}  Prize: 🏆${contest.prizeCoins}  Joined: ${contest.joinedCount}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp
                        )
                    }

                    // Delete button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFF5F6D).copy(alpha = 0.2f))
                            .clickable { db.child(contest.id).removeValue() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("🗑️ Delete", color = Color(0xFFFF5F6D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

// ─── HELPER ────────────────────────────────────────────────────────────────────

@Composable
fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFF8E2DE2),
            unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
            focusedBorderColor = Color(0xFF8E2DE2),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
            focusedContainerColor = Color.White.copy(alpha = 0.04f)
        ),
        singleLine = true
    )
}
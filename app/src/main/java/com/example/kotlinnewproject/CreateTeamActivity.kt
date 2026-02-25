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
        setContent {
            CreateTeamScreen()
        }
    }
}

@Composable
fun CreateTeamScreen() {
    val context = LocalContext.current
    var selectedSport by remember { mutableStateOf(0) } // 0 = Football, 1 = Cricket
    var selectedPlayers by remember { mutableStateOf(setOf<String>()) }
    var selectedPosition by remember { mutableStateOf("ALL") }

    val footballPlayers = listOf(
        Player("De Gea", "Man Utd", "GK", 9.0, 120),
        Player("Alisson", "Liverpool", "GK", 10.0, 145),
        Player("Ederson", "Man City", "GK", 9.5, 132),
        Player("Alexander-Arnold", "Liverpool", "DEF", 9.0, 155),
        Player("Trent", "Liverpool", "DEF", 8.5, 140),
        Player("Cancelo", "Man City", "DEF", 8.0, 130),
        Player("Salah", "Liverpool", "MID", 13.0, 210),
        Player("De Bruyne", "Man City", "MID", 12.5, 198),
        Player("Rashford", "Man Utd", "MID", 10.0, 165),
        Player("Haaland", "Man City", "FWD", 14.0, 230),
        Player("Kane", "Bayern", "FWD", 12.0, 195),
        Player("Firmino", "Liverpool", "FWD", 9.0, 150)
    )

    val cricketPlayers = listOf(
        Player("Dhoni", "CSK", "WK", 10.0, 180),
        Player("Buttler", "RR", "WK", 9.5, 165),
        Player("Kohli", "RCB", "BAT", 13.0, 220),
        Player("Rohit", "MI", "BAT", 12.5, 210),
        Player("Warner", "DC", "BAT", 11.0, 190),
        Player("Stokes", "CSK", "AR", 11.5, 195),
        Player("Jadeja", "CSK", "AR", 10.5, 185),
        Player("Hardik", "MI", "AR", 11.0, 188),
        Player("Bumrah", "MI", "BOWL", 10.0, 175),
        Player("Rashid", "GT", "BOWL", 9.5, 168),
        Player("Chahal", "RR", "BOWL", 9.0, 155),
        Player("Shami", "GT", "BOWL", 9.5, 162)
    )

    val footballPositions = listOf("ALL", "GK", "DEF", "MID", "FWD")
    val cricketPositions = listOf("ALL", "WK", "BAT", "AR", "BOWL")

    val currentPlayers = if (selectedSport == 0) footballPlayers else cricketPlayers
    val currentPositions = if (selectedSport == 0) footballPositions else cricketPositions
    val maxPlayers = 11
    val totalBudget = 100.0

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

        Column(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
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
                Text(
                    "Create Team",
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
                            .clickable {
                                selectedSport = index
                                selectedPlayers = setOf()
                                selectedPosition = "ALL"
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
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

            // PLAYER LIST
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredPlayers) { player ->
                    val isSelected = player.name in selectedPlayers
                    val canAdd = !isSelected &&
                            selectedPlayers.size < maxPlayers &&
                            remainingBudget >= player.credits

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                            // Position badge
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

            // SAVE TEAM BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (selectedPlayers.size == maxPlayers)
                            Brush.horizontalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))
                        else
                            Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.Gray.copy(alpha = 0.4f)))
                    )
                    .clickable(enabled = selectedPlayers.size == maxPlayers) { }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (selectedPlayers.size == maxPlayers) "✅ Save Team" else "Select ${maxPlayers - selectedPlayers.size} more players",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
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
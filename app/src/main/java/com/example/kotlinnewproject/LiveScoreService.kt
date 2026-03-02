package com.example.kotlinnewproject

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*

object LiveScoreService {

    private const val API_KEY = "ab525c6736ef4253a78343b517589979"
    private val db = FirebaseDatabase
        .getInstance("https://indvidual-ce210-default-rtdb.firebaseio.com")
        .getReference("liveMatches")

    private var pollingJob: Job? = null

    fun startPolling(scope: CoroutineScope) {
        if (pollingJob?.isActive == true) return
        pollingJob = scope.launch {
            while (isActive) {
                fetchAndPush()
                delay(60_000L)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun fetchAndPush() {
        try {
            val inPlay = withContext(Dispatchers.IO) {
                FootballApi.service.getLiveMatches(API_KEY, "IN_PLAY")
            }
            val paused = withContext(Dispatchers.IO) {
                FootballApi.service.getLiveMatches(API_KEY, "PAUSED")
            }
            val all = inPlay.matches + paused.matches

            if (all.isEmpty()) {
                db.setValue(null)
                return
            }

            all.forEach { match ->
                val data = mapOf(
                    "matchId"     to match.id,
                    "homeTeam"    to (match.homeTeam.shortName ?: match.homeTeam.name),
                    "awayTeam"    to (match.awayTeam.shortName ?: match.awayTeam.name),
                    "homeScore"   to (match.score.fullTime.home ?: 0),
                    "awayScore"   to (match.score.fullTime.away ?: 0),
                    "status"      to match.status,
                    "minute"      to (match.minute ?: 0),
                    "lastUpdated" to System.currentTimeMillis()
                )
                db.child(match.id.toString()).setValue(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
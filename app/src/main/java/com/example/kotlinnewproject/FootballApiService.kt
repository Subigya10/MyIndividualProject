package com.example.kotlinnewproject

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

// Teams
data class TeamResponse(val teams: List<Team>)
data class Team(val id: Int, val name: String, val shortName: String)

// Squad
data class SquadResponse(val squad: List<SquadPlayer>)
data class SquadPlayer(val id: Int, val name: String, val position: String?)

// Matches
data class MatchResponse(val matches: List<ApiMatch>)
data class ApiMatch(
    val id: Int,
    val utcDate: String,
    val status: String,
    val homeTeam: MatchTeam,
    val awayTeam: MatchTeam
)
data class MatchTeam(val id: Int, val name: String, val shortName: String?)

// Live Matches
data class LiveMatchResponse(val matches: List<LiveApiMatch>)
data class LiveApiMatch(
    val id: Int,
    val status: String,
    val minute: Int?,
    val homeTeam: MatchTeam,
    val awayTeam: MatchTeam,
    val score: LiveScore
)
data class LiveScore(
    val fullTime: ScoreDetail,
    val halfTime: ScoreDetail
)
data class ScoreDetail(
    val home: Int?,
    val away: Int?
)

// API interface
interface FootballApiService {
    @GET("competitions/PL/teams")
    suspend fun getPLTeams(
        @Header("X-Auth-Token") token: String
    ): TeamResponse

    @GET("teams/{id}")
    suspend fun getTeamSquad(
        @Header("X-Auth-Token") token: String,
        @Path("id") teamId: Int
    ): SquadResponse

    @GET("competitions/PL/matches?status=SCHEDULED")
    suspend fun getPLMatches(
        @Header("X-Auth-Token") token: String
    ): MatchResponse

    @GET("matches")
    suspend fun getLiveMatches(
        @Header("X-Auth-Token") token: String,
        @Query("status") status: String = "IN_PLAY"
    ): LiveMatchResponse
}

// Retrofit instance
object FootballApi {
    val service: FootballApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.football-data.org/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FootballApiService::class.java)
    }
}
package com.example.kotlinnewproject

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object CreateTeam : Screen("create_team/{matchId}")
}
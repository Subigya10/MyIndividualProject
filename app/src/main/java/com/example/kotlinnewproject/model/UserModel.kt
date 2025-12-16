package com.example.kotlinnewproject.model

data class UserModel(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "",
    val dob: String = "",
    val email: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "firstName" to firstName,
            "lastName" to lastName,
            "gender" to gender,
            "dob" to dob,
            "email" to email
        )
    }
}

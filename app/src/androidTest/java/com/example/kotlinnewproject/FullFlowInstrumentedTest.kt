package com.example.kotlinnewproject

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullFlowInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSignupThenLogin() {

        // ===== STEP 1: SIGNUP =====
        composeRule.onNodeWithTag("firstName")
            .performTextInput("TestUser")

        composeRule.onNodeWithTag("regEmail")
            .performTextInput("flowtest999@gmail.com")

        composeRule.onNodeWithTag("regPassword")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("checkbox")
            .performClick()

        composeRule.onNodeWithTag("signUpBtn")
            .performClick()

        // Wait for Firebase signup + navigate to LoginAct
        Thread.sleep(6000)

        // Check it went to LoginAct
        Intents.intended(hasComponent(LoginAct::class.java.name))

        // ===== STEP 2: LOGIN =====
        composeRule.onNodeWithTag("email")
            .performTextInput("flowtest999@gmail.com")

        composeRule.onNodeWithTag("password")
            .performTextInput("Test@123")

        composeRule.onNodeWithTag("loginBtn")
            .performClick()

        // Wait for Firebase login + navigate to Dashboard
        Thread.sleep(6000)

        // Check it went to Dashboard
        Intents.intended(hasComponent(DashboardActivity::class.java.name))
    }
}
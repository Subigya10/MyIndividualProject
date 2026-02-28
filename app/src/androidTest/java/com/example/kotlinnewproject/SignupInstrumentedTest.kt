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
class SignupInstrumentedTest {

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
    fun testSuccessfulSignup_navigatesToLogin() {
        // Type first name
        composeRule.onNodeWithTag("firstName")
            .performTextInput("TestUser")

        // Type email (use unique email each time!)
        composeRule.onNodeWithTag("regEmail")
            .performTextInput("testuser99@gmail.com")

        // Type password
        composeRule.onNodeWithTag("regPassword")
            .performTextInput("test1234")

        // Type confirm password
        composeRule.onNodeWithTag("confirmPassword")
            .performTextInput("test1234")

        // Check the checkbox
        composeRule.onNodeWithTag("checkbox")
            .performClick()

        // Click sign up button
        composeRule.onNodeWithTag("signUpBtn")
            .performClick()

        // Wait for Firebase to respond
        Thread.sleep(5000)

        // Check it navigated to LoginAct
        Intents.intended(hasComponent(LoginAct::class.java.name))
    }
}
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
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginAct>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Type email
        composeRule.onNodeWithTag("email")
            .performTextInput("subi@gmail.com")

        // Type password
        composeRule.onNodeWithTag("password")
            .performTextInput("subi111")

        // Click login button
        composeRule.onNodeWithTag("loginBtn")
            .performClick()

        // Wait for Firebase to respond
        Thread.sleep(4000)

        // Check it navigated to DashboardActivity
        Intents.intended(hasComponent(DashboardActivity::class.java.name))
    }
}

//    @Test
//    fun testEmptyEmail_doesNotNavigate() {
//        // Leave email empty, only type password
//        composeRule.onNodeWithTag("password")
//            .performTextInput("subi123")
//
//        // Click login button
//        composeRule.onNodeWithTag("loginBtn")
//            .performClick()
//
//        // Should NOT navigate to dashboard
//        assert(Intents.getIntents().isEmpty())
//    }
//
//    @Test
//    fun testEmptyPassword_doesNotNavigate() {
//        // Only type email, leave password empty
//        composeRule.onNodeWithTag("email")
//            .performTextInput("subi@gmail.com")
//
//        // Click login button
//        composeRule.onNodeWithTag("loginBtn")
//            .performClick()
//
//        // Should NOT navigate to dashboard
//        assert(Intents.getIntents().isEmpty())
//    }
//
//    @Test
//    fun testWrongPassword_doesNotNavigate() {
//        // Type correct email but wrong password
//        composeRule.onNodeWithTag("email")
//            .performTextInput("subi@gmail.com")
//
//        composeRule.onNodeWithTag("password")
//            .performTextInput("wrongpassword")
//
//        // Click login button
//        composeRule.onNodeWithTag("loginBtn")
//            .performClick()
//
//        // Wait for Firebase to respond
//        Thread.sleep(4000)
//
//        // Should NOT navigate to dashboard
//        assert(Intents.getIntents().isEmpty())
//    }
//}
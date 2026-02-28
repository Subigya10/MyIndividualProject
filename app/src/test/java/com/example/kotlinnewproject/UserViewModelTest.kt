package com.example.kotlinnewproject

import com.example.kotlinnewproject.repository.UserRepo
import com.example.kotlinnewproject.viewmodel.UserViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class UserViewModelTest {

    // ─── LOGIN TESTS ───────────────────────────────────────────────────────────

    @Test
    fun login_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, "Login success")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.login("test@gmail.com", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Login success", messageResult)
        verify(repo).login(eq("test@gmail.com"), eq("123456"), any())
    }

    @Test
    fun login_wrong_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Login failed: The password is invalid")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq("wrongpass"), any())

        var successResult = false
        var messageResult = ""

        viewModel.login("test@gmail.com", "wrongpass") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Login failed: The password is invalid", messageResult)
        verify(repo).login(eq("test@gmail.com"), eq("wrongpass"), any())
    }

    @Test
    fun login_empty_email_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Login failed: email is empty")
            null
        }.`when`(repo).login(eq(""), eq("123456"), any())

        var successResult = true
        var messageResult = ""

        viewModel.login("", "123456") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        verify(repo).login(eq(""), eq("123456"), any())
    }

    @Test
    fun login_empty_password_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, "Login failed: password is empty")
            null
        }.`when`(repo).login(eq("test@gmail.com"), eq(""), any())

        var successResult = true
        var messageResult = ""

        viewModel.login("test@gmail.com", "") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        verify(repo).login(eq("test@gmail.com"), eq(""), any())
    }

    // ─── REGISTER TESTS ────────────────────────────────────────────────────────

    @Test
    fun register_success_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(true, "Signup success", "uid123")
            null
        }.`when`(repo).register(eq("newuser@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.register("newuser@gmail.com", "123456") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Signup success", messageResult)
        verify(repo).register(eq("newuser@gmail.com"), eq("123456"), any())
    }

    @Test
    fun register_duplicate_email_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String, String) -> Unit>(2)
            callback(false, "Signup failed: The email address is already in use", "")
            null
        }.`when`(repo).register(eq("existing@gmail.com"), eq("123456"), any())

        var successResult = false
        var messageResult = ""

        viewModel.register("existing@gmail.com", "123456") { success, msg, _ ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals("Signup failed: The email address is already in use", messageResult)
        verify(repo).register(eq("existing@gmail.com"), eq("123456"), any())
    }

    @Test
    fun register_empty_fields_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        var successResult = true

        viewModel.register("", "") { success, msg, _ ->
            successResult = success
        }

        // repo.register should still be called since ViewModel doesn't validate
        // Just verify it was called with empty strings
        verify(repo).register(eq(""), eq(""), any())
    }
}
package com.example.travee.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.travee.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController, auth: FirebaseAuth,
                 db: FirebaseFirestore
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2533))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // App logo/illustration
            Image(
                painter = painterResource(id = R.drawable.romantic_getaway_bro_1),
                contentDescription = "Beach illustration",
                modifier = Modifier
                    .size(150.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Join Let's Travel",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Create an account to start your adventure",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // First Name field
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("First Name", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "First Name",
                        tint = Color(0xFF1EBFC3)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF0A2533),
                    unfocusedBorderColor = Color(0xFF1EBFC3).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Last Name field
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Last Name", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Last Name",
                        tint = Color(0xFF1EBFC3)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF0A2533),
                    unfocusedBorderColor = Color(0xFF1EBFC3).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Username", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Username",
                        tint = Color(0xFF1EBFC3)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF0A2533),
                    unfocusedBorderColor = Color(0xFF1EBFC3).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(text="Email",
                    color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color(0xFF1EBFC3)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF0A2533),
                    unfocusedBorderColor = Color(0xFF1EBFC3).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text(text="Password",
                    color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = Color(0xFF1EBFC3)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.eye_off else R.drawable.eye
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF1EBFC3),
                            modifier = Modifier.size(20.dp))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF0A2533),
                    unfocusedBorderColor = Color(0xFF1EBFC3).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Confirm Password field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                placeholder = { Text(text="Confirm Password",
                    color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password",
                        tint = Color(0xFF1EBFC3)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.eye_off else R.drawable.eye
                            ),
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF1EBFC3),
                            modifier = Modifier.size(20.dp))
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    containerColor = Color(0xFF0A2533),
                    unfocusedBorderColor = Color(0xFF1EBFC3).copy(alpha = 0.5f),
                    focusedBorderColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Sign Up button
            Button(
                onClick = {
                    if (firstName.isBlank() || lastName.isBlank() || username.isBlank() ||
                        email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        errorMessage = "Please fill in all fields"
                        return@Button
                    }

                    if (password != confirmPassword) {
                        errorMessage = "Passwords don't match"
                        return@Button
                    }

                    if (password.length < 6) {
                        errorMessage = "Password should be at least 6 characters"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                // Store additional user data in Firestore
                                val user = hashMapOf(
                                    "firstName" to firstName,
                                    "lastName" to lastName,
                                    "username" to username,
                                    "email" to email,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )

                                db.collection("users")
                                    .document(auth.currentUser?.uid ?: "")
                                    .set(user)
                                    .addOnSuccessListener {
                                        navController.navigate("home") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Failed to save user data: ${e.message}"
                                        Log.e("Signup", "Error saving user data", e)
                                    }
                            } else {
                                // Handle specific error cases
                                errorMessage = when (val exception = task.exception) {
                                    is FirebaseNetworkException -> "Network error. Please check your internet connection."
                                    is FirebaseAuthException -> {
                                        when (exception.errorCode) {
                                            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email already in use"
                                            "ERROR_INVALID_EMAIL" -> "Invalid email format"
                                            "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                                            else -> "Authentication failed: ${exception.message}"
                                        }
                                    }
                                    else -> "Sign up failed: ${task.exception?.message}"
                                }
                                Log.e("Signup", "Signup error", task.exception)
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1EBFC3)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(
                        text = "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Already have an account
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                TextButton(onClick = {
                    navController.navigate("login")
                }) {
                    Text(
                        text = "Login",
                        color = Color(0xFF1EBFC3),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
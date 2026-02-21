package com.GR8Studios.souc.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// This component holds just the white slider part content
@Composable
fun LoginFormComponent(onLoginClick: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .padding(top = 32.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 32.dp).align(Alignment.Start).padding(start=32.dp)
        )

        // 📦 The Form Box + Green Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // The White Form Container
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterStart)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(topEnd = 60.dp, bottomEnd = 60.dp))
                    .background(Color.White, RoundedCornerShape(topEnd = 60.dp, bottomEnd = 60.dp))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(topEnd = 60.dp, bottomEnd = 60.dp))
                    .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 60.dp)
            ) {
                Column {
                    // Username Input
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        placeholder = { Text("Username", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // Password Input
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("********", color = Color.LightGray) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 🟢 The Green Floating Arrow Button
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(x = 24.dp)
                    .shadow(12.dp, CircleShape)
                    .background(
                        brush = Brush.linearGradient(colors = listOf(Color(0xFF00E676), Color(0xFF1DE9B6))),
                        shape = CircleShape
                    )
                    .clickable { onLoginClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Login", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        // 🔗 Bottom Links
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Forgot ?",
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End).padding(end = 48.dp).clickable { }
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Register",
            color = Color(0xFFFF5252),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start).padding(start = 32.dp, bottom = 16.dp).clickable { }
        )
    }
}
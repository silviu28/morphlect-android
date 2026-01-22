package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(navController: NavController, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.padding(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text="options",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                navController.navigate("save")
            }) {
                Text("save image")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                navController.navigate("compare")
            }) {
                Text("compare with initial image")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                navController.navigate("settings")
            }) {
                Text("settings")
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
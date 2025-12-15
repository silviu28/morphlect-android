package com.sil.morphlect

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val imageViewModel: PickImageViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = "frontpage") {
        composable("frontpage") {
            Frontpage(navController)
        }
        composable("pick") {
            PickImage(navController, imageViewModel)
        }
        composable("editor") {
            Editor(navController, imageViewModel)
        }
    }
}

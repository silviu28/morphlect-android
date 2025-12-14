package com.sil.morphlect

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.core.net.toUri

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "frontpage") {
        composable("frontpage") {
            Frontpage(navController)
        }
        composable("pick") {
            PickImage(navController)
        }
        composable("editor/{imageUri}", arguments = listOf(navArgument("imageUri") {
            type = NavType.StringType
        })) { navBackStackEntry ->
            val uriStr = navBackStackEntry.arguments?.getString("imageUri")
            val uri = uriStr?.toUri()
            Editor(navController, uri)
        }
    }
}

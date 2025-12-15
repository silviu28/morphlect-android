package com.sil.morphlect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sil.morphlect.view.Editor
import com.sil.morphlect.view.Frontpage
import com.sil.morphlect.view.ImageComparison
import com.sil.morphlect.view.ImageEvaluation
import com.sil.morphlect.view.PickImage
import com.sil.morphlect.view.SaveImage
import com.sil.morphlect.view.Settings
import com.sil.morphlect.view.StyleTransfer
import com.sil.morphlect.view.VibeMatcher
import com.sil.morphlect.viewmodel.EditorViewModel
import com.sil.morphlect.viewmodel.PickImageViewModel

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val imageViewModel: PickImageViewModel = viewModel()
    val editorViewModel: EditorViewModel = viewModel()
    val ctx = LocalContext.current
    val configRepository = remember { AppConfigRepository(ctx) }

    NavHost(
        navController = navController,
        startDestination = "frontpage") {
        composable("*") {
            Frontpage(navController)
        }
        composable("frontpage") {
            Frontpage(navController)
        }
        composable("pick") {
            PickImage(navController, imageViewModel)
        }
        composable("editor") {
            Editor(navController, imageViewModel)
        }
        composable("vibematch") {
            VibeMatcher()
        }
        composable("imageeval") {
            ImageEvaluation()
        }
        composable("styletransfer") {
            StyleTransfer()
        }
        composable("save") {
            SaveImage()
        }
        composable("compare") {
            ImageComparison()
        }
        composable("settings") {
            Settings(configRepository)
        }
    }
}

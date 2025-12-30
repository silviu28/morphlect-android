package com.sil.morphlect

import android.content.SharedPreferences.Editor
import android.os.Build
import androidx.annotation.RequiresApi
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

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val imageViewModel: PickImageViewModel = viewModel()
    val editorViewModel: EditorViewModel = viewModel()
    val ctx = LocalContext.current
    val configRepository = remember { AppConfigRepository(ctx) }
    val presetsRepository = remember { PresetsRepository(ctx) }

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
            Editor(
                navController,
                imageViewModel,
                editorViewModel,
                presetsRepository
            )
        }
        composable("vibematch") {
            VibeMatcher(editorViewModel, navController)
        }
        composable("imageeval") {
            ImageEvaluation(editorViewModel, navController)
        }
        composable("styletransfer") {
            StyleTransfer()
        }
        composable("save") {
            SaveImage(editorViewModel)
        }
        composable("compare") {
            ImageComparison(editorViewModel)
        }
        composable("settings") {
            Settings(configRepository)
        }
    }
}

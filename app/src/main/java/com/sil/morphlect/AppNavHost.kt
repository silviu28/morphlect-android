package com.sil.morphlect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.view.Editor
import com.sil.morphlect.view.Frontpage
import com.sil.morphlect.view.ImageComparison
import com.sil.morphlect.view.ImageEvaluation
import com.sil.morphlect.view.ModelManager
import com.sil.morphlect.view.PickImage
import com.sil.morphlect.view.SaveImage
import com.sil.morphlect.view.Settings
import com.sil.morphlect.view.StyleTransfer
import com.sil.morphlect.view.VibeMatcher
import com.sil.morphlect.view.OnboardingCarousel
import com.sil.morphlect.view.camera.CameraMode
import com.sil.morphlect.view.mxt.MXTComposedView
import com.sil.morphlect.viewmodel.CameraModeViewModel
import com.sil.morphlect.viewmodel.EditorViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun AppNavHost() {
    val navController                            = rememberNavController()
    val editorViewModel: EditorViewModel         = viewModel()
    val cameraModeViewModel: CameraModeViewModel = viewModel()
    val ctx                                      = LocalContext.current
    val configRepository                         = remember { AppConfigRepository(ctx) }
    val presetsRepository                        = remember { PresetsRepository(ctx) }
    val extensionsRepository                     = remember { ExtensionsRepository(ctx) }
    val analyzerFeedFlow                         = remember { MutableSharedFlow<String>() }

    NavHost(
        navController = navController,
        startDestination = "frontpage") {
        composable("*") {
            Frontpage(navController)
        }
        composable("frontpage") {
            Frontpage(navController)
        }
        composable("onboarding") {
            OnboardingCarousel(navController)
        }
        composable("pick") {
            PickImage(navController, editorViewModel)
        }
        composable("camera") {
            CameraMode(
                navController,
                cameraModeViewModel,
                analyzerFeedFlow,
                onCaptureConfirm = { uri ->
                    editorViewModel.loadImage(ctx, uri)
                    navController.navigate("editor")
                },
                presetsRepository,
                extensionsRepository
            )
        }
        composable("editor") {
            Editor(
                navController,
                editorViewModel,
                presetsRepository,
                configRepository,
                extensionsRepository,
            )
        }
        composable("vibe-match") {
            VibeMatcher(editorViewModel, navController)
        }
        composable("image-eval") {
            ImageEvaluation(editorViewModel, navController)
        }
        composable("style-transfer") {
            StyleTransfer()
        }
        composable("save") {
            SaveImage(editorViewModel, navController)
        }
        composable("compare") {
            ImageComparison(editorViewModel, navController)
        }
        composable("settings") {
            Settings(configRepository, navController)
        }
        composable("model-download") {
            ModelManager(navController)
        }
        composable(
            route = "extension-view/{extensionName}",
            arguments = listOf(navArgument("extensionName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val extensionName = backStackEntry.arguments?.getString("extensionName") ?: throw Exception()
            MXTComposedView(
                editorViewModel,
                extensionName,
                onRun = { },
            )
        }
    }
}

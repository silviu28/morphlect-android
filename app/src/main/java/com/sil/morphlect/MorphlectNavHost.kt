package com.sil.morphlect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.sil.morphlect.imgproc.FormatConverters
import com.sil.morphlect.repository.AppConfigRepository
import com.sil.morphlect.repository.ExtensionsRepository
import com.sil.morphlect.repository.FingerprintRepository
import com.sil.morphlect.repository.PresetsRepository
import com.sil.morphlect.view.nav.FingerprintManager
import com.sil.morphlect.view.nav.Frontpage
import com.sil.morphlect.view.nav.ImageComparison
import com.sil.morphlect.view.nav.smart.ImageEvaluation
import com.sil.morphlect.view.nav.ModelManager
import com.sil.morphlect.view.nav.PickImage
import com.sil.morphlect.view.nav.SaveImage
import com.sil.morphlect.view.nav.Settings
import com.sil.morphlect.view.nav.smart.StyleTransfer
import com.sil.morphlect.view.nav.smart.VibeMatcher
import com.sil.morphlect.view.nav.OnboardingCarousel
import com.sil.morphlect.view.nav.studio.Studio
import com.sil.morphlect.view.camera.CameraMode
import com.sil.morphlect.view.nav.smart.MXTComposedView
import com.sil.morphlect.viewmodel.CameraModeViewModel
import com.sil.morphlect.viewmodel.StudioViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun MorphlectNavHost() {
    val navController                            = rememberNavController()
    val studioViewModel: StudioViewModel         = viewModel()
    val cameraModeViewModel: CameraModeViewModel = viewModel()
    val ctx                                      = LocalContext.current
    val configRepository                         = remember { AppConfigRepository(ctx) }
    val presetsRepository                        = remember { PresetsRepository(ctx) }
    val extensionsRepository                     = remember { ExtensionsRepository(ctx) }
    val fingerprintRepository                    = remember { FingerprintRepository(ctx) }
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
            PickImage(navController, studioViewModel)
        }
        composable("camera") {
            CameraMode(
                navController,
                cameraModeViewModel,
                analyzerFeedFlow,
                onCaptureConfirm = { uri ->
                    studioViewModel.loadImage(ctx, uri)
                    navController.navigate("studio")
                },
                presetsRepository,
                extensionsRepository
            )
        }
        composable("studio") {
            Studio(
                navController,
                studioViewModel,
                presetsRepository,
                configRepository,
                extensionsRepository,
            )
        }
        composable("vibe-match") {
            VibeMatcher(studioViewModel, navController)
        }
        composable("image-eval") {
            ImageEvaluation(studioViewModel, navController)
        }
        composable("style-transfer") {
            StyleTransfer()
        }
        composable("save") {
            SaveImage(studioViewModel, navController)
        }
        composable("compare") {
            ImageComparison(
                originalImageBitmap =
                    studioViewModel.originalMat?.let { FormatConverters.matToBitmap(it) },
                layers = studioViewModel.layers,
                navController = navController,
            )
        }
        composable("settings") {
            Settings(configRepository, navController)
        }
        composable("model-download") {
            ModelManager(navController)
        }
        composable("fingerprint-manager") {
            FingerprintManager(fingerprintRepository)
        }
        composable(
            route = "extension-view/{extensionName}",
            arguments = listOf(navArgument("extensionName") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val extensionName = backStackEntry.arguments?.getString("extensionName") ?: throw Exception()
            MXTComposedView(
                studioViewModel,
                extensionName,
                onRun = { },
            )
        }
    }
}

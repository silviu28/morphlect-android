package com.sil.morphlect

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
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
import com.sil.morphlect.view.nav.smart.ImageEvaluation
import com.sil.morphlect.view.nav.ModelManager
import com.sil.morphlect.view.nav.PickImage
import com.sil.morphlect.view.nav.SaveAndCompareImage
import com.sil.morphlect.view.nav.Settings
import com.sil.morphlect.view.nav.smart.StyleTransfer
import com.sil.morphlect.view.nav.smart.VibeMatcher
import com.sil.morphlect.view.nav.OnboardingCarousel
import com.sil.morphlect.view.nav.studio.Studio
import com.sil.morphlect.view.camera.CameraMode
import com.sil.morphlect.view.nav.OnboardingPageContent
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
    val onboardingPages = remember {
        arrayOf(
            OnboardingPageContent(
                title = "morphlect",
                description = "a modern approach to post-processing, right from the comfort of your pocket.",
                imageVector = Icons.Default.CameraAlt,
            ),
            OnboardingPageContent(
                title = "personal fine-tuning",
                description = "apply any filters to your liking. combine them together and find ways to add style to your images.",
                imageVector = Icons.Default.Person
            ),
            OnboardingPageContent(
                title = "intuitive effect application",
                description = "extend your creative potential using personalized machine-learning models, running efficiently right from your device.",
                imageVector = Icons.Default.Animation,
            ),
            OnboardingPageContent(
                title = "extensible",
                description = "want to add a personal feature? just add your own pre-trained TFLite model and start experimenting.",
                imageVector = Icons.Default.Extension,
            ),
            OnboardingPageContent(
                title = "transparent",
                description = "no telemetry collected. most things happen locally on your device, with minor friction between content servers.",
                imageVector = Icons.Default.Star,
            ),
            OnboardingPageContent(
                title = "start creating",
                description = "that's all there is to know. now go ahead and make your pics truly yours!",
                imageVector = Icons.Default.Star,
            )
        )
    }

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
            OnboardingCarousel(
                pages = onboardingPages,
                onNavigate = { route -> navController.navigate(route) }
            )
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
                fingerprintRepository = fingerprintRepository,
            )
        }

        composable("vibe-match") {
            VibeMatcher(
                originalMat = studioViewModel.originalMat,
                onFinished = { evalResult -> studioViewModel.emitEvaluationResult(evalResult) },
                onReturn = { navController.popBackStack() },
            )
        }

        composable("image-eval") {
            ImageEvaluation(
                studioViewModel.previewBitmap,
                onFinished = { evalResult -> studioViewModel.emitEvaluationResult(evalResult) },
                onReturn = { navController.popBackStack() },
            )
        }

        composable("style-transfer") {
            StyleTransfer(
                studioViewModel.previewBitmap,
                onFinished = { evalResult -> studioViewModel.emitEvaluationResult(evalResult) },
                onReturn = { navController.popBackStack() }
            )
        }

        composable("save") {
            SaveAndCompareImage(
                originalImageBitmap =
                    studioViewModel.originalMat?.let { FormatConverters.matToBitmap(it) },
                layers = studioViewModel.layers.toList(),
                onReturn = { navController.popBackStack() },
                fingerprintRepository = fingerprintRepository,
            )
        }

        composable("settings") {
            Settings(configRepository, { route -> navController.navigate(route) })
        }

        composable("model-download") {
            ModelManager({ navController.popBackStack() })
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
            val extensionName = backStackEntry.arguments
                ?.getString("extensionName")
                ?: throw IllegalAccessException("This extension does not exist.")

            MXTComposedView(
                previewBitmap = studioViewModel.previewBitmap,
                extensionName,
                onRun = { },
                onReturn = { navController.popBackStack() }
            )
        }
    }
}

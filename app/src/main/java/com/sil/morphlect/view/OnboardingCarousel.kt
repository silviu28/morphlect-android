package com.sil.morphlect.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sil.morphlect.R
import com.sil.morphlect.view.custom.PixelatedGraphic
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// all page content organized in a tidy class
private data class OnboardingPageContent(
    val title: String,
    val description: String,
    val imageBitmap: ImageBitmap? = null,
    val imageVector: ImageVector? = null,
)

@Composable
fun OnboardingCarousel(navController: NavController) {
    val pages = listOf(
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

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // auto-scroll through slides/pages
    LaunchedEffect(pagerState.currentPage) {
        delay(3500)
        coroutineScope.launch {
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % pages.size)
        }
    }

    Scaffold(modifier = Modifier.padding(18.dp)) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPage(pages[page])
            }

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = { navController.navigate("pick") }) {
                    Text("let's get started")
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(pageContent: OnboardingPageContent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PixelatedGraphic(
            imageBitmap = pageContent.imageBitmap,
            imageVector = pageContent.imageVector,
        )

        Text(
            text = pageContent.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = pageContent.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
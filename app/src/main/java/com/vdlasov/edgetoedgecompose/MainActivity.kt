package com.vdlasov.edgetoedgecompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.vdlasov.edgetoedgecompose.CircleState.Center
import com.vdlasov.edgetoedgecompose.CircleState.Expanded
import com.vdlasov.edgetoedgecompose.CircleState.Start

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                Content()
            }
        }
    }
}

@Composable
private fun Content() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) {
        var clickedStartAnim by remember { mutableStateOf(false) }
        val animationCircleState = remember { mutableStateOf(Start) }

        Image(painter = painterResource(id = R.drawable.sea), contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), contentDescription = "")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopEnd), horizontalAlignment = Alignment.End
            ) {
                Button(onClick = { clickedStartAnim = true }) {
                    Text(text = "start animation")
                }
            }
        }

        LazyColumn(
            contentPadding = WindowInsets.systemBars.asPaddingValues()
        ) {
            items(100) {
                Text(text = "Hello Android", color = White, fontSize = 24.sp)
            }
        }

        CircleAnimation(animationCircleState, clickedStartAnim)

        if (animationCircleState.value == Expanded) {
            AnimatedVisibility(
                visible = clickedStartAnim, enter = fadeIn(
                    animationSpec = tween(delayMillis = 150, easing = EaseIn)
                ), modifier = Modifier.align(Alignment.Center)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(color = Green)
                    )
                    Text(text = "Hello Edge to edge Android!")
                }
            }
        }
    }
}

// Define animation states
private enum class CircleState {
    Start, Center, Expanded
}

// Holds the animation values.
private class TransitionData(
    endPadding: State<Dp>, scale: State<Float>
) {
    val endPadding by endPadding
    val scale by scale
}

// Create a Transition and return its animation values.
@Composable
private fun updateTransitionData(
    circleState: CircleState, circleSize: Dp, circlePadding: Dp, updateState: (CircleState) -> Unit
): TransitionData {
    val halfScreen = LocalConfiguration.current.screenWidthDp / 2
    val transition = updateTransition(circleState, label = "circle anim")
    val moveToCenterAnim = transition.animateDp(label = "move to center",
        transitionSpec = { spring(dampingRatio = DampingRatioMediumBouncy) }) { state ->
        when (state) {
            Start -> 0.dp
            Center, Expanded -> (halfScreen).dp - circleSize + circlePadding
        }
    }
    val expandToFullscreenAnim = transition.animateFloat(label = "expand to fullscreen",
        transitionSpec = { tween(easing = EaseIn) }) { state ->
        when (state) {
            Start, Center -> 1f
            Expanded -> 24f
        }
    }
    if (transition.currentState == Center && transition.targetState == Center) {
        updateState(Expanded)
    }
    return remember(transition) { TransitionData(moveToCenterAnim, expandToFullscreenAnim) }
}

@Composable
private fun BoxScope.CircleAnimation(animationCircleState: MutableState<CircleState>, clickedStartAnim: Boolean) {
    val circleSize = 64.dp
    val circlePadding = 16.dp
    val transitionData = updateTransitionData(
        circleState = animationCircleState.value, circlePadding = circlePadding, circleSize = circleSize
    ) { animationCircleState.value = it }

    if (clickedStartAnim) {
        LaunchedEffect(true) {
            animationCircleState.value = Center
        }
    }

    Box(modifier = Modifier
        .align(BottomEnd)
        .padding(bottom = circlePadding, end = circlePadding + transitionData.endPadding)
        .size(circleSize)
        .scale(transitionData.scale)
        .drawBehind {
            drawCircle(color = White)
        })
}

@Preview(showSystemUi = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        Content()
    }
}
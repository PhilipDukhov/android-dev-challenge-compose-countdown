/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.ui.screens.countdownScreen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.androiddevchallenge.LocalBackPressedDispatcher
import com.example.androiddevchallenge.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun CountdownScreen(
    endDate: Date,
    close: () -> Unit,
) {
    BackPressHandler(close)

    val isInitial = remember { mutableStateOf(true) }
    val finalDate = remember {
        SimpleDateFormat.getTimeInstance(DateFormat.MEDIUM)
            .format(endDate)
    }
    var currentDate by remember { mutableStateOf(Date()) }
    val nextSecondNotifier = remember {
        NextSecondNotifier {
            currentDate = Date()
        }.also {
            it.date = currentDate
        }
    }
    val secondsLeft = (endDate.time - currentDate.time) / 1000L
    if (secondsLeft <= 0) {
        nextSecondNotifier.cancel()
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Finished")
            Button(onClick = close) {
                Text("Great")
            }
        }
        val vibrator = LocalContext.current.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
        return
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            ProgressCircle(
                endDate = endDate,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(20.dp)
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${secondsLeft / 60}:${secondsLeft % 60}",
                    style = MaterialTheme.typography.h1
                )
                Row {
                    Image(
                        painter = painterResource(R.drawable.ic_baseline_notifications_24),
                        contentDescription = "",
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        finalDate,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }
        }
        Button(onClick = close) {
            Text("Cancel")
        }
    }
    isInitial.value = false
}

@Composable
fun ProgressCircle(endDate: Date, modifier: Modifier) {
    val stroke = with(LocalDensity.current) { Stroke(5.dp.toPx()) }
    val color = MaterialTheme.colors.primary
    val sweepAngle = remember {
        Animatable(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = TweenSpec(
                durationMillis = (endDate.time - Date().time).toInt(),
                easing = LinearEasing,
            )
        )
    }
    Canvas(
        modifier = modifier
    ) {
        val innerRadius = (size.minDimension - stroke.width) / 2
        val halfSize = size / 2.0f
        val topLeft = Offset(
            halfSize.width - innerRadius,
            halfSize.height - innerRadius
        )
        val size = Size(innerRadius * 2, innerRadius * 2)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle.value,
            topLeft = topLeft,
            size = size,
            useCenter = false,
            style = stroke
        )
    }
}

fun Animatable(
    initialValue: Float,
    targetValue: Float,
    animationSpec: AnimationSpec<Float>,
) = Animatable(initialValue).apply {
    MainScope().launch {
        animateTo(
            targetValue,
            animationSpec = animationSpec
        )
    }
}

@Composable
fun BackPressHandler(onBackPressed: () -> Unit) {
    // Safely update the current `onBack` lambda when a new one is provided
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)

    // Remember in Composition a back callback that calls the `onBackPressed` lambda
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }
    }

    val backDispatcher = LocalBackPressedDispatcher.current

    // Whenever there's a new dispatcher set up the callback
    DisposableEffect(backDispatcher) {
        backDispatcher.addCallback(backCallback)
        // When the effect leaves the Composition, or there's a new dispatcher, remove the callback
        onDispose {
            backCallback.remove()
        }
    }
}

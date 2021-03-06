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
package com.example.androiddevchallenge.ui.screens.durationSelectionScreen

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

private data class SelectedOffset(
    val lineOffset: Offset = Offset.Zero,
    val selectedOffset: Offset = Offset.Zero
)

data class Duration(val hour: Int = 0, val minute: Int = 0, val second: Int = 0) {
    fun isEmpty() = hour == 0 && minute == 0 && second == 0
    fun finishDate() =
        Date(System.currentTimeMillis() + totalSeconds() * 1000L)
    private fun totalSeconds() = (hour * 60 + minute) * 60 + second
}

interface TimePickerColors {
    val border: BorderStroke

    @Composable
    fun backgroundColor(active: Boolean): State<Color>

    @Composable
    fun textColor(active: Boolean): State<Color>

    fun selectorColor(): Color
    fun selectorTextColor(): Color

    @Composable
    fun periodBackgroundColor(active: Boolean): State<Color>
}

private class DefaultTimePickerColors(
    private val activeBackgroundColor: Color,
    private val inactiveBackgroundColor: Color,
    private val activeTextColor: Color,
    private val inactiveTextColor: Color,
    private val inactivePeriodBackground: Color,
    private val selectorColor: Color,
    private val selectorTextColor: Color,
    borderColor: Color
) : TimePickerColors {
    override val border = BorderStroke(1.dp, borderColor)

    @Composable
    override fun backgroundColor(active: Boolean): State<Color> {
        SliderDefaults
        return rememberUpdatedState(if (active) activeBackgroundColor else inactiveBackgroundColor)
    }

    @Composable
    override fun textColor(active: Boolean): State<Color> {
        return rememberUpdatedState(if (active) activeTextColor else inactiveTextColor)
    }

    override fun selectorColor(): Color {
        return selectorColor
    }

    override fun selectorTextColor(): Color {
        return selectorTextColor
    }

    @Composable
    override fun periodBackgroundColor(active: Boolean): State<Color> {
        return rememberUpdatedState(if (active) activeBackgroundColor else inactivePeriodBackground)
    }
}

object TimePickerDefaults {
    @Composable
    fun colors(
        activeBackgroundColor: Color = MaterialTheme.colors.primary.copy(0.3f),
        inactiveBackgroundColor: Color = MaterialTheme.colors.onBackground.copy(0.3f),
        activeTextColor: Color = MaterialTheme.colors.onPrimary,
        inactiveTextColor: Color = MaterialTheme.colors.onBackground,
        inactivePeriodBackground: Color = Color.Transparent,
        borderColor: Color = MaterialTheme.colors.onBackground,
        selectorColor: Color = MaterialTheme.colors.primary,
        selectorTextColor: Color = MaterialTheme.colors.onPrimary
    ): TimePickerColors {
        return DefaultTimePickerColors(
            activeBackgroundColor = activeBackgroundColor,
            inactiveBackgroundColor = inactiveBackgroundColor,
            activeTextColor = activeTextColor,
            inactiveTextColor = inactiveTextColor,
            inactivePeriodBackground = inactivePeriodBackground,
            selectorColor = selectorColor,
            selectorTextColor = selectorTextColor,
            borderColor = borderColor
        )
    }
}

enum class ClockScreen {
    Hour,
    Minute,
    Second,
    ;

    fun isHour() = this == Hour
    fun isMinute() = this == Minute
    fun isSecond() = this == Second
}

data class TimePickerState(
    val selectedTime: Duration,
    val colors: TimePickerColors,
)

@Composable
fun TimePicker(
    selectedTime: Duration,
    updateTime: (Duration) -> Unit,
    modifier: Modifier = Modifier,
    colors: TimePickerColors = TimePickerDefaults.colors(),
) {
    var currentScreen by remember { mutableStateOf<ClockScreen?>(null) }
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(start = 24.dp, end = 24.dp)
    ) {
        TimeLayout(
            selectedTime, colors, currentScreen
        ) {
            currentScreen = it
        }

        Spacer(modifier = Modifier.height(36.dp))
        Crossfade(currentScreen) {
            when (it) {
                ClockScreen.Hour -> ClockLayout(
                    anchorPoints = 12,
                    label = { index -> if (index == 0) "12" else index.toString() },
                    onAnchorChange = { updateTime(selectedTime.copy(hour = it)) },
                    startAnchor = if (selectedTime.hour == 12) 0 else selectedTime.hour,
                    colors = colors,
                )

                ClockScreen.Minute -> ClockLayout(
                    anchorPoints = 60,
                    label = { index -> index.toString().padStart(2, '0') },
                    onAnchorChange = { updateTime(selectedTime.copy(minute = it)) },
                    startAnchor = selectedTime.minute,
                    isNamedAnchor = { anchor -> anchor % 5 == 0 },
                    colors = colors,
                )
                ClockScreen.Second -> ClockLayout(
                    anchorPoints = 60,
                    label = { index -> index.toString().padStart(2, '0') },
                    onAnchorChange = { updateTime(selectedTime.copy(second = it)) },
                    startAnchor = selectedTime.second,
                    isNamedAnchor = { anchor -> anchor % 5 == 0 },
                    colors = colors,
                )
            }
        }
    }
}

@Composable
fun ClockLabel(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 50.sp,
                    color = textColor
                )
            )
        }
    }
}

@Composable
fun TimeLayout(
    time: Duration,
    colors: TimePickerColors,
    currentScreen: ClockScreen?,
    selectScreen: (ClockScreen) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        ClockLabel(
            text = time.hour.toString(),
            backgroundColor = colors.backgroundColor(currentScreen?.isHour() == true).value,
            textColor = colors.textColor(currentScreen?.isHour() == true).value,
            onClick = { selectScreen(ClockScreen.Hour) },
            modifier = Modifier.weight(1f)
        )

        Box(
            Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ":",
                style = TextStyle(fontSize = 60.sp, color = MaterialTheme.colors.onBackground)
            )
        }

        ClockLabel(
            text = time.minute.toString().padStart(2, '0'),
            backgroundColor = colors.backgroundColor(currentScreen?.isMinute() == true).value,
            textColor = colors.textColor(currentScreen?.isMinute() == true).value,
            onClick = { selectScreen(ClockScreen.Minute) },
            modifier = Modifier.weight(1f)

        )

        Box(
            Modifier
                .width(24.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ":",
                style = TextStyle(fontSize = 60.sp, color = MaterialTheme.colors.onBackground)
            )
        }

        ClockLabel(
            text = time.second.toString().padStart(2, '0'),
            backgroundColor = colors.backgroundColor(currentScreen?.isSecond() == true).value,
            textColor = colors.textColor(currentScreen?.isSecond() == true).value,
            onClick = { selectScreen(ClockScreen.Second) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ClockLayout(
    isNamedAnchor: (Int) -> Boolean = { true },
    anchorPoints: Int,
    label: (Int) -> String,
    startAnchor: Int,
    colors: TimePickerColors,
    onAnchorChange: (Int) -> Unit = {},
    onLift: () -> Unit = {}
) {
    val outerRadius = with(LocalDensity.current) { 100.dp.toPx() }
    val selectedRadius = 70f

    val offset = remember { mutableStateOf(Offset.Zero) }
    val center = remember { mutableStateOf(Offset.Zero) }
    val namedAnchor = remember { mutableStateOf(isNamedAnchor(startAnchor)) }
    val selectedAnchor = remember { mutableStateOf(startAnchor) }

    val anchors = remember {
        val anchors = mutableListOf<SelectedOffset>()
        for (x in 0 until anchorPoints) {
            val angle = (2 * PI / anchorPoints) * (x - 15)
            val selectedOuterOffset = outerRadius.getOffset(angle)
            val lineOuterOffset = (outerRadius - selectedRadius).getOffset(angle)

            anchors.add(
                SelectedOffset(
                    lineOuterOffset,
                    selectedOuterOffset
                )
            )
        }
        anchors
    }

    val anchoredOffset = remember { mutableStateOf(anchors[startAnchor]) }

    fun updateAnchor(newOffset: Offset) {
        val absDiff = anchors.map {
            val diff = it.selectedOffset - newOffset + center.value
            diff.x.pow(2) + diff.y.pow(2)
        }
        val minAnchor = absDiff.withIndex().minByOrNull { (_, f) -> f }?.index
        if (anchoredOffset.value.selectedOffset != anchors[minAnchor!!].selectedOffset) {
            onAnchorChange(label(minAnchor).toInt())

            anchoredOffset.value = anchors[minAnchor]
            namedAnchor.value = isNamedAnchor(minAnchor)
            selectedAnchor.value = minAnchor
        }
    }

    val dragObserver: suspend PointerInputScope.() -> Unit = {
        detectDragGestures(
            onDragEnd = { onLift() }
        ) { change, _ ->
            updateAnchor(change.position)
            change.consumePositionChange()
        }
    }

    val tapObserver: suspend PointerInputScope.() -> Unit = {
        detectTapGestures(
            onPress = {
                updateAnchor(it)
                val success = tryAwaitRelease()
                if (success) {
                    onLift()
                }
            }
        )
    }

    BoxWithConstraints(
        Modifier
            .padding(horizontal = 12.dp)
            .size(256.dp)
            .pointerInput(null, dragObserver)
            .pointerInput(null, tapObserver)
    ) {
        SideEffect {
            center.value =
                Offset(constraints.maxWidth.toFloat() / 2f, constraints.maxWidth.toFloat() / 2f)
            offset.value = center.value
        }

        val inactiveTextColor = colors.textColor(false).value.toArgb()
        val clockBackgroundColor = colors.backgroundColor(false).value
        val selectorColor = remember { colors.selectorColor() }
        val selectorTextColor = remember { colors.selectorTextColor().toArgb() }
        val clockSurfaceDiameter =
            remember(constraints.maxWidth) { constraints.maxWidth.toFloat() / 2f }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(clockBackgroundColor, radius = clockSurfaceDiameter, center = center.value)
            drawCircle(selectorColor, radius = 16f, center = center.value)
            drawLine(
                color = selectorColor,
                start = center.value,
                end = center.value + anchoredOffset.value.lineOffset,
                strokeWidth = 10f,
                alpha = 0.8f
            )

            drawCircle(
                selectorColor,
                center = center.value + anchoredOffset.value.selectedOffset,
                radius = selectedRadius,
                alpha = 0.7f
            )

            if (!namedAnchor.value) {
                drawCircle(
                    Color.White,
                    center = center.value + anchoredOffset.value.selectedOffset,
                    radius = 10f,
                    alpha = 0.8f
                )
            }

            drawIntoCanvas { canvas ->
                for (x in 0 until 12) {
                    val angle = (2 * PI / 12) * (x - 15)
                    val textOuter = label(x * anchorPoints / 12)
                    val textColor = if (selectedAnchor.value == textOuter.toInt()) {
                        selectorTextColor
                    } else {
                        inactiveTextColor
                    }

                    drawText(
                        60f,
                        textOuter,
                        center.value,
                        angle.toFloat(),
                        canvas,
                        outerRadius,
                        color = textColor
                    )
                }
            }
        }
    }
}

private fun drawText(
    textSize: Float,
    text: String,
    center: Offset,
    angle: Float,
    canvas: Canvas,
    radius: Float,
    alpha: Int = 255,
    color: Int = android.graphics.Color.WHITE
) {
    val outerText = Paint()
    outerText.color = color
    outerText.textSize = textSize
    outerText.textAlign = Paint.Align.CENTER
    outerText.alpha = alpha

    val r = Rect()
    outerText.getTextBounds(text, 0, text.length, r)

    canvas.nativeCanvas.drawText(
        text,
        center.x + (radius * cos(angle)),
        center.y + (radius * sin(angle)) + (abs(r.height())) / 2,
        outerText
    )
}


private fun Float.getOffset(angle: Double): Offset =
    Offset((this * cos(angle)).toFloat(), (this * sin(angle)).toFloat())

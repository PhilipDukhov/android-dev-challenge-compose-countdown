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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.util.*

@Composable
fun DurationSelectionScreen(
    start: (Date) -> Unit
) {
    var duration by remember { mutableStateOf(defaultDuration) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TimePicker(
            duration,
            updateTime = {
                duration = it
            },
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
        )
        Button(
            onClick = { start(duration.finishDate()) },
            enabled = !duration.isEmpty(),
        ) {
            Text(text = "Start")
        }
    }
}

private val defaultDuration = Duration(second = 30)
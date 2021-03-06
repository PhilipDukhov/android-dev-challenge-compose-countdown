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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*

class NextSecondNotifier(
    val handler: () -> Unit,
) {
    var date: Date? = null
        set(value) {
            if (field == value) return
            field = value
            job?.cancel()
            job = wait()
        }

    private var job = wait()

    private fun wait(): Job? =
        date?.let { date ->
            GlobalScope.launch {
                delay((1000 - (Date().time - date.time) % 1000))
                if (!isActive) return@launch
                handler()
                job = wait()
            }
        }

    fun cancel() {
        job?.cancel()
    }
}
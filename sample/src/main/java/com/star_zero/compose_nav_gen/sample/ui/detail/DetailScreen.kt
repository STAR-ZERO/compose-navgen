/*
 * Copyright 2021 Kenji Abe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.star_zero.compose_nav_gen.sample.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.star_zero.compose_nav_gen.NavGen

@NavGen("detail")
@Composable
fun DetailScreen(data: String, navController: NavController) {
    Column {
        Button(onClick = { navController.popBackStack() }) {
            Text(text = "Back")
        }

        Text(
            text = data,
            style = MaterialTheme.typography.h3
        )
    }
}

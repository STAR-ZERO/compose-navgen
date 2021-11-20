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
package com.star_zero.compose_nav_gen.sample.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.star_zero.compose_nav_gen.NavGen
import com.star_zero.compose_nav_gen.sample.ui.detail.detail
import kotlin.random.Random

@NavGen("list")
@Composable
fun ListScreen(navController: NavController) {

    val dataList = listOf(
        "Kotlin" to "ことりん",
        "Java" to "じゃゔぁ",
        "Swift" to "すいふと",
        "Dart" to "だーと",
        "Go" to "ごー",
        "Rust" to "らすと",
        "JavaScript" to "じゃゔぁすくりぷと"
    )

    LazyColumn {
        items(dataList) { data ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (Random.nextBoolean()) {
                            navController.detail(
                                lang = data.first,
                                ja = data.second,
                                like = Random.nextBoolean()
                            )
                        } else {
                            navController.detail(
                                lang = data.first,
                                ja = data.second,
                                like = Random.nextBoolean()
                            )
                        }
                    }
                    .padding(16.dp)
            ) {
                Text(text = data.first)
            }
            Divider()
        }
    }
}

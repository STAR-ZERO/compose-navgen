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
package com.star_zero.compose_nav_gen.processor

import com.google.devtools.ksp.symbol.KSType

data class NavGenInfo(
    val fileName: String,
    val packageName: String,
    val functionName: String,
    val navGenName: String,
    val arguments: List<Argument>
) {

    val navArguments: List<Argument.NavArgument> =
        arguments.filterIsInstance<Argument.NavArgument>()
    val navControllerArgs = arguments.filterIsInstance<Argument.NavController>()

    val requiredArgument = navArguments.filter { !it.nullable }
    val optionalArgument = navArguments.filter { it.nullable }

    val route: String = if (navArguments.isEmpty()) {
        navGenName
    } else {

        var route = navGenName
        if (requiredArgument.isNotEmpty()) {
            // e.g. {id}/{name}
            val args = requiredArgument.joinToString(separator = "/") { "{${it.name}}" }
            route = "$route/$args"
        }
        if (optionalArgument.isNotEmpty()) {
            // e.g. id={id}&name={name}
            val args = optionalArgument.joinToString(separator = "&") { "${it.name}={${it.name}}" }
            route = "$route?$args"
        }
        route
    }

    sealed class Argument {
        abstract val name: String

        data class NavController(override val name: String) : Argument()
        data class NavArgument(
            override val name: String,
            val type: KSType,
            val navType: NavType,
            val nullable: Boolean,
        ) : Argument()
    }

    enum class NavType(val value: String) {
        STRING("StringType"),
        INT("IntType")
    }
}

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

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import com.star_zero.compose_nav_gen.DefaultBool
import com.star_zero.compose_nav_gen.DefaultInt
import com.star_zero.compose_nav_gen.DefaultString
import com.star_zero.compose_nav_gen.NavGen
import com.star_zero.compose_nav_gen.NavGenRoutes

data class Types(
    val navGenAnnotation: KSType,
    val defaultIntAnnotation: KSType,
    val defaultBoolAnnotation: KSType,
    val defaultStringAnnotation: KSType,
    val navGenRoutes: KSType,
    val navGraphBuilder: KSType,
    val navController: KSType,
    val string: KSType,
    val int: KSType,
    val bool: KSType,
) {
    companion object {
        fun create(resolver: Resolver): Types {
            val navGenAnnotation = resolver.getKSTypByName(NavGen::class.qualifiedName!!)
            val defaultIntAnnotation = resolver.getKSTypByName(DefaultInt::class.qualifiedName!!)
            val defaultBoolAnnotation = resolver.getKSTypByName(DefaultBool::class.qualifiedName!!)
            val defaultStringAnnotation =
                resolver.getKSTypByName(DefaultString::class.qualifiedName!!)
            val navGenRoutes = resolver.getKSTypByName(NavGenRoutes::class.qualifiedName!!)
            val navGraphBuilder = resolver.getKSTypByName("androidx.navigation.NavGraphBuilder")
            val navController = resolver.getKSTypByName("androidx.navigation.NavController")
            val string = resolver.getKSTypByName(String::class.qualifiedName!!)
            val int = resolver.getKSTypByName(Int::class.qualifiedName!!)
            val bool = resolver.getKSTypByName(Boolean::class.qualifiedName!!)

            return Types(
                navGenAnnotation,
                defaultIntAnnotation,
                defaultBoolAnnotation,
                defaultStringAnnotation,
                navGenRoutes,
                navGraphBuilder,
                navController,
                string,
                int,
                bool
            )
        }
    }
}

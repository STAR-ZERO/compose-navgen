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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.star_zero.compose_nav_gen.NavGen

@KotlinPoetKspPreview
class NavGenProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private lateinit var types: Types

    override fun process(resolver: Resolver): List<KSAnnotated> {
        types = Types.create(resolver)

        printLog("Start process")

        val symbols = resolver.getSymbolsWithAnnotation(NavGen::class.qualifiedName!!)
        symbols.filter { it is KSFunctionDeclaration }.forEach { symbol ->
            val function = symbol as KSFunctionDeclaration

            val navGenAnnotation =
                function.annotations.find { it.annotationType.resolve() == types.navGenAnnotation }!!

            val navGenInfo = parseNavGen(function, navGenAnnotation)

            printLog("Generate code for `${navGenInfo.functionName}`")

            val fileSpec = FileSpec.builder(navGenInfo.packageName, navGenInfo.fileName)
                .addFunction(generateNavGraphFun(navGenInfo))
                .addFunction(generateNavControllerFun(navGenInfo))
                .addProperty(generateNavGenRoutesProperty(navGenInfo))
                .build()

            fileSpec.writeTo(codeGenerator, Dependencies(false, symbol.containingFile!!))
        }

        printLog("Finish process")

        return emptyList()
    }

    /**
     * Parse code from @NavCon
     */
    private fun parseNavGen(
        function: KSFunctionDeclaration,
        navGenAnnotation: KSAnnotation
    ): NavGenInfo {
        val packageName = function.packageName.asString()
        val functionName = function.simpleName.asString()
        val navGenName = navGenAnnotation.getMember<String>("name")
        val arguments = function.parameters.map { parameter ->
            val name = parameter.name!!.asString()
            val type = parameter.type.resolve()
            when (type.makeNotNullable()) { // Judgment by nonnull type
                types.navController -> NavGenInfo.Argument.NavController(parameter.name!!.asString())
                types.string -> {
                    val defaultAnnotation = parameter.annotations.find { it.annotationType.resolve() == types.defaultStringAnnotation }
                    val defaultValue = defaultAnnotation?.getMember<String>("defaultValue")

                    NavGenInfo.Argument.NavArgument(
                        name,
                        type,
                        NavGenInfo.NavType.STRING,
                        type.isMarkedNullable,
                        defaultValue
                    )
                }
                types.int -> {
                    val nullable = type.isMarkedNullable
                    if (nullable) {
                        error("Int argument does not allow nullable")
                    }
                    val defaultAnnotation = parameter.annotations.find { it.annotationType.resolve() == types.defaultIntAnnotation }
                    val defaultValue = defaultAnnotation?.getMember<Int>("defaultValue")

                    NavGenInfo.Argument.NavArgument(
                        name,
                        type,
                        NavGenInfo.NavType.INT,
                        false,
                        defaultValue
                    )
                }
                else -> error("Not supported argument type")
            }
        }

        val fileName = "NavGen${navGenName.replaceFirstChar { it.uppercase() }}"

        return NavGenInfo(
            fileName,
            packageName,
            functionName,
            navGenName,
            arguments
        )
    }

    /**
     * Generate `NavGraphBuilder` extension function.
     */
    private fun generateNavGraphFun(navGenInfo: NavGenInfo): FunSpec {
        val composableName = MemberName("androidx.navigation.compose", "composable")
        val navArgumentName = MemberName("androidx.navigation", "navArgument")
        val navTypeName = MemberName("androidx.navigation", "NavType")
        val functionName = MemberName(navGenInfo.packageName, navGenInfo.functionName)

        return FunSpec.builder(navGenInfo.navGenName)
            .receiver(types.navGraphBuilder.toTypeName())
            .apply {
                navGenInfo.navControllerArgs.forEach { navControllerArg ->
                    addParameter(
                        ParameterSpec.builder(
                            navControllerArg.name,
                            types.navController.toTypeName()
                        ).build()
                    )
                }
                if (navGenInfo.arguments.isEmpty()) {
                    /**
                     * e.g.
                     *
                     * composable("sample") {
                     *   SampleScreen()
                     * }
                     */
                    addCode(
                        """
                            %M(%S) {
                              %M()
                            }
                        """.trimIndent(),
                        composableName,
                        navGenInfo.route,
                        functionName
                    )
                } else {
                    /**
                     * e.g.
                     *
                     * composable("sample/{id}?name={name}",
                     *   arguments = listOf(
                     *     navArgument("id") { type = NavType.IntType },
                     *     navArgument("name") { type = NavType.StringType; nullability = true },
                     *   )
                     * ) { backStackEntry ->
                     *   SampleScreen(
                     *     backStackEntry.arguments!!.getInt("id"),
                     *     backStackEntry.arguments?.getString("name"),
                     *   )
                     * }
                     */
                    if (navGenInfo.navArguments.isNotEmpty()) {
                        addStatement("%M(%S,", composableName, navGenInfo.route)
                        addStatement("  arguments = listOf(")
                        navGenInfo.navArguments.forEach { arg ->
                            val nullable = if (arg.nullable) {
                                "; nullable = true"
                            } else {
                                ""
                            }
                            val defaultValue = if (arg.defaultValue != null) {
                                if (arg.navType == NavGenInfo.NavType.STRING) {
                                    "; defaultValue = \"${arg.defaultValue}\""
                                } else {
                                    "; defaultValue = ${arg.defaultValue}"
                                }
                            } else {
                                ""
                            }
                            addStatement(
                                "    %M(%S) { type = %M.${arg.navType.value}$nullable$defaultValue },",
                                navArgumentName,
                                arg.name,
                                navTypeName
                            )
                        }
                        addStatement("  )")
                        addStatement(") { backStackEntry ->")
                    } else {
                        addStatement("%M(%S) {", composableName, navGenInfo.route)
                    }
                    addStatement("  %M(", functionName)
                    navGenInfo.arguments.forEach { arg ->
                        when (arg) {
                            is NavGenInfo.Argument.NavController -> {
                                addStatement("    ${arg.name},")
                            }
                            is NavGenInfo.Argument.NavArgument -> {
                                when (arg.navType) {
                                    NavGenInfo.NavType.STRING -> {
                                        if (arg.nullable) {
                                            addStatement(
                                                "    backStackEntry.arguments?.getString(%S),",
                                                arg.name
                                            )
                                        } else {
                                            addStatement(
                                                "    backStackEntry.arguments!!.getString(%S)!!,",
                                                arg.name
                                            )
                                        }
                                    }
                                    NavGenInfo.NavType.INT -> {
                                        addStatement(
                                            "    backStackEntry.arguments!!.getInt(%S),",
                                            arg.name
                                        )
                                    }
                                }
                            }
                        }
                    }
                    addStatement("  )")
                    addStatement("}")
                }
            }
            .build()
    }

    /**
     * Generate `NavController` extension function
     */
    private fun generateNavControllerFun(navGenInfo: NavGenInfo): FunSpec {
        return FunSpec.builder(navGenInfo.navGenName)
            .receiver(types.navController.toTypeName())
            .apply {
                if (navGenInfo.navArguments.isEmpty()) {
                    /**
                     * e.g.
                     *
                     * fun NavController.sample() {
                     *   navigate("sample")
                     * }
                     */
                    addStatement("navigate(%S)", navGenInfo.route)
                } else {
                    /**
                     * e.g.
                     *
                     * fun NavController.sample(id: Int, name: String? = null) {
                     *   navigate("sample/$id?name=$name")
                     * }
                     */
                    navGenInfo.navArguments.forEach { arg ->
                        addParameter(
                            ParameterSpec.builder(
                                arg.name,
                                arg.type.toTypeName()
                            ).apply {
                                if (arg.nullable) {
                                    defaultValue("null")
                                } else if (arg.defaultValue != null) {
                                    // default value
                                    if (arg.navType == NavGenInfo.NavType.STRING) {
                                        defaultValue("\"${arg.defaultValue}\"")
                                    } else {
                                        defaultValue("${arg.defaultValue}")
                                    }
                                }
                            }.build()
                        )
                    }
                    // e.g. navigate("routes/${id}?name=${name}")
                    var route = navGenInfo.navGenName
                    if (navGenInfo.requiredArgument.isNotEmpty()) {
                        val args =
                            navGenInfo.requiredArgument.joinToString(separator = "/") { "$" + it.name }
                        route = "$route/$args"
                    }
                    if (navGenInfo.optionalArgument.isNotEmpty()) {
                        val args = navGenInfo.optionalArgument.joinToString(separator = "&") {
                            if (it.nullable) {
                                // nullable
                                // e.g. name=${name ?: ""}
                                "${it.name}=" + "$" + "{${it.name} ?: \"\"}"
                            } else {
                                // default value
                                // e.g. name=$name
                                "${it.name}=" + "$" + it.name
                            }
                        }
                        route = "$route?$args"
                    }
                    addStatement("navigate(%P)", route)
                }
            }
            .build()
    }

    /**
     * Generate property for navigation route value
     */
    private fun generateNavGenRoutesProperty(navGenInfo: NavGenInfo): PropertySpec {
        return PropertySpec.builder(navGenInfo.navGenName, String::class)
            .receiver(types.navGenRoutes.toTypeName())
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %S", navGenInfo.route)
                    .build()
            )
            .build()
    }

    private fun printLog(message: String) {
        logger.logging("[NavGen]: $message")
    }
}

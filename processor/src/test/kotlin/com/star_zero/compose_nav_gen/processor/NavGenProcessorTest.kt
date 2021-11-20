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

import com.google.common.truth.Truth.assertThat
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Test

@KotlinPoetKspPreview
class NavGenProcessorTest {

    private val dummySourceNavigation = SourceFile.kotlin(
        "Navigation.kt",
        """
            package androidx.navigation
            
            class NavController
            class NavGraphBuilder
        """.trimIndent()
    )

    @Test
    fun plain() {
        val navGenName = "test"
        val methodName = "Test"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName() {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName") {
                    $methodName()
                  }
                }
                
                public fun NavController.$navGenName(): Unit {
                  navigate("$navGenName")
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName"
            """.trimIndent()
        )
    }

    @Test
    fun hasStringArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: String) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName/{$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.StringType },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getString("$argName")!!,
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: String): Unit {
                  navigate(${"\"\"\""}$navGenName/${"$"}$argName${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName/{$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasStringNullableArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: String?) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName?$argName={$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.StringType; nullable = true },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments?.getString("$argName"),
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: String? = null): Unit {
                  navigate(${"\"\"\""}$navGenName?$argName=${"$"}{$argName ?: ""}${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName?$argName={$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasStringDefaultArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen
                import com.star_zero.compose_nav_gen.DefaultString

                @NavGen("$navGenName")
                fun $methodName(@DefaultString("abc") $argName: String) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName?$argName={$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.StringType; defaultValue = "abc" },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getString("$argName")!!,
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: String = "abc"): Unit {
                  navigate(${"\"\"\""}$navGenName?$argName=${"$"}$argName${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName?$argName={$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasIntArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: Int) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName/{$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.IntType },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getInt("$argName"),
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: Int): Unit {
                  navigate(${"\"\"\""}$navGenName/${"$"}$argName${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName/{$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasIntDefaultArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen
                import com.star_zero.compose_nav_gen.DefaultInt

                @NavGen("$navGenName")
                fun $methodName(@DefaultInt(123) $argName: Int) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName?$argName={$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.IntType; defaultValue = 123 },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getInt("$argName"),
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: Int = 123): Unit {
                  navigate(${"\"\"\""}$navGenName?$argName=${"$"}$argName${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName?$argName={$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasBoolArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: Boolean) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName/{$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.BoolType },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getBoolean("$argName"),
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: Boolean): Unit {
                  navigate(${"\"\"\""}$navGenName/${"$"}$argName${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName/{$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasBoolDefaultArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen
                import com.star_zero.compose_nav_gen.DefaultBool

                @NavGen("$navGenName")
                fun $methodName(@DefaultBool(true) $argName: Boolean) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(): Unit {
                  composable("$navGenName?$argName={$argName}",
                    arguments = listOf(
                      navArgument("$argName") { type = NavType.BoolType; defaultValue = true },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getBoolean("$argName"),
                    )
                  }
                }
                
                public fun NavController.$navGenName(`$argName`: Boolean = true): Unit {
                  navigate(${"\"\"\""}$navGenName?$argName=${"$"}$argName${"\"\"\""})
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName?$argName={$argName}"
            """.trimIndent()
        )
    }

    @Test
    fun hasNavController() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "navController"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import androidx.navigation.NavController
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: NavController) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()
        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName(navController: NavController): Unit {
                  composable("$navGenName") {
                    $methodName(
                      navController,
                    )
                  }
                }

                public fun NavController.$navGenName(): Unit {
                  navigate("$navGenName")
                }

                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName"
            """.trimIndent()
        )
    }

    @Test
    fun hasComplexArgument() {
        val navGenName = "test"
        val methodName = "Test"
        val argName1 = "data1"
        val argName2 = "data2"
        val argName3 = "data3"
        val argName4 = "data4"
        val argName5 = "data5"
        val argName6 = "data6"
        val argName7 = "data7"
        val argNavController = "navController"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import androidx.navigation.NavController
                import com.star_zero.compose_nav_gen.NavGen
                import com.star_zero.compose_nav_gen.DefaultString
                import com.star_zero.compose_nav_gen.DefaultInt
                import com.star_zero.compose_nav_gen.DefaultBool

                @NavGen("$navGenName")
                fun $methodName(
                    $argName1: Int,
                    $argName2: Boolean,
                    $argName3: String,
                    $argName4: String?,
                    @DefaultString("abc") $argName5: String,
                    @DefaultInt(123) $argName6: Int,
                    @DefaultBool(true) $argName7: Boolean,
                    $argNavController: NavController) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generatedFiles =
            compilation.kspSourcesDir.walkTopDown().filter { it.extension == "kt" }.toList()
        assertThat(generatedFiles).hasSize(1)

        val generatedSource = generatedFiles.first().readText()

        assertThat(generatedSource).contains(
            """
                public fun NavGraphBuilder.$navGenName($argNavController: NavController): Unit {
                  composable("$navGenName/{$argName1}/{$argName2}/{$argName3}?$argName4={$argName4}&$argName5={$argName5}&$argName6={$argName6}&$argName7={$argName7}",
                    arguments = listOf(
                      navArgument("$argName1") { type = NavType.IntType },
                      navArgument("$argName2") { type = NavType.BoolType },
                      navArgument("$argName3") { type = NavType.StringType },
                      navArgument("$argName4") { type = NavType.StringType; nullable = true },
                      navArgument("$argName5") { type = NavType.StringType; defaultValue = "abc" },
                      navArgument("$argName6") { type = NavType.IntType; defaultValue = 123 },
                      navArgument("$argName7") { type = NavType.BoolType; defaultValue = true },
                    )
                  ) { backStackEntry ->
                    $methodName(
                      backStackEntry.arguments!!.getInt("$argName1"),
                      backStackEntry.arguments!!.getBoolean("$argName2"),
                      backStackEntry.arguments!!.getString("$argName3")!!,
                      backStackEntry.arguments?.getString("$argName4"),
                      backStackEntry.arguments!!.getString("$argName5")!!,
                      backStackEntry.arguments!!.getInt("$argName6"),
                      backStackEntry.arguments!!.getBoolean("$argName7"),
                      $argNavController,
                    )
                  }
                }
                
                public fun NavController.$navGenName(
                  $argName1: Int,
                  $argName2: Boolean,
                  $argName3: String,
                  $argName4: String? = null,
                  $argName5: String = "abc",
                  $argName6: Int = 123,
                  $argName7: Boolean = true
                ): Unit {
                  navigate(${"\"\"\""}$navGenName/${"$"}$argName1/${"$"}$argName2/${"$"}$argName3?$argName4=${"$"}{$argName4 ?: ""}&$argName5=${"$"}$argName5&$argName6=${"$"}$argName6&$argName7=${"$"}$argName7${"\"\"\""})
                }
                
                public val NavGenRoutes.$navGenName: String
                  get() = "$navGenName/{$argName1}/{$argName2}/{$argName3}?$argName4={$argName4}&$argName5={$argName5}&$argName6={$argName6}&$argName7={$argName7}"
            """.trimIndent()
        )
    }

    @Test
    fun notSupportedArgumentType() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: Exception) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Not supported argument type")
    }

    @Test
    fun notAllowIntNullable() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: Int?) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Int argument does not allow nullable")
    }

    @Test
    fun notAllowBoolNullable() {
        val navGenName = "test"
        val methodName = "Test"
        val argName = "data"

        val source = SourceFile.kotlin(
            "Test.kt",
            """
                package test
                import com.star_zero.compose_nav_gen.NavGen

                @NavGen("$navGenName")
                fun $methodName($argName: Boolean?) {
                }
            """.trimIndent()
        )
        val compilation = prepareCompilation(source)
        val result = compilation.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Boolean argument does not allow nullable")
    }

    private fun prepareCompilation(source: SourceFile): KotlinCompilation {
        return KotlinCompilation().apply {
            inheritClassPath = true
            sources = listOf(dummySourceNavigation, source)
            symbolProcessorProviders = listOf(NavGenProcessorProvider())
        }
    }
}

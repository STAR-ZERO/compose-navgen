Compose NavGen
===

Generate helper methods for [compose navigation](https://developer.android.com/jetpack/compose/navigation) using [KSP](https://github.com/google/ksp).

:construction: You can try it now, but it's still under development. :construction:

## TODO

- Support argument types.
- Support animation navigation.
- Refactor continuously

## Usage

Add `@NavGen` annotation to each compose screen.

```kt
@NavGen("list")
@Composable
fun ListScreen(navController: NavController) {
    // ...
}

@NavGen("detail")
@Composable
fun DetailScreen(data: String, navController: NavController) {
    // ...
}
```

When build, the processor generates helper methods from `@NavGen`.

<details>
<summary>Generated code</summary>

```kt
// list
fun NavGraphBuilder.list(navController: NavController) {
  composable("list") {
    ListScreen(navController)
  }
}
public fun NavController.list(): Unit {
  navigate("list")
}

// detail
fun NavGraphBuilder.detail(navController: NavController) {
  composable(
  "detail/{data}",
    arguments = listOf(navArgument("data") { type = NavType.StringType })
  ) { backStackEntry ->
    DetailScreen(
      backStackEntry.arguments!!.getString("data")!!,
      navController,
    )
  }
}
fun NavController.detail(`data`: String) {
  navigate("detail/$data")
}
```

</details>

You can use generated code for compose navigation.

```kt
val navController = rememberNavController()
NavHost(navController = navController, startDestination = NavGenRoutes.list) {
    list(navController) // Add route 'list'
    detail(navController) // Add route 'detail/{data}'
}

// navigate to next screen
navController.detail(data) // Call NavController.navigate("detail/$data")
```

### Optional arguments

If you want to use optional arguments, you can use nullable or a default value.

```kt
@NavGen("sample")
@Composable
fun SampleScreen(
    data1: String?,
    @DefaultString("default") data2: String
) {
    // ...
}
```

<details>
<summary>Generated code</summary>

```kt
composable("sample?data1={data1}&data2={data2}",
  arguments = listOf(
    navArgument("data1") { type = NavType.StringType; nullable = true },
    navArgument("data2") { type = NavType.StringType; defaultValue = "default" },
)
) { backStackEntry ->
    // ...
}
```

</details>

If you use nullable or a optional value, no need pass a arguemnt.

```kt
navController.sample()
```

### Set srcDirs

Android Studio does not automatically index generated codes by KSP. You have to add sourceSets manually.

Ref: https://github.com/google/ksp/issues/37

- build.gradle.kts

```kt
android {
    // ...
    applicationVariants.all {
        val variantName = name
        sourceSets {
            getByName("main") {
                java.srcDir(File("build/generated/ksp/$variantName/kotlin"))
            }
        }
    }
}
```
- build.gradle

```
android {
    // ...
    applicationVariants.all { variant ->
        variant.sourceSets.java.each {
            it.srcDirs += "build/generated/ksp/${variant.name}/kotlin"
        }
    }
}
```

## Download

```kt
plugins {
    id("com.google.devtools.ksp") version "1.5.30-1.0.0"
}

dependencies {
    implementation("androidx.navigation:navigation-compose:2.4.0-alpha10")

    implementation("com.star-zero:compose-navgen:1.0.0-alpha02")
    ksp("com.star-zero:compose-navgen-processor:1.0.0-alpha02")
}
```

## License

```
Copyright 2021 Kenji Abe

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

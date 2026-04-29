@file:Suppress("FunctionName")

package com.fitlogic.ai.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitlogic.ai.BuildConfig
import com.fitlogic.ai.feature.auth.AuthScreen
import com.fitlogic.ai.feature.exercises.ExercisesDetailScreen
import com.fitlogic.ai.feature.exercises.ExercisesScreen
import com.fitlogic.ai.feature.home.HomeScreen
import com.fitlogic.ai.feature.nutrition.AddFoodScreen
import com.fitlogic.ai.feature.nutrition.BarcodeScannerScreen
import com.fitlogic.ai.feature.nutrition.FoodDetailScreen
import com.fitlogic.ai.feature.nutrition.ManualAddFoodScreen
import com.fitlogic.ai.feature.nutrition.NutritionScreen
import com.fitlogic.ai.feature.nutrition.mealTypeFromName
import com.fitlogic.ai.feature.onboarding.OnboardingScreen
import com.fitlogic.ai.feature.profile.ProfileScreen
import com.fitlogic.ai.feature.workout.WorkoutScreen

private val bottomTabs =
    listOf(
        FitLogicRoute.Home,
        FitLogicRoute.Workout,
        FitLogicRoute.Exercises,
        FitLogicRoute.Nutrition,
        FitLogicRoute.Profile,
    )

@Composable
fun FitLogicNavHost(navController: NavHostController = rememberNavController()) {
    val appStartViewModel: AppStartViewModel = hiltViewModel()
    val startRoute by appStartViewModel.startRoute.collectAsState()
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    val showBottomBar = bottomTabs.any { tab -> currentDestination?.hierarchy?.any { it.route == tab.route } == true }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(text = tab.label.take(1)) },
                            label = { Text(text = tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(FitLogicRoute.Onboarding.route) {
                OnboardingScreen(
                    onCompleted = {
                        navController.navigate(FitLogicRoute.Auth.route) {
                            popUpTo(FitLogicRoute.Onboarding.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(FitLogicRoute.Auth.route) {
                AuthScreen(
                    googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
                    onAuthenticated = {
                        navController.navigate(FitLogicRoute.Home.route) {
                            popUpTo(FitLogicRoute.Auth.route) { inclusive = true }
                        }
                    },
                )
            }
            composable(FitLogicRoute.Home.route) { HomeScreen() }
            composable(FitLogicRoute.Workout.route) { WorkoutScreen() }
            composable(FitLogicRoute.Exercises.route) {
                ExercisesScreen(
                    onExerciseClick = { exerciseId ->
                        navController.navigate(FitLogicRoute.ExercisesDetail.createRoute(exerciseId))
                    },
                )
            }
            composable(
                route = FitLogicRoute.ExercisesDetail.route,
                arguments = listOf(navArgument("exerciseId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                ExercisesDetailScreen(
                    exerciseId = exerciseId,
                    onBack = { navController.popBackStack() },
                    onExerciseClick = { altId ->
                        navController.navigate(FitLogicRoute.ExercisesDetail.createRoute(altId))
                    },
                )
            }
            composable(FitLogicRoute.Nutrition.route) {
                NutritionScreen(
                    onAddFoodClick = { mealType ->
                        navController.navigate(FitLogicRoute.NutritionAddFood.createRoute(mealType.name))
                    },
                    onOpenBarcode = { mealType ->
                        navController.navigate(FitLogicRoute.NutritionBarcode.createRoute(mealType.name))
                    },
                )
            }
            composable(
                route = FitLogicRoute.NutritionAddFood.route,
                arguments = listOf(navArgument("mealType") { type = NavType.StringType }),
            ) { backStackEntry ->
                val mealType = mealTypeFromName(backStackEntry.arguments?.getString("mealType"))
                AddFoodScreen(
                    targetMealType = mealType,
                    onBack = { navController.popBackStack() },
                    onFoodClick = { foodId, selectedMeal ->
                        navController.navigate(FitLogicRoute.NutritionFoodDetail.createRoute(foodId, selectedMeal.name))
                    },
                    onBarcodeClick = { selectedMeal ->
                        navController.navigate(FitLogicRoute.NutritionBarcode.createRoute(selectedMeal.name))
                    },
                    onManualAddClick = { selectedMeal ->
                        navController.navigate(FitLogicRoute.NutritionManualAdd.createRoute(selectedMeal.name))
                    },
                )
            }
            composable(
                route = FitLogicRoute.NutritionFoodDetail.route,
                arguments = listOf(
                    navArgument("foodId") { type = NavType.StringType },
                    navArgument("mealType") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getString("foodId") ?: return@composable
                val mealType = mealTypeFromName(backStackEntry.arguments?.getString("mealType"))
                FoodDetailScreen(
                    foodId = foodId,
                    mealType = mealType,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack(FitLogicRoute.Nutrition.route, inclusive = false)
                    },
                )
            }
            composable(
                route = FitLogicRoute.NutritionBarcode.route,
                arguments = listOf(navArgument("mealType") { type = NavType.StringType }),
            ) { backStackEntry ->
                val mealType = mealTypeFromName(backStackEntry.arguments?.getString("mealType"))
                BarcodeScannerScreen(
                    targetMealType = mealType,
                    onBack = { navController.popBackStack() },
                    onNavigateToFoodDetail = { foodId, selectedMeal ->
                        navController.navigate(FitLogicRoute.NutritionFoodDetail.createRoute(foodId, selectedMeal.name))
                    },
                    onNavigateToManualAdd = { selectedMeal ->
                        navController.navigate(FitLogicRoute.NutritionManualAdd.createRoute(selectedMeal.name))
                    },
                )
            }
            composable(
                route = FitLogicRoute.NutritionManualAdd.route,
                arguments = listOf(navArgument("mealType") { type = NavType.StringType }),
            ) { backStackEntry ->
                val mealType = mealTypeFromName(backStackEntry.arguments?.getString("mealType"))
                ManualAddFoodScreen(
                    targetMealType = mealType,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack(FitLogicRoute.Nutrition.route, inclusive = false)
                    },
                )
            }
            composable(FitLogicRoute.Profile.route) { ProfileScreen() }
        }
    }
}

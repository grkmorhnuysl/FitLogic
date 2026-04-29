package com.fitlogic.ai.navigation

sealed class FitLogicRoute(
    val route: String,
    val label: String,
) {
    data object Onboarding : FitLogicRoute("onboarding", "Başlangıç")

    data object Auth : FitLogicRoute("auth", "Giriş")

    data object Home : FitLogicRoute("home", "Ana Sayfa")

    data object Workout : FitLogicRoute("workout", "Antrenman")

    data object Exercises : FitLogicRoute("exercises", "Egzersizler")

    data object ExercisesDetail : FitLogicRoute("exercises/detail/{exerciseId}", "Egzersiz Detay") {
        fun createRoute(exerciseId: String) = "exercises/detail/$exerciseId"
    }

    data object Nutrition : FitLogicRoute("nutrition", "Beslenme")

    data object NutritionAddFood : FitLogicRoute("nutrition/add/{mealType}", "Yemek Ekle") {
        fun createRoute(mealType: String) = "nutrition/add/$mealType"
    }

    data object NutritionFoodDetail : FitLogicRoute("nutrition/detail/{foodId}/{mealType}", "Yemek Detay") {
        fun createRoute(
            foodId: String,
            mealType: String,
        ) = "nutrition/detail/$foodId/$mealType"
    }

    data object NutritionBarcode : FitLogicRoute("nutrition/barcode/{mealType}", "Barkod Tara") {
        fun createRoute(mealType: String) = "nutrition/barcode/$mealType"
    }

    data object NutritionManualAdd : FitLogicRoute("nutrition/manual/{mealType}", "Manuel Ekle") {
        fun createRoute(mealType: String) = "nutrition/manual/$mealType"
    }

    data object Stats : FitLogicRoute("stats", "İstatistik")

    data object Coach : FitLogicRoute("coach", "AI Koç")

    data object Profile : FitLogicRoute("profile", "Profil")
}

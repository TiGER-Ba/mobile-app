package com.example.mealplanner.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NutritionApiService {
    @GET("api/food-database/v2/parser")
    suspend fun searchFood(
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("ingr") query: String,
        @Query("nutrition-type") nutritionType: String = "cooking"
    ): Response<FoodSearchResponse>
}
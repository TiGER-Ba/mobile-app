package com.example.mealplanner.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mealplanner.data.model.UserProfile

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = :id")
    fun getUserProfileLiveData(id: String = "user_profile"): LiveData<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun getUserProfile(id: String = "user_profile"): UserProfile?
}
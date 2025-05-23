package com.example.mealplanner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mealplanner.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "🟢 MainActivity onCreate - DÉBUT")

        try {
            // ÉTAPE 1: Initialiser le binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "✅ ÉTAPE 1: Binding créé")

            // ÉTAPE 2: Configuration de base de la navigation
            setupBasicNavigation()
            Log.d(TAG, "✅ ÉTAPE 2: Navigation configurée")

            Log.d(TAG, "🎉 MainActivity onCreate - SUCCÈS COMPLET")

        } catch (e: Exception) {
            Log.e(TAG, "💥 CRASH dans MainActivity onCreate", e)
            // Configuration d'urgence super basique
            handleCrash(e)
        }
    }

    private fun setupBasicNavigation() {
        try {
            // Récupérer le NavHostFragment de manière sûre
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            if (navHostFragment != null) {
                val navController = navHostFragment.navController
                Log.d(TAG, "✅ NavController trouvé")

                // Configuration basic de l'ActionBar (optionnelle)
                try {
                    val appBarConfiguration = AppBarConfiguration(
                        setOf(
                            R.id.navigation_meal_plan,
                            R.id.navigation_food_search,
                            R.id.navigation_recipes,
                            R.id.navigation_nutrition,
                            R.id.navigation_profile
                        )
                    )
                    setupActionBarWithNavController(navController, appBarConfiguration)
                    Log.d(TAG, "✅ ActionBar configurée")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ ActionBar non configurée: ${e.message}")
                }

                // Configuration de la bottom navigation
                binding.navView.setupWithNavController(navController)
                Log.d(TAG, "✅ Bottom Navigation configurée")

            } else {
                Log.e(TAG, "❌ NavHostFragment non trouvé")
                throw Exception("NavHostFragment non trouvé")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur configuration navigation", e)
            throw e
        }
    }

    private fun handleCrash(e: Exception) {
        Log.e(TAG, "🚨 Gestion de crash d'urgence", e)
        try {
            // Version ultra-minimale
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "🆘 Configuration d'urgence appliquée")
        } catch (emergencyException: Exception) {
            Log.e(TAG, "💀 Même la configuration d'urgence a échoué", emergencyException)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val navController = navHostFragment?.navController
            navController?.navigateUp() ?: false || super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur navigation up", e)
            super.onSupportNavigateUp()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "📱 MainActivity onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶️ MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸️ MainActivity onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "⏹️ MainActivity onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🗑️ MainActivity onDestroy")
    }
}
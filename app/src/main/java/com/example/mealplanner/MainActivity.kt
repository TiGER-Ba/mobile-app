package com.example.mealplanner

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mealplanner.data.sync.SyncManager
import com.example.mealplanner.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var syncManager: SyncManager

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "MainActivity onCreate démarré")

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupNavigation()
            setupSyncWithDelay()

            Log.d(TAG, "MainActivity onCreate terminé avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur dans MainActivity onCreate", e)
        }
    }

    private fun setupNavigation() {
        try {
            val navController = findNavController(R.id.nav_host_fragment)

            // Les destinations de niveau supérieur (ne montrent pas de bouton retour)
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
            binding.navView.setupWithNavController(navController)

            Log.d(TAG, "Navigation configurée avec succès")
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la configuration de la navigation", e)
        }
    }

    private fun setupSyncWithDelay() {
        // Configurer la synchronisation avec un délai pour éviter les problèmes d'initialisation
        lifecycleScope.launch {
            try {
                delay(2000) // Délai de 2 secondes pour s'assurer que tout est initialisé
                syncManager.setupPeriodicSync()
                Log.d(TAG, "Synchronisation configurée avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "Erreur lors de la configuration de la synchronisation", e)
                // L'application continue de fonctionner même si la sync échoue
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
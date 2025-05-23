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

        Log.d(TAG, "=== DÉMARRAGE DE L'APPLICATION MEAL PLANNER ===")

        try {
            // Initialiser le binding
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "✅ Binding initialisé avec succès")

            // Configurer la navigation
            setupNavigation()
            Log.d(TAG, "✅ Navigation configurée avec succès")

            // Configurer la synchronisation (avec délai pour éviter les erreurs)
            setupSyncWithDelay()
            Log.d(TAG, "✅ Synchronisation programmée")

            Log.d(TAG, "=== APPLICATION DÉMARRÉE AVEC SUCCÈS ===")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERREUR CRITIQUE lors du démarrage", e)
            // Ne pas faire crash l'application, continuer avec une configuration minimale
            try {
                setupMinimalConfiguration()
            } catch (fallbackError: Exception) {
                Log.e(TAG, "❌ ERREUR lors de la configuration minimale", fallbackError)
            }
        }
    }

    private fun setupNavigation() {
        try {
            val navController = findNavController(R.id.nav_host_fragment)
            Log.d(TAG, "NavController trouvé: ${navController.javaClass.simpleName}")

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
            Log.d(TAG, "✅ ActionBar configurée")

            binding.navView.setupWithNavController(navController)
            Log.d(TAG, "✅ BottomNavigation configurée")

            // Log de la destination actuelle
            navController.addOnDestinationChangedListener { _, destination, _ ->
                Log.d(TAG, "Navigation vers: ${destination.label} (${destination.id})")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la configuration de la navigation", e)
            throw e
        }
    }

    private fun setupMinimalConfiguration() {
        Log.w(TAG, "Configuration minimale en cours...")
        // Configuration de base sans navigation avancée
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "Configuration minimale terminée")
    }

    private fun setupSyncWithDelay() {
        // Configurer la synchronisation avec un délai pour éviter les problèmes d'initialisation
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Démarrage de la configuration de synchronisation...")
                delay(3000) // Délai de 3 secondes pour s'assurer que tout est initialisé

                syncManager.setupPeriodicSync()
                Log.d(TAG, "✅ Synchronisation périodique configurée avec succès")
            } catch (e: Exception) {
                Log.e(TAG, "⚠️ Erreur lors de la configuration de la synchronisation", e)
                // L'application continue de fonctionner même si la sync échoue
                Log.w(TAG, "L'application continuera sans synchronisation automatique")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigateUp() || super.onSupportNavigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la navigation retour", e)
            super.onSupportNavigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "MainActivity - onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity - onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity - onDestroy()")
    }
}
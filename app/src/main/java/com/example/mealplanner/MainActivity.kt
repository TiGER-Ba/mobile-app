package com.example.mealplanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mealplanner.databinding.ActivityMainBinding
import com.example.mealplanner.notifications.MealReminderScheduler
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    // NOUVEAU: Launcher pour demander la permission de notification
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ Permission de notification accordée")
            // Démarrer le service de rappels de repas
            MealReminderScheduler.scheduleDailyMealChecks(this)
            showPermissionGrantedMessage()
        } else {
            Log.w(TAG, "❌ Permission de notification refusée")
            showPermissionDeniedMessage()
        }
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

            // NOUVEAU: ÉTAPE 3: Vérifier et demander les permissions de notification
            checkNotificationPermissions()
            Log.d(TAG, "✅ ÉTAPE 3: Permissions vérifiées")

            // NOUVEAU: ÉTAPE 4: Gérer les intentions provenant des notifications
            handleNotificationIntent()
            Log.d(TAG, "✅ ÉTAPE 4: Intentions gérées")

            Log.d(TAG, "🎉 MainActivity onCreate - SUCCÈS COMPLET")

        } catch (e: Exception) {
            Log.e(TAG, "💥 CRASH dans MainActivity onCreate", e)
            handleCrash(e)
        }
    }

    private fun setupBasicNavigation() {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            if (navHostFragment != null) {
                val navController = navHostFragment.navController
                Log.d(TAG, "✅ NavController trouvé")

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

    // NOUVEAU: Méthode pour vérifier et demander les permissions de notification
    private fun checkNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ Permission de notification déjà accordée")
                    // Démarrer le service de rappels si pas déjà fait
                    MealReminderScheduler.scheduleDailyMealChecks(this)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Log.d(TAG, "📝 Affichage de l'explication pour la permission")
                    showNotificationPermissionRationale()
                }
                else -> {
                    Log.d(TAG, "❓ Demande de permission de notification")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "📱 Version Android < 13, pas besoin de permission explicite")
            MealReminderScheduler.scheduleDailyMealChecks(this)
        }
    }

    // NOUVEAU: Méthode pour gérer les intentions provenant des notifications
    private fun handleNotificationIntent() {
        try {
            val mealId = intent.getStringExtra("meal_id")
            val openNutrition = intent.getBooleanExtra("open_nutrition", false)

            when {
                mealId != null -> {
                    Log.d(TAG, "🍽️ Navigation vers détails du repas: $mealId")
                    // Naviguer vers les détails du repas
                    navigateToMealDetails(mealId)
                }
                openNutrition -> {
                    Log.d(TAG, "📊 Navigation vers nutrition")
                    // Naviguer vers l'onglet nutrition
                    navigateToNutrition()
                }
                else -> {
                    Log.d(TAG, "🏠 Ouverture normale de l'application")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur lors de la gestion des intentions", e)
        }
    }

    private fun navigateToMealDetails(mealId: String) {
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            navHostFragment?.navController?.navigate(
                R.id.navigation_meal_details,
                Bundle().apply { putString("mealId", mealId) }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur navigation vers détails repas", e)
        }
    }

    private fun navigateToNutrition() {
        try {
            binding.navView.selectedItemId = R.id.navigation_nutrition
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erreur navigation vers nutrition", e)
        }
    }

    private fun showNotificationPermissionRationale() {
        Snackbar.make(
            binding.root,
            "Les notifications permettent de vous rappeler vos repas planifiés",
            Snackbar.LENGTH_LONG
        ).setAction("Autoriser") {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }.show()
    }

    private fun showPermissionGrantedMessage() {
        Snackbar.make(
            binding.root,
            "🔔 Rappels de repas activés !",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showPermissionDeniedMessage() {
        Snackbar.make(
            binding.root,
            "⚠️ Les rappels de repas ne fonctionneront pas sans cette permission",
            Snackbar.LENGTH_LONG
        ).setAction("Paramètres") {
            // Optionnel: Ouvrir les paramètres de l'application
        }.show()
    }

    private fun handleCrash(e: Exception) {
        Log.e(TAG, "🚨 Gestion de crash d'urgence", e)
        try {
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Gérer les nouvelles intentions quand l'activité est déjà en cours
        if (intent != null) {
            this.intent = intent
            handleNotificationIntent()
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
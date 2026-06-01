package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AuraRepository
import com.example.data.local.AuraDatabase
import com.example.ui.AuraAppContent
import com.example.ui.AuraViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AuraDatabase
    private lateinit var repository: AuraRepository
    private lateinit var viewModel: AuraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup local Room DB (SQLite cache)
        database = Room.databaseBuilder(
            applicationContext,
            AuraDatabase::class.java,
            "aura_luxury_flagship_database"
        ).fallbackToDestructiveMigration().build()

        repository = AuraRepository(database.dao(), applicationContext)
        
        // Define Custom ViewModelFactory
        val factory = AuraViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuraViewModel::class.java]

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = false) {
                AuraAppContent(viewModel = viewModel)
            }
        }
    }
}

class AuraViewModelFactory(private val repository: AuraRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class representation: ${modelClass.name}")
    }
}

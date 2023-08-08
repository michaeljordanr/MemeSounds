package com.michaeljordanr.memesounds.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.michaeljordanr.memesounds.ui.theme.MemesoundsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MemesoundsTheme {
                MemeSoundsScreen()
            }
        }
    }
}
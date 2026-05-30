package it.roadies.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import it.roadies.android_app.ui.theme.AndroidappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidappTheme {
                RoadiesApp()
            }
        }
    }
}
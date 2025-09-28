package com.pykens.earthzoo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pykens.earthzoo.ui.InteractiveEarthView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        val selectedContinentLabel: TextView = findViewById(R.id.selected_continent_label)
        val earthView: InteractiveEarthView = findViewById(R.id.interactive_earth)

        earthView.setOnContinentSelectedListener { continentName ->
            selectedContinentLabel.text = getString(R.string.selected_continent_format, continentName)
        }
    }
}

package com.pykens.earthzoo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pykens.earthzoo.ui.InteractiveEarthView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectedContinentLabel: TextView = findViewById(R.id.selected_continent_label)
        val earthView: InteractiveEarthView = findViewById(R.id.interactive_earth)

        earthView.setOnContinentSelectedListener { continentName ->
            selectedContinentLabel.text = getString(R.string.selected_continent_format, continentName)
        }
    }
}

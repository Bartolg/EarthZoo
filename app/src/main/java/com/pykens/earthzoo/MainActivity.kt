package com.pykens.earthzoo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import gov.nasa.worldwind.WorldWind
import gov.nasa.worldwind.WorldWindow
import gov.nasa.worldwind.geom.LookAt
import gov.nasa.worldwind.layer.AtmosphereLayer
import gov.nasa.worldwind.layer.BackgroundLayer
import gov.nasa.worldwind.layer.BlueMarbleLayer
import gov.nasa.worldwind.layer.CompassLayer
import gov.nasa.worldwind.navigator.LookAtNavigator
import gov.nasa.worldwind.controller.BasicWorldWindowController

class MainActivity : AppCompatActivity() {

    private lateinit var worldWindow: WorldWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        worldWindow = findViewById(R.id.world_window)
        setupGlobe()
    }

    override fun onResume() {
        super.onResume()
        worldWindow.onResume()
    }

    override fun onPause() {
        worldWindow.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        worldWindow.onDestroy()
        super.onDestroy()
    }

    private fun setupGlobe() {
        worldWindow.worldWindowController = BasicWorldWindowController()

        worldWindow.layers.addLayer(BackgroundLayer())
        worldWindow.layers.addLayer(BlueMarbleLayer())
        worldWindow.layers.addLayer(AtmosphereLayer())
        worldWindow.layers.addLayer(CompassLayer())

        val lookAt = LookAt()
        lookAt.set(0.0, 0.0, 0.0, WorldWind.ABSOLUTE, 6.5e6, 0.0, 45.0, 0.0)

        val navigator = worldWindow.navigator
        if (navigator is LookAtNavigator) {
            navigator.lookAt = lookAt
        } else {
            worldWindow.navigator = LookAtNavigator().also { it.lookAt = lookAt }
        }
    }
}

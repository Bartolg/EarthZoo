package com.pykens.earthzoo

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val earthImage: ImageView = findViewById(R.id.earth_image)
        earthImage.setImageResource(R.drawable.earth)
    }
}

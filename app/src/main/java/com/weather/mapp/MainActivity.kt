package com.weather.mapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    var button: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        popMenu()
        supportActionBar?.hide();
    }

    fun popMenu() {
        button = findViewById<View>(R.id.imageButtom) as ImageButton

        button!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val popupMenu = PopupMenu(this@MainActivity, button)
                popupMenu.menuInflater.inflate(R.menu.side_menu, popupMenu.menu)
                popupMenu.show()
            }
        })
    }
}
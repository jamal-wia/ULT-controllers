package com.navigation.ult_controllers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.fragmentcontrollers.core.fragment_navigation_controllers.FragmentNavigationController

class AppActivity : AppCompatActivity() {

    private lateinit var navigationController: FragmentNavigationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        navigationController = supportFragmentManager.findFragmentById(R.id.app_container)
                as? FragmentNavigationController ?: FragmentNavigationController.Builder()
            .addScreen(NumberFragment.createInstance(1))
            .addScreen(NumberFragment.createInstance(2))
            .addScreen(NumberFragment.createInstance(3))
            .addScreen(NumberFragment.createInstance(4))
            .showInContainer(R.id.app_container, this)
            .build()
    }

    override fun onBackPressed() {
        if (navigationController.goBack()) return
        finish()
    }
}

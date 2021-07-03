package com.navigation.ult_controllers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fragmentcontrollers.core.fragment_navigation_controllers.FragmentNavigationController

class AppActivity : AppCompatActivity() {

    private lateinit var navigationController: FragmentNavigationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        println("$this onCreate")
        navigationController =
            supportFragmentManager.findFragmentByTag("app_navigationController")
                    as? FragmentNavigationController ?: FragmentNavigationController.Builder()
                .ignoreEqualScreen(true)
                .show(R.id.app_container, supportFragmentManager, "app_navigationController")
                .build()

        navigationController.goForward(ColorFragment.createInstance())
        navigationController.goForward(ColorFragment.createInstance())
        navigationController.goForward(ColorFragment.createInstance())

//        Handler().postDelayed({
//            navigationController.replace(ColorFragment.createInstance())
//        }, 3000)
    }

    override fun onBackPressed() {
        if (navigationController.goBack()) return
        finish()
    }
}

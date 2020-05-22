package hu.csomorbalazs.mydiary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.*
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logoAnimation = TranslateAnimation(0f, 0f, 100f, 0f).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    startActivity(Intent(this@SplashScreen, MainListActivity::class.java))
                    finish()
                }

                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        val textAnimation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 600
        }

        appName.startAnimation(textAnimation)
        logo.startAnimation(logoAnimation)
    }
}

package jp.dev.daisken.mysplash

import android.animation.ObjectAnimator
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import jp.dev.daisken.mysplash.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle the splash screen transition.
        //TODO 6.SplashScreenを有効にします
        val splashScreen = installSplashScreen()

        // Check if the initial data is ready; start drawing.
        //  true : keep splash screen
        //  false: finish splash screen
        //TODO 6.これは必須な処理ではありませんが、
        // KeepOnScreenConditionにtrueを渡している間はSplashScreenが表示されつづけます
        // falseを渡すとSplashScreenが終了します
        // OSが定期的にこの値を参照して終了タイミングを図っているいるようです
        // 例えば、初期データの読み込み中はSplashScreenを表示しつづけ、
        // 初期データの読み込みが完了したタイミングでSplashScreenを終了させる、というような使い方ができます
        // 注意：The condition is evaluated before each request to draw the application, so it needs to be fast to avoid blocking the UI.
        splashScreen.setKeepOnScreenCondition {
            !isReady
        }

        // Animation Vector Drawableのアニメーション自体が終わる前？に
        // setOnExitAnimationListenerが動いてしまうと？スプラッシュスクリーンが一瞬チラつくことがありました
        //TODO 7.setOnExitAnimationListenerをセットしなかった場合は、アプリの最初の画面が描画可能な状態になると
        // SplashScreenは自動的に終了されます
        // setOnExitAnimationListenerをセットした場合は、SplashScreenViewProvider.remove を呼びだすまで
        // SplashScreenは終了されません
        // また、SplashScreen終了時のアニメーションを指定できます
        // アニメーションは通常のObjectAnimatorなどを使ってカスタムすることができます
        // https://developer.android.com/guide/topics/ui/splash-screen#customize-animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create your custom animation.
            val fadeout = ObjectAnimator.ofFloat(splashScreenView.view, "alpha", 1f, 0f)
            fadeout.duration = 3000L  //フェードアウトアニメーションをわかりやすくするため3秒にしています

            // Call SplashScreenView.remove at the end of your custom animation.
            fadeout.doOnEnd {
                splashScreenView.remove()
            }

            // Get the duration of the animated vector drawable.
            //TODO SplashScreenに表示したAnimatedVectorDrawableのアニメーションの残り時間を算出できます
            // 例えば、アニメーションがまだ終了していないうちに、画面描画の準備が整うとsetOnExitAnimationListener
            // が呼び出されてしまうので、アニメーションを最後まで見せたい場合は、
            // 残り時間分delayさせてSplashScreenの終了を遅らせるようなこともできます
            val animationDurationMillis = splashScreenView.iconAnimationDurationMillis
            // Get the start time of the animation.
            val animationStartMillis = splashScreenView.iconAnimationStartMillis
            val currentMillis = System.currentTimeMillis()
            // Calculate the remaining duration of the animation.
            val remainingDurationMillis = (animationDurationMillis - (currentMillis - animationStartMillis))
                    .coerceAtLeast(0L)

            lifecycleScope.launch {
                delay(remainingDurationMillis)

                // Run your animation.
                fadeout.start()
            }
        }



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        // for test asynchronous data loading, etc.
        lifecycleScope.launch {
            delay(2000)
            isReady = true
        }
    }
}
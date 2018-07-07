package com.hmproductions.bingo.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import com.hmproductions.bingo.BingoActionServiceGrpc
import com.hmproductions.bingo.R
import com.hmproductions.bingo.actions.GetSessionIdRequest
import com.hmproductions.bingo.dagger.ContextModule
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent
import com.hmproductions.bingo.ui.main.MainActivity
import com.hmproductions.bingo.utils.ConnectionUtils.*
import com.hmproductions.bingo.utils.Constants
import com.hmproductions.bingo.utils.Constants.SERVER_ADDRESS
import com.hmproductions.bingo.utils.Constants.SERVER_PORT
import io.grpc.ManagedChannel
import kotlinx.android.synthetic.main.activity_splash_screen.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    companion object {
        val SHOW_SNACKBAR_KEY = "show-snackbar-key"
    }

    @Inject
    lateinit var channel: ManagedChannel

    private lateinit var flybySound: MediaPlayer

    private var actionServiceBlockingStub: BingoActionServiceGrpc.BingoActionServiceBlockingStub? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        DaggerBingoApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)

        flybySound = MediaPlayer.create(this, R.raw.flyby)
        flybySound.setVolume(0.8f, 0.8f)
        flybySound.start()

        appNameTextView.animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out_animation)

        if (!isGooglePlayServicesAvailable(this)) {
            val playServicesBuilder = AlertDialog.Builder(this)
            playServicesBuilder
                    .setMessage("Bingo requires latest version of google play services.")
                    .setPositiveButton("Close") { dialogInterface, i -> finish() }
                    .setTitle("Update PlayServices")
                    .setCancelable(true)
                    .show()
        }

        actionServiceBlockingStub = BingoActionServiceGrpc.newBlockingStub(channel)

        appNameTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out_animation))

        Handler().postDelayed({ this.startMainActivity() }, 1000)
    }

    private fun startMainActivity() {


        if (!getConnectionInfo(this)) {
            loadingTextView!!.setText(R.string.internet_unavailable)
            Handler().postDelayed({
                Snackbar
                        .make(findViewById<View>(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY") { v -> startMainActivity() }
                        .show()
            }, 500)
            return
        }

        doAsync {
            if (getConnectionInfo(this@SplashActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

                val data = actionServiceBlockingStub?.getSessionId(GetSessionIdRequest.newBuilder().setTime(System.currentTimeMillis()).build());

                uiThread {
                    Constants.SESSION_ID = data?.sessionId

                    val mainActivityIntent = Intent(this@SplashActivity, MainActivity::class.java)
                    mainActivityIntent.putExtra(SHOW_SNACKBAR_KEY, true)

                    startActivity(mainActivityIntent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(this@SplashActivity, appNameTextView, appNameTextView.transitionName).toBundle())
                    finish()
                }

            } else {
                uiThread {
                    loadingTextView.setText(R.string.server_unreachable)

                    Handler().postDelayed({
                        Snackbar
                                .make(findViewById<View>(android.R.id.content), "Couldn't connect to server", Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY") { v -> startMainActivity() }
                                .show()
                    }, 500)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        flybySound.stop()
    }
}
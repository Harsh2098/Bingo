package com.hmproductions.bingo.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AnimationUtils
import com.hmproductions.bingo.BingoActionServiceGrpc
import com.hmproductions.bingo.R
import com.hmproductions.bingo.actions.GetSessionIdRequest
import com.hmproductions.bingo.actions.ReconnectRequest
import com.hmproductions.bingo.actions.ReconnectResponse
import com.hmproductions.bingo.dagger.ContextModule
import com.hmproductions.bingo.dagger.DaggerBingoApplicationComponent
import com.hmproductions.bingo.ui.main.MainActivity
import com.hmproductions.bingo.ui.main.RoomFragment.ROOM_NAME_BUNDLE_KEY
import com.hmproductions.bingo.ui.main.RoomFragment.TIME_LIMIT_BUNDLE_KEY
import com.hmproductions.bingo.utils.ConnectionUtils.*
import com.hmproductions.bingo.utils.Constants
import com.hmproductions.bingo.utils.Constants.*
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import kotlinx.android.synthetic.main.activity_splash_screen.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import javax.inject.Inject

class SplashActivity : AppCompatActivity() {

    companion object {
        const val SHOW_SNACKBAR_KEY = "show-snack-bar-key"
    }

    @Inject
    lateinit var channel: ManagedChannel

    private lateinit var flybySound: MediaPlayer

    private lateinit var actionServiceBlockingStub: BingoActionServiceGrpc.BingoActionServiceBlockingStub

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        DaggerBingoApplicationComponent.builder().contextModule(ContextModule(this)).build().inject(this)
        actionServiceBlockingStub = BingoActionServiceGrpc.newBlockingStub(channel)

        flybySound = MediaPlayer.create(this, R.raw.flyby)
        flybySound.setVolume(0.8f, 0.8f)
        flybySound.start()

        appNameTextView.animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out_animation)

        if (!isGooglePlayServicesAvailable(this)) {

            alert("Bingo requires latest version of google play services", "Update PlayServices") {
                isCancelable = false
                positiveButton("Close") {
                    finish()
                }
            }.show()
        }

        appNameTextView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out_animation))

        Handler().postDelayed({ this.startMainActivity() }, 1000)
    }

    private fun startMainActivity() {
        if (!getConnectionInfo(this)) {
            loadingTextView!!.setText(R.string.internet_unavailable)
            Handler().postDelayed({
                val snackBar = Snackbar
                        .make(findViewById<View>(android.R.id.content), "Please check internet connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY") { _ -> startMainActivity() }

                snackBar.view.backgroundColor = ContextCompat.getColor(this@SplashActivity, R.color.dark_blue_background)
                snackBar.show()
            }, 500)
            return
        }

        if (Constants.SESSION_ID == null)
            getNewSessionIdAsync()
        else
            reconnectAsync(Constants.SESSION_ID)
    }

    private fun getNewSessionIdAsync() {
        doAsync {
            if (getConnectionInfo(this@SplashActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

                val data = actionServiceBlockingStub.getSessionId(GetSessionIdRequest.newBuilder().setTime(System.currentTimeMillis()).build())

                uiThread {
                    Constants.SESSION_ID = data?.sessionId

                    val mainActivityIntent = Intent(this@SplashActivity, MainActivity::class.java)
                    mainActivityIntent.putExtra(SHOW_SNACKBAR_KEY, true)

                    startActivity(mainActivityIntent, ActivityOptionsCompat
                            .makeSceneTransitionAnimation(this@SplashActivity, appNameTextView, appNameTextView.transitionName).toBundle())
                    finish()
                }

            } else {
                uiThread { showServerUnreachable() }
            }
        }
    }

    private fun reconnectAsync(sessionId: String) {
        doAsync {
            if (getConnectionInfo(this@SplashActivity) && isReachableByTcp(SERVER_ADDRESS, SERVER_PORT)) {

                val metadata = Metadata()
                val metadataKey: Metadata.Key<String> = Metadata.Key.of(SESSION_ID_KEY, Metadata.ASCII_STRING_MARSHALLER)
                metadata.put(metadataKey, sessionId)

                val data = MetadataUtils.attachHeaders(actionServiceBlockingStub, metadata).reconnect(ReconnectRequest.newBuilder().setSessionsId(sessionId).build())

                uiThread {
                    when (data.statusCode) {
                        ReconnectResponse.StatusCode.OK -> {
                            with(Intent(this@SplashActivity, MainActivity::class.java)) {
                                action = Constants.RECONNECT_ACTION
                                putExtra(MainActivity.PLAYER_LEFT_KEY, false)
                                putExtra(GameActivity.PLAYER_ID, data.playerId)
                                putExtra(GameActivity.ROOM_ID, data.roomId)
                                putExtra(TIME_LIMIT_BUNDLE_KEY, data.timeLimit)
                                putExtra(ROOM_NAME_BUNDLE_KEY, data.roomName)

                                startActivity(this, ActivityOptionsCompat.makeSceneTransitionAnimation(
                                        this@SplashActivity, appNameTextView, appNameTextView.transitionName).toBundle())
                                finish()
                            }
                        }

                        ReconnectResponse.StatusCode.SESSION_ID_NOT_EXIST -> {
                            Constants.SESSION_ID = null
                            startMainActivity()
                        }

                        else -> {
                            Constants.SESSION_ID = null
                            showServerUnreachable()
                        }
                    }
                }

            } else {
                uiThread { showServerUnreachable() }
            }
        }
    }

    private fun showServerUnreachable() {
        loadingTextView.setText(R.string.server_unreachable)

        Handler().postDelayed({
            val snackBar = Snackbar
                    .make(findViewById<View>(android.R.id.content), "Couldn't connect to server", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY") { _ -> startMainActivity() }

            snackBar.view.backgroundColor = ContextCompat.getColor(this@SplashActivity, R.color.dark_blue_background)
            snackBar.show()
        }, 500)
    }

    override fun onPause() {
        super.onPause()
        flybySound.stop()
    }
}
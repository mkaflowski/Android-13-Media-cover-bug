package com.example.testproject

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.util.*

class MyService : MediaBrowserServiceCompat() {

    val TAG = "Test App"
    lateinit var iv: ImageView
    lateinit var coverBitmap: Bitmap
    lateinit var mediaSessionCompat: MediaSessionCompat

    private val CHANNEL_ID = "media_playback_channel"
    val NOTIFICATION_ID = 345

    override fun onCreate() {
        super.onCreate()
        iv = ImageView(this)
        coverBitmap =
            BitmapFactory.decodeResource(this.resources, R.drawable.e)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        initMediaSession()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        coverBitmap =
            BitmapFactory.decodeResource(this.resources, R.drawable.e)
        updateMetadata("init", "init", "init", "https://github.com/mkaflowski/Media-Style-Palette/blob/master/app/src/main/res/drawable-xhdpi/e.png")


        when (intent?.extras?.getString("ACTION")) {
            "NAME_CHANGE" -> {
                startWithNameChange()
            }
            "ONLY_COVER_CHANGE" -> {
                startNoNameChange()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }


    private fun startWithNameChange() {
        Handler().postDelayed({
            setSongAfterCoverLoad(
                "Album 1",
                "Artist 1",
                "Name 1",
                "https://github.com/mkaflowski/Media-Style-Palette/blob/master/app/src/main/res/drawable-xhdpi/f.png?raw=true"
            )
        }, 1500)


        Handler().postDelayed({
            setSongAfterCoverLoad(
                "Album 2",
                "Artist 2",
                "Name 2",
                "https://github.com/mkaflowski/Media-Style-Palette/blob/master/app/src/main/res/drawable-xhdpi/d.png?raw=true"
            )
        }, 3500)
    }

    private fun startNoNameChange() {
        Handler().postDelayed({
            setSongAfterCoverLoad(
                "Album 1",
                "Artist 1",
                "Name 1",
                "https://github.com/mkaflowski/Media-Style-Palette/blob/master/app/src/main/res/drawable-xhdpi/f.png?raw=true"
            )
        }, 1500)


        Handler().postDelayed({
            setSongAfterCoverLoad(
                "Album 1",
                "Artist 1",
                "Name 1",
                "https://github.com/mkaflowski/Media-Style-Palette/blob/master/app/src/main/res/drawable-xhdpi/d.png?raw=true"
            )
        }, 3500)
    }

    private fun setSongAfterCoverLoad(
        album: String,
        artist: String,
        name: String,
        coverUrl: String
    ) {
        var url =
            coverUrl

        Picasso.get().load(url).into(iv, object : Callback {
            override fun onSuccess() {
                coverBitmap = iv.drawable.toBitmap()
                updateMetadata(album, artist, name, url)
            }

            override fun onError(e: Exception?) {
//                TODO("Not yet implemented")
                Log.e("ERROR","ERRROR")
            }
        })
    }

    private fun makeNotification() {
        val notification: Notification = buildNotification() ?: return
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (true) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            stopForeground(false)
            mNotificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification? {

        val notificationIntent = Intent(this, MainActivity::class.java)
        val backIntent = Intent(this, MainActivity::class.java)
        backIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentIntent = PendingIntent.getActivities(
            this,
            55,
            arrayOf(backIntent, notificationIntent),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID)

        builder.setSmallIcon(R.drawable.e)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setShowCancelButton(true)
                    .setMediaSession(mediaSessionCompat.sessionToken)
            )
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColorized(true)
            .setColor(ContextCompat.getColor(this, R.color.black))
            .setContentIntent(contentIntent)
            .setContentTitle("A")
            .setContentText("B")
            .setSmallIcon(R.drawable.e)
            .setAutoCancel(true)
            .setLargeIcon(coverBitmap)

        return builder.build()
    }

    private fun updateMetadata(album: String, artist: String, name: String, songCoverUrl: String) {
        val builder = MediaMetadataCompat.Builder()
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, name)
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "id" + Random().nextInt(999))
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, songCoverUrl)
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, coverBitmap)

        if (mediaSessionCompat.isActive)
            mediaSessionCompat.setMetadata(builder.build())
        makeNotification()
    }

    private fun setMediaPlaybackState(state: Int) {
        val position = 10L
        val playbackStateCompat = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_FAST_FORWARD or PlaybackStateCompat.ACTION_REWIND or
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP
            )
            .setState(state, position, 1f, SystemClock.elapsedRealtime())
            .build()
        mediaSessionCompat.setPlaybackState(playbackStateCompat)
    }

    private fun initMediaSession() {
        val mediaButtonReceiver = ComponentName(this, MediaButtonReceiver::class.java)
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 99 /*request code*/,
            intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        mediaSessionCompat =
            MediaSessionCompat(applicationContext, "MediaTAG", mediaButtonReceiver, pi)

        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        mediaSessionCompat.isActive = true
        sessionToken = mediaSessionCompat.sessionToken
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // The id of the channel.
        val id: String = CHANNEL_ID
        // The user-visible name of the channel.
        val name: CharSequence = "Media playback"
        // The user-visible description of the channel.
        val description = "Media playback controls"
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(id, name, importance)
        // Configure the notification channel.
        mChannel.description = description
        mChannel.setShowBadge(false)
        mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mNotificationManager.createNotificationChannel(mChannel)
    }


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }

}
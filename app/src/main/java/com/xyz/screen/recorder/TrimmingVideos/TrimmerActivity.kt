package com.xyz.screen.recorder.TrimmingVideos

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lb.video_trimmer_library.interfaces.VideoTrimmingListener
import com.xyz.screen.recorder.R
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants
import com.xyz.screen.recorder.CoderlyticsServices.SwimCameraViewService.context
import kotlinx.android.synthetic.main.activity_trimmer.*
import java.io.File

class TrimmerActivity : AppCompatActivity(), VideoTrimmingListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        }
        Log.i("iaminf", "TrimmerActivity= oncreate " )
      val inputVideoUri: Uri?  = Uri.parse(intent.getStringExtra(CoderlyticsConstants.VIDEO_EDIT_URI_KEY))

        if (inputVideoUri == null) {
            finish()
            return
        }

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, inputVideoUri)
        val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
        retriever.release()

        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, inputVideoUri)
        val parseLong = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(9)).toInt() / 1000 + 1000

        if (duration != null) {
            videoTrimmerView.setMaxDurationInMs(duration.toInt())
        }

        videoTrimmerView.setOnK4LVideoListener(this)
        val parentFolder = getExternalFilesDir(null)!!
        parentFolder.mkdirs()
        val fileName = "trimmedVideo_${System.currentTimeMillis()}.mp4"

        val sb5 = StringBuilder()
        sb5.append(Environment.getExternalStorageDirectory())
        sb5.append(File.separator)
        sb5.append(CoderlyticsConstants.APPDIR)

        val parfldr=File(sb5.toString())
        val trimmedVideoFile = File(parfldr, fileName)
        videoTrimmerView.setDestinationFile(trimmedVideoFile)
        videoTrimmerView.setVideoURI(inputVideoUri)
        videoTrimmerView.setVideoInformationVisibility(true)
    }

    override fun onTrimStarted() {
        trimmingProgressView.visibility = View.VISIBLE
    }

    override fun onFinishedTrimming(uri: Uri?) {
        trimmingProgressView.visibility = View.GONE
        if (uri == null) {
            Toast.makeText(this@TrimmerActivity, "failed trimming", Toast.LENGTH_SHORT).show()
        } else {

            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setDataAndType(uri, "video/mp4")
            startActivity(intent)
        }
        finish()
    }

    override fun onErrorWhileViewingVideo(what: Int, extra: Int) {
        trimmingProgressView.visibility = View.GONE
        Toast.makeText(this@TrimmerActivity, "error while previewing video", Toast.LENGTH_SHORT).show()
    }

    override fun onVideoPrepared() {

    }

    fun File.getMediaDuration(context: Context): Long
    {
        if (!exists()) return 0
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(absolutePath))
        val duration = retriever.extractMetadata(METADATA_KEY_DURATION)
        retriever.release()

        return duration?.toLongOrNull() ?: 0
    }
}

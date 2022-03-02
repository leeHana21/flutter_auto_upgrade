package com.example.ttt_flutter

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import com.example.ttt_flutter.MainActivity.Companion.TAG
import java.io.File


/*
  getExternalFilesDir(DIRECTORY_DOWNLOADS) : /storage/sdcard0/Android/data/package/files/Download
  getCacheDir()	/data/data/package/cache
  getFilesDir()	/data/data/package/files
  getFilesDir().getParent()	/data/data/package
  Environment.getDataDirectory()	/data
  Environment.getDownloadCacheDirectory()	/cache
  Environment.getRootDirectory()	/system`
*/

class DownloadController(
    private val context: Context,
    private val url: String
) {

    companion object {
        private const val FILE_NAME = "patientSafe.apk"
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    var downloadManager: DownloadManager? = null
    var apkOutputFile: File? = null
    var mDownloadQueueId: Long? = null

    fun enqueueDownload(): Long? {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        var outputFilePath =
            context.getExternalFilesDir(DIRECTORY_DOWNLOADS)

        val outputFile = File(outputFilePath, FILE_NAME)
        Log.d(TAG, "enqueueDownload: $outputFilePath")
        Log.d(TAG, "enqueueDownload: ${outputFile.path}")

        if (outputFile.exists()) outputFile.delete()

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri)
        request.setTitle("스마트 환자 안전 관리 Download")
        request.setDescription("다운로드 중입니다.")
        request.setAllowedOverMetered(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION)
        request.setDestinationUri(Uri.fromFile(outputFile))

        apkOutputFile = outputFile
        mDownloadQueueId = downloadManager?.enqueue(request)

        return mDownloadQueueId
    }
}
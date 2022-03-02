package com.example.ttt_flutter

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.provider.TedPermissionProvider
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    companion object {
        const val TAG = "auto download & install"
        private const val CHANNEL = "DOWNLOAD_CHANNEL"
        private const val REMOTE_KEY_APP_VERSION = "android_version"
        private const val REMOTE_KEY_APP_URL = "android_url"

        private const val METHOD_DOWNLOAD = "APK_DOWNLOAD"
        private const val METHOD_INSTALL = "APK_INSTALL"
    }

    private lateinit var downloadController: DownloadController
    private var downLoadResult: MethodChannel.Result? = null
    private var installResult: MethodChannel.Result? = null


    override fun onResume() {
        super.onResume()
        val completeFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver, completeFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadCompleteReceiver)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                METHOD_DOWNLOAD -> {
                    val apkLink = call.argument<String>("apkLink")!!
                    Log.d("configureFlutterEngine", "configureFlutterEngine: $apkLink")
                    apkDownload(apkLink)
                    downLoadResult = result
                }
                METHOD_INSTALL -> {
                    apkInstall()
                    installResult = result
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun apkDownload(remoteUrl: String) {
        downloadController = DownloadController(this, remoteUrl).apply {
            Log.d(
                TAG,
                "apkDownload: ${TedPermissionProvider.context.applicationContext.packageName}"
            )
            enqueueDownload()
        }
    }

    private fun apkInstall() {
        downloadController.apkOutputFile?.let {
            if (Build.VERSION.SDK_INT >= 24) { // Android Nougat ( 7.0 ) and later
                val fileUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    it
                )
                //안될 때는...!!!!!!!! com.example.ttt_flutter.fileprovider
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_VIEW)
                val apkUri = Uri.fromFile(it)
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                applicationContext.startActivity(intent)
            }
        } ?: run {
        }
    }

    private val downloadCompleteReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadController.mDownloadQueueId == reference) {
                val query = DownloadManager.Query() // 다운로드 항목 조회에 필요한 정보 포함
                query.setFilterById(reference)

                downloadController.downloadManager?.let {
                    val cursor: Cursor = it.query(query)
                    cursor.moveToFirst()
                    val columnIndex: Int = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val columnReason: Int = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                    val status: Int = cursor.getInt(columnIndex)
                    val reason: Int = cursor.getInt(columnReason)
                    cursor.close()

                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downLoadResult?.success("SUCCESS")
                            apkInstall()
                        }
                        DownloadManager.STATUS_PAUSED -> {
                            downLoadResult?.error(
                                "PAUSED",
                                "다운로드가 중지 되었습니다.$status $reason",
                                "PAUSED"
                            )
                            // 다운로드 중단 처리
                        }
                        DownloadManager.STATUS_FAILED -> {
                            downLoadResult?.error(
                                "FAILED",
                                "다운로드가 취소 되었습니다.$status $reason",
                                "FAILED"
                            )
                            // 다운로드 취소 처리
                        }
                        else -> {
                            downLoadResult?.error(
                                "ERROR",
                                "다운로드중 오류가 발생하였습니다.$status $reason",
                                "ERROR"
                            )
                        }
                    }
                }
            }
        }
    }
}

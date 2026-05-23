package com.systemleveling.core.ota

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtaUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun checkForUpdate(currentVersionCode: Int): OtaUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get(RELEASES_URL) {
                header("Accept", "application/vnd.github.v3+json")
                header("User-Agent", "SoloLevelingSystem-OTA/1.0")
            }
            val release = json.decodeFromString<GitHubRelease>(response.bodyAsText())
            val remoteVersionCode = release.tagName.removePrefix("v").toIntOrNull()
                ?: return@withContext null
            if (remoteVersionCode <= currentVersionCode) return@withContext null
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
            OtaUpdateInfo(
                versionCode = remoteVersionCode,
                displayName = release.name,
                releaseNotes = release.body ?: "",
                downloadUrl = apkAsset?.browserDownloadUrl,
                htmlUrl = release.htmlUrl
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun downloadAndInstall(downloadUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val updateDir = File(context.getExternalFilesDir(null), "ota").also { it.mkdirs() }
            val apkFile = File(updateDir, "update.apk")
            val bytes = httpClient.get(downloadUrl).body<ByteArray>()
            apkFile.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    companion object {
        private const val RELEASES_URL =
            "https://api.github.com/repos/danghoangsqtt-sys/solo-leveling-system/releases/latest"
    }
}

data class OtaUpdateInfo(
    val versionCode: Int,
    val displayName: String,
    val releaseNotes: String,
    val downloadUrl: String?,
    val htmlUrl: String
)

@Serializable
private data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String,
    @SerialName("body") val body: String? = null,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("assets") val assets: List<GitHubAsset> = emptyList()
)

@Serializable
private data class GitHubAsset(
    @SerialName("name") val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)

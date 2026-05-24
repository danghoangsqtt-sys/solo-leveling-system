package com.systemleveling.core.sync

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.systemleveling.core.BuildConfig
import com.systemleveling.core.database.dao.CourseDao
import com.systemleveling.core.database.dao.LessonDao
import com.systemleveling.core.database.entity.CourseEntity
import com.systemleveling.core.database.entity.LessonEntity
import com.systemleveling.core.model.CourseContentType
import com.systemleveling.core.model.ItemRarity
import com.systemleveling.core.settings.SettingsManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteSyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient,
    private val json: Json,
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val settingsManager: SettingsManager
) {
    companion object {
        private const val TAG = "AppwriteSync"
    }

    private val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    /**
     * Sync courses from Appwrite MODULES collection → Room DB.
     * @param apiKey Appwrite API Key với quyền databases.read
     * @return Result với số khóa học đã import
     */
    suspend fun syncCourses(apiKey: String): Result<Int> {
        return try {
            val endpoint = settingsManager.appwriteEndpoint.first()
            val projectId = settingsManager.appwriteProjectId.first()
            val databaseId = settingsManager.appwriteDatabaseId.first()
            val collectionId = settingsManager.appwriteCollectionId.first()
            var importedCount = 0
            var offset = 0
            val limit = 100
            var hasMore = true

            while (hasMore) {
                val url = "$endpoint/databases/$databaseId/collections/$collectionId/documents?queries[]=limit($limit)&queries[]=offset($offset)"
                if (BuildConfig.DEBUG) Log.d(TAG, "Fetching from Appwrite: $url")

                val response = httpClient.get(url) {
                    headers {
                        append("X-Appwrite-Project", projectId)
                        append("X-Appwrite-Key", apiKey)
                        append("Content-Type", "application/json")
                    }
                }

                if (response.status != HttpStatusCode.OK) {
                    val errorBody = response.bodyAsText()
                    Log.e(TAG, "Appwrite error ${response.status}: $errorBody")
                    return Result.failure(Exception("Lỗi Appwrite: ${response.status} — $errorBody"))
                }

                val raw = response.bodyAsText()
                val docsResponse = lenientJson.decodeFromString<AppwriteDocumentsResponse>(raw)
                val batchDocs = docsResponse.documents
                if (BuildConfig.DEBUG) Log.d(TAG, "Fetched ${batchDocs.size} documents at offset $offset")

                if (batchDocs.size < limit) {
                    hasMore = false
                }
                if (batchDocs.isEmpty()) {
                    break
                }

                var batchImportedCount = 0

                batchDocs
                    .filter { it.moduleType == "course_tree" && it.data.isNotBlank() }
                    .forEach { doc ->
                        try {
                            val tree = lenientJson.decodeFromString<CourseNodeDto>(doc.data)
                            batchImportedCount += importNode(tree, parentCourseId = null, depth = 0)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse doc ${doc.id}: ${e.message}")
                        }
                    }

                // Nếu không tìm thấy course_tree, thử parse tất cả documents có data
                if (batchImportedCount == 0) {
                    batchDocs
                        .filter { it.data.isNotBlank() }
                        .forEach { doc ->
                            try {
                                val tree = lenientJson.decodeFromString<CourseNodeDto>(doc.data)
                                batchImportedCount += importNode(tree, parentCourseId = null, depth = 0)
                            } catch (_: Exception) { }
                        }
                }
                
                importedCount += batchImportedCount
                offset += limit
            }

            if (BuildConfig.DEBUG) Log.d(TAG, "Import done: $importedCount courses/lessons")
            Result.success(importedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }

    /**
     * Import từ raw JSON array (flat list of CourseNodeDto) — không cần Appwrite API key.
     * Format: kết quả paste trực tiếp từ web app's course data.
     */
    suspend fun syncFromNodeArray(jsonArray: String): Result<Int> {
        return try {
            val nodes = lenientJson.decodeFromString<List<CourseNodeDto>>(jsonArray)
            var count = 0
            nodes
                .filter { it.id != "root" && it.id.isNotBlank() }
                .forEach { node ->
                    try {
                        count += importNode(node, parentCourseId = null, depth = 0)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to import node ${node.id}: ${e.message}")
                    }
                }
            if (BuildConfig.DEBUG) Log.d(TAG, "JSON array import done: $count items")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "JSON array import failed", e)
            Result.failure(e)
        }
    }

    /**
     * Seed dữ liệu từ file JSON bundled trong assets (courses_seed.json).
     * Gọi lần đầu khi DB rỗng để pre-populate toàn bộ khóa học.
     */
    suspend fun seedFromBundledAsset(): Result<Int> {
        return try {
            val jsonArray = context.assets.open("courses_seed.json").bufferedReader().use { it.readText() }
            syncFromNodeArray(jsonArray)
        } catch (e: Exception) {
            Log.e(TAG, "seedFromBundledAsset failed", e)
            Result.failure(e)
        }
    }

    /**
     * Đệ quy import CourseNode tree:
     * - folder (depth=0) → CourseEntity
     * - folder (depth>0) → CourseEntity với parentId context
     * - file → LessonEntity trong course gần nhất
     */
    private suspend fun importNode(
        node: CourseNodeDto,
        parentCourseId: String?,
        depth: Int,
        orderIndex: Int = -1
    ): Int {
        var count = 0

        when (node.type) {
            "folder" -> {
                // Tạo CourseEntity mới cho folder
                val courseId = node.id.ifBlank { java.util.UUID.randomUUID().toString() }
                val contentType = inferContentType(node)
                val rarity = when (depth) {
                    0 -> ItemRarity.UNCOMMON
                    1 -> ItemRarity.RARE
                    else -> ItemRarity.COMMON
                }

                val course = CourseEntity(
                    id = courseId,
                    title = node.title,
                    author = node.topic ?: "Khóa học",
                    description = node.notes ?: "",
                    totalModules = node.children.count { it.type == "file" },
                    rewardExp = 100L * (depth + 1),
                    rarity = rarity,
                    contentType = contentType,
                    category = node.topic ?: "",
                    isPinned = node.isPinned
                )
                courseDao.insertCourse(course)
                count++

                var currentLessonCount = 0
                // Import con
                node.children.forEach { child ->
                    if (child.type == "file") {
                        count += importNode(child, courseId, depth + 1, currentLessonCount)
                        currentLessonCount++
                    } else {
                        count += importNode(child, courseId, depth + 1)
                    }
                }
            }

            "file" -> {
                if (parentCourseId == null) {
                    // File ở root level → tạo course đơn giản
                    val lessonData = node.data
                    val courseId = node.id.ifBlank { java.util.UUID.randomUUID().toString() }
                    courseDao.insertCourse(
                        CourseEntity(
                            id = courseId,
                            title = node.title,
                            author = "Tài liệu",
                            description = node.notes ?: "",
                            totalModules = 1,
                            rewardExp = 50L,
                            contentUrl = lessonData?.url ?: "",
                            contentType = mapContentType(lessonData?.type),
                            category = node.topic ?: ""
                        )
                    )
                    count++
                } else {
                    // File trong folder → tạo Lesson
                    val lessonData = node.data
                    val actualOrderIndex = if (orderIndex >= 0) orderIndex else lessonDao.getTotalCountSync(parentCourseId)
                    lessonDao.insertLesson(
                        LessonEntity(
                            id = node.id.ifBlank { java.util.UUID.randomUUID().toString() },
                            courseId = parentCourseId,
                            title = node.title,
                            contentUrl = lessonData?.url ?: "",
                            contentType = mapContentType(lessonData?.type),
                            orderIndex = actualOrderIndex,
                            notes = node.notes ?: ""
                        )
                    )
                    count++
                }
            }
        }

        return count
    }

    private fun inferContentType(node: CourseNodeDto): CourseContentType {
        val childTypes = node.children.mapNotNull { it.data?.type }.toSet()
        return when {
            childTypes.contains("VIDEO") -> CourseContentType.VIDEO
            childTypes.contains("PDF") -> CourseContentType.PDF
            childTypes.contains("DOCX") -> CourseContentType.EBOOK
            childTypes.contains("HTML") -> CourseContentType.ARTICLE
            else -> CourseContentType.GENERAL
        }
    }

    private fun mapContentType(appwriteType: String?): CourseContentType = when (appwriteType?.uppercase()) {
        "PDF" -> CourseContentType.PDF
        "VIDEO" -> CourseContentType.VIDEO
        "DOCX" -> CourseContentType.EBOOK
        "HTML" -> CourseContentType.ARTICLE
        else -> CourseContentType.GENERAL
    }
}

// ── Appwrite response DTOs ────────────────────────────────────────────────────

@Serializable
private data class AppwriteDocumentsResponse(
    val total: Int = 0,
    val documents: List<AppwriteModuleDocument> = emptyList()
)

@Serializable
private data class AppwriteModuleDocument(
    @SerialName("\$id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("module_type") val moduleType: String = "",
    val data: String = "",
    @SerialName("\$createdAt") val createdAt: String = ""
)

// ── CourseNode DTOs (khớp với web app TypeScript types) ──────────────────────

@Serializable
private data class CourseNodeDto(
    val id: String = "",
    val title: String = "",
    val type: String = "folder",
    val children: List<CourseNodeDto> = emptyList(),
    val data: LessonContentDto? = null,
    val isPinned: Boolean = false,
    val topic: String? = null,
    val notes: String? = null
)

@Serializable
private data class LessonContentDto(
    val id: String = "",
    val title: String = "",
    val url: String = "",
    val type: String = "HTML",
    val content: String? = null,
    val notes: String? = null
)

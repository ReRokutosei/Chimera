/*
 * Based on ImageToolbox, an image editor for android
 * Original work Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
 * Modified work Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program. If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * This file contains modifications from the original source code.
 * Original source: https://github.com/T8RIN/ImageToolbox
 */

package com.t8rin.embeddedpicker.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.t8rin.embeddedpicker.domain.MediaRetriever
import com.t8rin.embeddedpicker.domain.model.Album
import com.t8rin.embeddedpicker.domain.model.AllowedMedia
import com.t8rin.embeddedpicker.domain.model.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Serializable

class AndroidMediaRetriever(
    private val context: Context
) : MediaRetriever {
    
    private val cacheManager = CacheManager(context)
    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            // 媒体内容发生变化时清除缓存
            cacheManager.clear()
        }
        
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            // 媒体内容发生变化时清除缓存
            cacheManager.clear()
        }
    }
    
    init {
        // 注册内容观察者
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
        context.contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }
    
    override fun getAlbums(): Flow<List<Album>> = flow {
        // 尝试从缓存获取
        val cachedAlbums: List<Album>? = cacheManager.get("albums")
        if (cachedAlbums != null) {
            emit(cachedAlbums)
            return@flow
        }
        
        val albums: List<Album> = withContext(Dispatchers.IO) {
            val albumList = mutableListOf<Album>()
            // 不再添加默认的"All"相册，让查询结果决定是否显示相册选择器

            val projection = arrayOf(
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.MIME_TYPE
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            
            // 使用Bundle优化查询 (Android 10+)
            val bundle =
                Bundle().apply {
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(MediaStore.Images.Media.DATE_MODIFIED)
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }

            // 只查询图片，不查询视频
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                bundle,
                null
            )

            cursor?.use { 
                val bucketIdColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
                val bucketNameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val relativePathColumn =
                    it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

                while (it.moveToNext()) {
                    try {
                        val bucketId = it.getLong(bucketIdColumn)
                        val bucketName = it.getString(bucketNameColumn) ?: Build.MODEL
                        val data = it.getString(dataColumn)
                        val relativePath =
                            it.getString(relativePathColumn)
                        val id = it.getLong(idColumn)
                        val dateModified = it.getLong(dateModifiedColumn)
                        val mimeType = it.getString(mimeTypeColumn)
                    
                        // 内容URI为图片
                        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id).toString()
                        val file = File(data)
                        val pathToThumbnail = if (file.exists()) data else ""

                        val album = Album(
                            id = bucketId,
                            label = bucketName,
                            uri = uri,
                            pathToThumbnail = pathToThumbnail,
                            relativePath = relativePath,
                            timestamp = dateModified,
                            count = 1
                        )
                    
                        val currentAlbum = albumList.find { a -> a.id == bucketId }
                        if (currentAlbum == null) {
                            albumList.add(album)
                        } else {
                            val index = albumList.indexOf(currentAlbum)
                            albumList[index] = albumList[index].let { a -> 
                                a.copy(count = a.count + 1).let { updatedAlbum ->
                                    // 如果当前图片的时间戳更新，则更新相册的缩略图
                                    if (updatedAlbum.timestamp <= dateModified) {
                                        album.copy(count = updatedAlbum.count)
                                    } else {
                                        updatedAlbum
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        
            // 按照相册名称排序
            albumList.sortBy { it.label }
            albumList
        }
        
        // 缓存结果，使用长期缓存策略
        cacheManager.put("albums", albums as Serializable, CacheManager.CacheStrategy.LONG_LIVED)
        emit(albums)
    }.flowOn(Dispatchers.IO).conflate() // 添加conflate避免处理过时数据

    override fun getMedia(albumId: Long, allowedMedia: AllowedMedia): Flow<List<Media>> = flow {
        // 为每个albumId创建唯一的缓存键
        val cacheKey = "media_$albumId"
        
        // 尝试从缓存获取
        val cachedMedia: List<Media>? = cacheManager.get(cacheKey)
        if (cachedMedia != null) {
            emit(cachedMedia)
            return@flow
        }
        
        val mediaList: List<Media> = withContext(Dispatchers.IO) {
            val mediaItems = mutableListOf<Media>()
        
            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_ID
            )

            val selection = if (albumId != -1L) {
                "${MediaStore.Images.Media.BUCKET_ID} = ?"
            } else {
                null
            }

            val selectionArgs = if (albumId != -1L) {
                arrayOf(albumId.toString())
            } else {
                null
            }

            val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
            
            // 使用Bundle优化查询 (Android 10+)
            val bundle = if (selection == null) {
                Bundle().apply {
                    putStringArray(
                        ContentResolver.QUERY_ARG_SORT_COLUMNS,
                        arrayOf(MediaStore.Images.Media.DATE_MODIFIED)
                    )
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }
            } else {
                null
            }

            val cursor = if (bundle != null) {
                context.contentResolver.query(
                    collection,
                    projection,
                    bundle,
                    null
                )
            } else {
                context.contentResolver.query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )
            }

            cursor?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val size = cursor.getLong(sizeColumn)
                    val mimeType = cursor.getString(mimeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val dateModified = cursor.getLong(dateModifiedColumn)
                    val data = cursor.getString(dataColumn)
                    val bucketId = cursor.getLong(bucketIdColumn)
                
                    val uri =
                        MediaStore.Images.Media
                            .getContentUri(MediaStore.VOLUME_EXTERNAL)
                            .buildUpon()
                            .appendPath(id.toString())
                            .build()
                            .toString()

                    mediaItems.add(
                        Media(
                            id = id,
                            displayName = name,
                            size = size,
                            mimeType = mimeType,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            uri = uri,
                            bucketId = bucketId
                        )
                    )
                }
            }
            mediaItems
        }
        
        // 缓存结果，所有图片使用长期缓存，特定相册使用中等缓存
        val cacheStrategy = if (albumId == -1L) 
            CacheManager.CacheStrategy.LONG_LIVED 
        else 
            CacheManager.CacheStrategy.LONG_LIVED
            
        cacheManager.put(cacheKey, mediaList as Serializable, cacheStrategy)
        emit(mediaList)
    }.flowOn(Dispatchers.IO).conflate() // 添加conflate避免处理过时数据
    
    fun clearCache() {
        cacheManager.clear()
    }
}
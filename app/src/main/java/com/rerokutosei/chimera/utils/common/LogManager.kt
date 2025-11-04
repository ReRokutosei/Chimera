/*
 * Chimera is an image stitching tool
 * Copyright (c) 2025 ReRokutosei
 *
 * Licensed under the GNU General Public License v3.0 (the "License");
 * you may redistribute and/or modify this program under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.en.html>.
 */

package com.rerokutosei.chimera.utils.common

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 日志管理器，负责应用内的日志记录和文件写入
 */
class LogManager private constructor(context: Context) {

    private val applicationContext = context.applicationContext
    
    companion object {
        private const val TAG = "LogManager"
        private const val LOG_FILE_NAME = "chimera_log.txt"
        private const val MAX_LOG_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        
        // 日志等级定义
        const val LOG_LEVEL_DEBUG = 0
        const val LOG_LEVEL_INFO = 1
        const val LOG_LEVEL_WARN = 2
        const val LOG_LEVEL_ERROR = 3
        
        @Volatile
        private var INSTANCE: LogManager? = null

        fun getInstance(context: Context): LogManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LogManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    @Volatile
    private var currentLogLevel = LOG_LEVEL_INFO
    private val logDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val logMutex = Mutex()
    
    /**
     * 设置日志等级
     */
    fun setLogLevel(level: Int) {
        currentLogLevel = level
    }
    

    
    /**
     * 获取日志文件路径
     */
    private fun getLogFile(): File {
        return try {
            // 保存到应用专属目录
            val logDir = File(applicationContext.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            File(logDir, LOG_FILE_NAME)
        } catch (e: Exception) {
            // 如果外部存储不可用，使用内部存储
            this.warn(TAG, "外部存储不可用，使用内部存储：$e")
            File(applicationContext.filesDir, LOG_FILE_NAME)
        }
    }
    
    /**
     * 记录调试日志
     */
    fun debug(tag: String, message: String) {
        if (currentLogLevel <= LOG_LEVEL_DEBUG) {
            Log.d(tag, message)
            writeLogToFile("DEBUG", tag, message)
        }
    }
    
    /**
     * 记录信息日志
     */
    fun info(tag: String, message: String) {
        if (currentLogLevel <= LOG_LEVEL_INFO) {
            Log.i(tag, message)
            writeLogToFile("INFO", tag, message)
        }
    }
    
    /**
     * 记录警告日志
     */
    fun warn(tag: String, message: String) {
        if (currentLogLevel <= LOG_LEVEL_WARN) {
            Log.w(tag, message)
            writeLogToFile("WARN", tag, message)
        }
    }
    
    /**
     * 记录错误日志
     */
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        if (currentLogLevel <= LOG_LEVEL_ERROR) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
            writeLogToFile("ERROR", tag, message + if (throwable != null) " | ${throwable.message}" else "")
        }
    }
    
    /**
     * 将日志写入文件
     */
    private fun writeLogToFile(level: String, tag: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            writeLogEntry(level, tag, message)
        }
    }
    
    /**
     * 同步写入日志到文件
     */
    private fun writeLogToFileSync(level: String, tag: String, message: String) {
        try {
            runBlocking {
                writeLogEntry(level, tag, message)
            }
        } catch (e: Exception) {
            if (tag != TAG) {
                this.error(TAG, "同步写入日志文件失败", e)
            }
        }
    }
    
    /**
     * 实际的日志写入操作
     */
    private suspend fun writeLogEntry(level: String, tag: String, message: String) {
        try {
            logMutex.withLock {
                val logFile = getLogFile()

                if (logFile.exists() && logFile.length() > MAX_LOG_FILE_SIZE) {
                    logFile.delete()
                }
                
                val timestamp = logDateFormat.format(Date())
                val logEntry = "[$timestamp] $level/$tag: $message\n"

                val parentDir = logFile.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs()
                }

                synchronized(this) {
                    FileOutputStream(logFile, true).use { fos ->
                        fos.write(logEntry.toByteArray())
                    }
                }

                if (tag != TAG) {
                    this.debug(TAG, "日志写入成功: ${logFile.absolutePath}")
                }
            }
        } catch (e: Exception) {
            if (tag != TAG) {
                this.error(TAG, "写入日志文件失败", e)
            }
        }
    }
    

    fun critical(tag: String, message: String) {
        this.error(tag, message)
        writeLogToFileSync("CRITICAL", tag, message)
    }
}
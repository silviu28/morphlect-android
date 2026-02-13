package com.sil.morphlect.repository

import android.content.Context
import android.util.Log
import com.sil.morphlect.dto.ModelInfoDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModelsRepository(private val context: Context) {
    val path = context.filesDir.toString() + "/models"
    private val defaults: List<String> = listOf()
    private val dir = File(path)

    suspend fun readContents(): List<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val contents: List<String> = dir.listFiles()
                ?.map { it.name }
                ?: defaults

            contents
        } catch (e: Exception) {
            defaults
        }
    }

    suspend fun delete(name: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            for (file in dir.listFiles()) {
                if (file.name.equals(name)) {
                    file.delete()
                    true
                }
            }
            false
        } catch (e: Exception) {
            Log.e("Delete model", e.toString())
            false
        }
    }
}
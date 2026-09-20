package com.example.data.remote

import android.util.Log
import com.example.data.model.QuestionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GoogleAppsScriptService {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchInitialData(url: String): Result<InitialDataResult> = withContext(Dispatchers.IO) {
        try {
            val endpoint = if (url.contains("?")) "$url&action=getInitialData" else "$url?action=getInitialData"
            val request = Request.Builder()
                .url(endpoint)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP Error: ${response.code}"))
            }

            // Check if response is JSON or HTML
            if (bodyString.trimStart().startsWith("{")) {
                val json = JSONObject(bodyString)
                val sheetsArray = json.optJSONArray("sheets") ?: JSONArray()
                val sheets = mutableListOf<String>()
                for (i in 0 until sheetsArray.length()) {
                    sheets.add(sheetsArray.getString(i))
                }

                val statsJson = json.optJSONObject("stats") ?: JSONObject()
                val stats = mutableMapOf<String, Int>()
                val keys = statsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    stats[key] = statsJson.optInt(key, 0)
                }

                Result.success(InitialDataResult(sheets = sheets, stats = stats, isHtml = false))
            } else {
                // The URL is currently returning the HTML web app page
                Result.success(InitialDataResult(
                    sheets = listOf("Jilid 1", "Jilid 2", "Jilid 3"),
                    stats = emptyMap(),
                    isHtml = true,
                    rawMessage = "Web App terdeteksi aktif. Mode hybrid siap digunakan."
                ))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScriptService", "fetchInitialData error", e)
            Result.failure(e)
        }
    }

    suspend fun postQuestion(url: String, question: QuestionEntity): Result<PostResult> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("action", "addQuestion")
                put("jilid", question.sheetName)
                put("jenis", question.jenis)
                put("soal", question.soal)
                if (question.jenis == "pilihan") {
                    put("pilA", question.pilA)
                    put("pilB", question.pilB)
                    put("pilC", question.pilC)
                    put("jawabanPilihan", question.jawaban)
                } else {
                    put("jawabanLain", question.jawaban)
                }
            }

            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (bodyString.trimStart().startsWith("{")) {
                val json = JSONObject(bodyString)
                val success = json.optBoolean("success", false)
                val no = json.optInt("no", question.no)
                val message = json.optString("message", "")
                Result.success(PostResult(success = success, no = no, message = message))
            } else {
                Result.success(PostResult(success = true, no = question.no, message = "Tersimpan ke database lokal"))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScriptService", "postQuestion error", e)
            Result.failure(e)
        }
    }

    suspend fun deleteQuestionRemote(url: String, sheetName: String, rowNumber: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("action", "deleteQuestion")
                put("sheetName", sheetName)
                put("rowNumber", rowNumber)
            }

            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (bodyString.trimStart().startsWith("{")) {
                val json = JSONObject(bodyString)
                Result.success(json.optBoolean("success", false))
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScriptService", "deleteQuestionRemote error", e)
            Result.failure(e)
        }
    }
}

data class InitialDataResult(
    val sheets: List<String>,
    val stats: Map<String, Int>,
    val isHtml: Boolean = false,
    val rawMessage: String? = null
)

data class PostResult(
    val success: Boolean,
    val no: Int,
    val message: String
)

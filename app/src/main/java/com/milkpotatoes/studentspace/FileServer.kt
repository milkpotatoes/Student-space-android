package com.milkpotatoes.studentspace

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.webkit.WebResourceResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.*
import java.security.MessageDigest


class FileServer(context: Context) {
    private val context = context
    private val assetsWebRoot = "web"
    private val client: OkHttpClient = OkHttpClient()

    private fun mimeType(type: String, name: String, needCharSet: Boolean): String {
        val charSet = "charset=utf-8"
        val expandedName = when (name) {
            "js" -> "javascript"
            else -> name
        }
        return "$type/$expandedName${if (needCharSet) ";$charSet" else ""}"
    }

    private fun mimeType(name: String): String {
        val charSet = "charset=utf-8"
        val textType = listOf("html", "css", "md")
        val applicationType = listOf("js", "json", "webmanifest", "mjs")
        val imageType = listOf("png", "webp", "jpg", "jpeg", "ico", "icon", "gif", "svg")
        val fontType = listOf("woff2")
        var uriTail = Regex("""\.([^\.]+?)$""").find(name)?.value
        if (uriTail != null) {
            uriTail = uriTail.substring(1)
        }
        val expandedName = when (uriTail) {
            "js" -> "javascript"
            "mjs" -> "javascript"
            "svg" -> "svg+xml"
            else -> uriTail
        }
        return if (textType.contains(uriTail)) "text/$expandedName"
        else if (applicationType.contains(uriTail)) "application/$expandedName; $charSet"
        else if (imageType.contains(uriTail)) "image/$expandedName"
        else if (fontType.contains(uriTail)) "font/$expandedName"
        else "*/*"
    }

    private fun response404(url: String, type: String): WebResourceResponse {

        return when (type) {
            "html" -> {
                WebResourceResponse(
                    mimeType("text", "html", false),
                    Charsets.UTF_8.toString(),
                    404,
                    "Not Found",
                    mapOf("Content-Type" to "text/html"),
                    ByteArrayInputStream("<!DOCTYPE html><html><body>404 Not Found. url: $url</body></html>\n".toByteArray(
                        Charsets.UTF_8))
                )
            }
            "json" -> {
                WebResourceResponse(
                    mimeType("text", "html", false),
                    Charsets.UTF_8.toString(),
                    404,
                    "Not Found",
                    mapOf("Content-Type" to "text/html"),
                    ByteArrayInputStream(
                        "{\"status\": 404, \"message\": \"404 Not Found\", \"path\": \"$url\"}".toByteArray(
                            Charsets.UTF_8))
                )

            }
            else -> {
                WebResourceResponse(
                    mimeType("text", "html", false),
                    Charsets.UTF_8.toString(),
                    404,
                    "Not Found",
                    mapOf("Content-Type" to "text/html"),
                    ByteArrayInputStream(
                        "<!DOCTYPE html><html><body>404 Not Found. url: $url</body></html>\n".toByteArray(
                            Charsets.UTF_8))
                )
            }
        }
    }

    private fun response404(path: String): WebResourceResponse {
        return response404(path, "html")
    }


    fun answerCardCache(url: String): WebResourceResponse {
        val name = md5sum(url)
        val img = File(context.externalCacheDir, name)
        Log.d(TAG, img.path)
        val mineType = mimeType("answer-card.jpg")
        Log.d(TAG, url)
        var bit: Bitmap? = null
        if (img.isFile) bit = BitmapFactory.decodeFile(img.path)

        return WebResourceResponse(
            mineType,
            "UTF-8",
            200,
            "OK",
            mapOf("Content-Type" to mineType),
            if (bit != null) {
                Log.d(TAG, "cache file found")
                img.inputStream()
            } else {
                Log.d(TAG, "cache file not found")
                val request: Request = Request.Builder()
                    .get()
                    .url(url)
                    .build()
                val response: okhttp3.Response = client.newCall(request).execute()
                img.writeBytes(response.body.bytes())
                img.inputStream()
            }
        )
    }

    private fun md5sum(content: String): String {
        val hash = MessageDigest.getInstance("MD5").digest(content.toByteArray())
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            var str = Integer.toHexString(b.toInt())
            if (b < 0x10) {
                str = "0$str"
            }
            hex.append(str.substring(str.length - 2))
        }
        return hex.toString()
    }

    fun assetsWeb(path: String): WebResourceResponse {
        try {
            val file =
                context.assets.open(assetsWebRoot + path)
            val mineType = mimeType(path)
//            Log.d(TAG, )

            return WebResourceResponse(
                mimeType(path),
                Charsets.UTF_8.toString(),
                200,
                "OK",
                mapOf("Content-Type" to mineType),
                file
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return response404(path)
    }
}
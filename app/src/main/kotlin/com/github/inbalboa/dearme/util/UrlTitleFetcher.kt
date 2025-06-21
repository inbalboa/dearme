package com.github.inbalboa.dearme.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

object UrlTitleFetcher {

    private const val TIMEOUT_MS = 5000L

    /**
     * Checks if the given text is a valid URL
     */
    fun isUrl(text: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(text.trim()).matches()
    }

    /**
     * Fetches the title of a webpage from the given URL
     * @param url The URL to fetch the title from
     * @return The page title or null if unable to fetch
     */
    suspend fun fetchTitle(url: String): String? = withContext(Dispatchers.IO) {
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val normalizedUrl = normalizeUrl(url)
                val connection = URL(normalizedUrl).openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "GET"
                    connectTimeout = 3000
                    readTimeout = 3000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:40.0) Gecko/40.0 Firefox/40.0")
                    setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    setRequestProperty("Accept-Language", "en-US,en;q=0.5")
                    setRequestProperty("Accept-Encoding", "gzip, deflate, identity")
                    setRequestProperty("Connection", "close")
                    setRequestProperty("Cache-Control", "no-cache")
                }

                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    return@withTimeoutOrNull null
                }

                val contentType = connection.contentType?.lowercase()
                if (contentType?.contains("text/html") != true) {
                    return@withTimeoutOrNull null
                }

                val inputStream = getDecompressedInputStream(connection)
                val content = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                extractTitle(content)
            } catch (e: IOException) {
                null
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Gets the appropriate input stream based on content encoding
     */
    private fun getDecompressedInputStream(connection: HttpURLConnection): InputStream {
        val encoding = connection.contentEncoding?.lowercase()
        val inputStream = connection.inputStream

        return when (encoding) {
            "gzip" -> {
                try {
                    GZIPInputStream(inputStream)
                } catch (e: IOException) {
                    inputStream
                }
            }
            "deflate" -> {
                try {
                    InflaterInputStream(inputStream)
                } catch (e: IOException) {
                    inputStream
                }
            }
            else -> inputStream
        }
    }

    /**
     * Normalizes the URL by adding protocol if missing
     */
    private fun normalizeUrl(url: String): String {
        val trimmed = url.trim()
        return if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else {
            trimmed
        }
    }

    /**
     * Extracts the title from HTML content
     */
    private fun extractTitle(html: String): String? {
        val titlePattern = Pattern.compile("<title[^>]*>([^<]*)</title>", Pattern.CASE_INSENSITIVE)
        val matcher = titlePattern.matcher(html)

        return if (matcher.find()) {
            val title = matcher.group(1)?.trim()
            if (title.isNullOrBlank()) {
                null
            } else {
                cleanTitle(title)
            }
        } else {
            null
        }
    }

    /**
     * Cleans the title by removing extra whitespace and decoding basic HTML entities
     */
    private fun cleanTitle(title: String): String {
        return title
            .replace("\\s+".toRegex(), " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
            .trim()
            .take(100)
    }
}

package com.github.inbalboa.dearme.util

import android.util.Patterns
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

data class PageMetadata(
    val title: String?,
    val description: String?
)

object UrlTitleFetcher {

    private const val TIMEOUT_MS = 5000L
    private const val MAX_READ_BYTES = 64 * 1024

    private val titlePattern = Pattern.compile("<title[^>]*>([^<]*)</title>", Pattern.CASE_INSENSITIVE)

    private val metaPropertyPatterns = mutableMapOf<String, Pattern>()
    private val metaNamePatterns = mutableMapOf<String, Pattern>()

    private fun getMetaPropertyPattern(property: String): Pattern =
        metaPropertyPatterns.getOrPut(property) {
            Pattern.compile(
                """<meta\s[^>]*property\s*=\s*["']$property["'][^>]*content\s*=\s*["']([^"']*)["'][^>]*/?>""" +
                    "|" +
                    """<meta\s[^>]*content\s*=\s*["']([^"']*)["'][^>]*property\s*=\s*["']$property["'][^>]*/?>""",
                Pattern.CASE_INSENSITIVE
            )
        }

    private fun getMetaNamePattern(name: String): Pattern =
        metaNamePatterns.getOrPut(name) {
            Pattern.compile(
                """<meta\s[^>]*name\s*=\s*["']$name["'][^>]*content\s*=\s*["']([^"']*)["'][^>]*/?>""" +
                    "|" +
                    """<meta\s[^>]*content\s*=\s*["']([^"']*)["'][^>]*name\s*=\s*["']$name["'][^>]*/?>""",
                Pattern.CASE_INSENSITIVE
            )
        }

    /**
     * Checks if the given text is a valid URL
     */
    fun isUrl(text: String): Boolean {
        return Patterns.WEB_URL.matcher(text.trim()).matches()
    }

    /**
     * Fetches the title of a webpage from the given URL
     * @param url The URL to fetch the title from
     * @return The page title or null if unable to fetch
     */
    suspend fun fetchTitle(url: String): String? = fetchMetadata(url).title

    /**
     * Fetches Open Graph metadata (title, description) from the given URL
     * @param url The URL to fetch metadata from
     * @return PageMetadata with title and description (either may be null)
     */
    suspend fun fetchMetadata(url: String): PageMetadata = withContext(Dispatchers.IO) {
        withTimeoutOrNull(TIMEOUT_MS) {
            val connection = try {
                val normalizedUrl = normalizeUrl(url)
                URL(normalizedUrl).openConnection() as HttpURLConnection
            } catch (_: Exception) {
                return@withTimeoutOrNull PageMetadata(null, null)
            }

            try {
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
                    return@withTimeoutOrNull PageMetadata(null, null)
                }

                val contentType = connection.contentType?.lowercase()
                if (contentType?.contains("text/html") != true) {
                    return@withTimeoutOrNull PageMetadata(null, null)
                }

                val inputStream = getDecompressedInputStream(connection)
                val content = inputStream.bufferedReader(Charsets.UTF_8).use {
                    val buffer = CharArray(MAX_READ_BYTES)
                    val charsRead = it.read(buffer)
                    if (charsRead > 0) String(buffer, 0, charsRead) else ""
                }
                extractMetadata(content)
            } catch (_: IOException) {
                PageMetadata(null, null)
            } catch (_: Exception) {
                PageMetadata(null, null)
            } finally {
                connection.disconnect()
            }
        } ?: PageMetadata(null, null)
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
                } catch (_: IOException) {
                    inputStream
                }
            }
            "deflate" -> {
                try {
                    InflaterInputStream(inputStream)
                } catch (_: IOException) {
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
     * Extracts title and description from HTML content, preferring OG meta tags
     */
    private fun extractMetadata(html: String): PageMetadata {
        val ogTitle = extractMetaProperty(html, "og:title")
        val title = ogTitle ?: extractHtmlTitle(html)
        val description = extractMetaProperty(html, "og:description")
            ?: extractMetaName(html)
        return PageMetadata(title, description)
    }

    /**
     * Extracts the <title> tag content from HTML
     */
    private fun extractHtmlTitle(html: String): String? {
        val matcher = titlePattern.matcher(html)

        return if (matcher.find()) {
            val title = matcher.group(1)?.trim()
            if (title.isNullOrBlank()) null else cleanTitle(title)
        } else {
            null
        }
    }

    /**
     * Extracts content from an Open Graph meta tag
     */
    private fun extractMetaProperty(html: String, property: String): String? {
        val matcher = getMetaPropertyPattern(property).matcher(html)
        if (matcher.find()) {
            val content = (matcher.group(1) ?: matcher.group(2))?.trim()
            if (!content.isNullOrBlank()) {
                return cleanTitle(content)
            }
        }
        return null
    }

    /**
     * Extracts content from a meta tag by name attribute
     */
    private fun extractMetaName(html: String, name: String = "description"): String? {
        val matcher = getMetaNamePattern(name).matcher(html)
        if (matcher.find()) {
            val content = (matcher.group(1) ?: matcher.group(2))?.trim()
            if (!content.isNullOrBlank()) {
                return cleanTitle(content)
            }
        }
        return null
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

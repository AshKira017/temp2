package com.mylibrary.app.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder

data class BookSearchResult(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String
)

data class CoverResult(
    val url: String,
    val source: String
)

private data class OLSearchResponse(val docs: List<OLDoc>?)
private data class OLDoc(
    val key: String?,
    val title: String?,
    val author_name: List<String>?,
    val cover_i: Long?
)

private data class GBResponse(val items: List<GBItem>?)
private data class GBItem(val volumeInfo: GBVolumeInfo?)
private data class GBVolumeInfo(val imageLinks: GBImageLinks?)
private data class GBImageLinks(
    val extraLarge: String?,
    val large: String?,
    val medium: String?,
    val thumbnail: String?
)

object ApiService {

    private val gson = Gson()

    private fun get(url: String): String {
        val conn = URL(url).openConnection()
        conn.setRequestProperty("User-Agent", "MyLibraryApp/1.0")
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        return conn.getInputStream().bufferedReader().readText()
    }

    suspend fun searchBooks(query: String): List<BookSearchResult> = withContext(Dispatchers.IO) {
        try {
            val q = URLEncoder.encode(query, "UTF-8")
            val json = get("https://openlibrary.org/search.json?q=$q&fields=title,author_name,cover_i,key&limit=15")
            val resp = gson.fromJson(json, OLSearchResponse::class.java)
            resp.docs
                ?.filter { it.cover_i != null && !it.key.isNullOrBlank() }
                ?.take(9)
                ?.map { doc ->
                    BookSearchResult(
                        id = doc.key!!,
                        title = doc.title ?: "Unknown Title",
                        author = doc.author_name?.firstOrNull() ?: "Unknown Author",
                        coverUrl = "https://covers.openlibrary.org/b/id/${doc.cover_i}-L.jpg"
                    )
                } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCovers(title: String, author: String): List<CoverResult> = withContext(Dispatchers.IO) {
        val olCovers = mutableListOf<CoverResult>()
        val gbCovers = mutableListOf<CoverResult>()

        val olJob = async {
            try {
                val q = URLEncoder.encode("$title $author", "UTF-8")
                val json = get("https://openlibrary.org/search.json?q=$q&fields=cover_i&limit=20")
                val resp = gson.fromJson(json, OLSearchResponse::class.java)
                val seen = mutableSetOf<Long>()
                resp.docs?.forEach { doc ->
                    val cid = doc.cover_i ?: return@forEach
                    if (seen.add(cid) && olCovers.size < 6) {
                        olCovers.add(CoverResult("https://covers.openlibrary.org/b/id/$cid-L.jpg", "Open Library"))
                    }
                }
            } catch (_: Exception) {}
        }

        val gbJob = async {
            try {
                val t = URLEncoder.encode(title, "UTF-8")
                val a = URLEncoder.encode(author, "UTF-8")
                val json = get("https://www.googleapis.com/books/v1/volumes?q=intitle:$t+inauthor:$a&maxResults=10")
                val resp = gson.fromJson(json, GBResponse::class.java)
                resp.items?.forEach { item ->
                    val links = item.volumeInfo?.imageLinks ?: return@forEach
                    val raw = links.extraLarge ?: links.large ?: links.medium ?: links.thumbnail ?: return@forEach
                    val url = raw.replace("http://", "https://")
                        .replace("&edge=curl", "")
                        .replace("zoom=1", "zoom=3")
                    if (gbCovers.size < 6) gbCovers.add(CoverResult(url, "Google Books"))
                }
            } catch (_: Exception) {}
        }

        awaitAll(olJob, gbJob)

        // Smart merge: up to 3 from each, fill gaps from the other
        val result = mutableListOf<CoverResult>()
        result.addAll(olCovers.take(3))
        result.addAll(gbCovers.take(3))
        var oi = 3; var gi = 3
        while (result.size < 6) {
            val fromOl = olCovers.getOrNull(oi++)
            val fromGb = gbCovers.getOrNull(gi++)
            if (fromOl != null) result.add(fromOl)
            if (fromGb != null && result.size < 6) result.add(fromGb)
            if (fromOl == null && fromGb == null) break
        }
        result
    }
}

package io.github.teccheck.fastlyrics.api.provider

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.Tokens
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.exceptions.ParseException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

object Genius : LyricsProvider {

    private const val TAG = "GeniusProvider"

    // Json keys for deserialization
    private const val KEY_RESPONSE = "response"
    private const val KEY_RESULT = "result"
    private const val KEY_HITS = "hits"
    private const val KEY_TITLE = "title"
    private const val KEY_PRIMARY_ARTIST = "primary_artist"
    private const val KEY_NAME = "name"
    private const val KEY_URL = "url"
    private const val KEY_ID = "id"
    private const val KEY_SONG = "song"
    private const val KEY_ALBUM = "album"
    private const val KEY_SONG_ART_URL = "header_image_thumbnail_url"
    private const val KEY_LYRICS = "lyrics"
    private const val KEY_DOM = "dom"
    private const val KEY_CHILDREN = "children"
    private const val KEY_TAG = "tag"

    // Dom tags for lyrics parsing
    private const val TAG_BR = "br"

    private val apiService: ApiService

    init {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${Tokens.GENIUS_API}")
                .build()
            chain.proceed(newRequest)
        }.build()

        val gson = GsonBuilder().disableJdkUnsafe().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "genius"

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        Log.i(TAG, "Searching for \"$searchQuery\"")

        val jsonBody: JsonElement?

        try {
            jsonBody = apiService.search(searchQuery)?.execute()?.body()
            val jsonResponse = jsonBody?.asJsonObject?.getAsJsonObject(KEY_RESPONSE)
            val jsonHits =
                jsonResponse?.getAsJsonArray(KEY_HITS) ?: return Failure(ParseException())

            val results = mutableListOf<SearchResult>()
            for (jsonHit in jsonHits) {
                val jo = jsonHit.asJsonObject.get(KEY_RESULT).asJsonObject

                val title = jo.get(KEY_TITLE).asString
                val artist = jo.get(KEY_PRIMARY_ARTIST).asJsonObject.get(KEY_NAME).asString
                val album = getAlbum(jo)
                val artUrl = jo.get(KEY_SONG_ART_URL).asString
                val url = jo.get(KEY_URL).asString
                val id = jo.get(KEY_ID).asInt

                val result = SearchResult(title, artist, album, artUrl, url, id, this)
                results.add(result)
            }

            return Success(results)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }
    }

    override fun fetchLyrics(songId: Int): Result<SongWithLyrics, LyricsApiException> {
        Log.i(TAG, "Fetching song $songId")
        val jsonBody: JsonElement?

        try {
            jsonBody = apiService.fetchSongInfo(songId)?.execute()?.body()

            val jsonResponse = jsonBody?.asJsonObject?.getAsJsonObject(KEY_RESPONSE)
            val jsonSong =
                jsonResponse?.getAsJsonObject(KEY_SONG) ?: return Failure(ParseException())

            val title = jsonSong.get(KEY_TITLE).asString
            val artist = jsonSong.get(KEY_PRIMARY_ARTIST).asJsonObject.get(KEY_NAME).asString
            val sourceUrl = jsonSong.get(KEY_URL).asString
            val album = getAlbum(jsonSong)
            val artUrl = jsonSong.get(KEY_SONG_ART_URL).asString

            val lyrics =
                parseLyricsJsonTag(jsonSong.get(KEY_LYRICS).asJsonObject.get(KEY_DOM).asJsonObject)

            return Success(
                SongWithLyrics(
                    0,
                    title,
                    artist,
                    lyrics,
                    null,
                    sourceUrl,
                    album,
                    artUrl,
                    LyricsType.RAW_TEXT,
                    getName()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }
    }

    private fun getAlbum(jsonObject: JsonObject): String? {
        val albumJson = jsonObject.get(KEY_ALBUM) ?: return null
        if (albumJson.isJsonNull) return null
        return albumJson.asJsonObject.get(KEY_NAME).asString
    }

    private fun parseLyricsJsonTag(lyricsJsonTag: JsonElement): String {
        if (lyricsJsonTag.isJsonPrimitive) {
            return lyricsJsonTag.asString
        }
        val jsonObject = lyricsJsonTag.asJsonObject
        if (jsonObject.has(KEY_TAG) && jsonObject.get(KEY_TAG).asString.equals(TAG_BR)) {
            return "\n"
        }
        if (jsonObject.has(KEY_CHILDREN)) {
            var text = ""
            val jsonChildren = jsonObject.get(KEY_CHILDREN).asJsonArray
            for (jsonChild in jsonChildren) {
                text += parseLyricsJsonTag(jsonChild)
            }
            return text
        }
        return ""
    }

    interface ApiService {
        @GET("search")
        fun search(@Query("q") query: String): Call<JsonElement>?

        @GET("songs/{songId}")
        fun fetchSongInfo(@Path("songId") songId: Int): Call<JsonElement>?
    }
}
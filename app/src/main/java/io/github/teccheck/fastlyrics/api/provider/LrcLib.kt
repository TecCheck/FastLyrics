package io.github.teccheck.fastlyrics.api.provider

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object LrcLib : LyricsProvider {

    private const val TAG = "LrclibProvider"

    private const val TRACK_NAME = "trackName"
    private const val ARTIST_NAME = "artistName"
    private const val ALBUM_NAME = "albumName"
    private const val ID = "id"
    private const val LYRICS_PLAN = "plainLyrics"
    private const val LYRICS_SYNCED = "syncedLyrics"

    private val apiService: ApiService

    init {
        val gson = GsonBuilder().disableJdkUnsafe().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://lrclib.net/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "lrclib"

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        Log.i(TAG, "Searching for \"$searchQuery\"")

        val jsonBody: JsonArray?

        try {
            jsonBody = apiService.search(searchQuery)?.execute()?.body()?.asJsonArray

            if (jsonBody == null) return Failure(LyricsNotFoundException())

            val results = mutableListOf<SearchResult>()
            for (jsonHit in jsonBody) {
                val jo = jsonHit.asJsonObject

                val title = jo.get(TRACK_NAME).asString
                val artist = jo.get(ARTIST_NAME).asString
                val album = jo.get(ALBUM_NAME).asString
                val id = jo.get(ID).asInt

                val result = SearchResult(title, artist, album, null, null, id, this)
                results.add(result)
            }

            return Success(results)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }
    }

    override fun fetchLyrics(songId: Int): Result<SongWithLyrics, LyricsApiException> {
        val jsonBody: JsonObject?

        try {
            jsonBody = apiService.fetchSongInfo(songId)?.execute()?.body()?.asJsonObject

            if (jsonBody == null) return Failure(LyricsNotFoundException())

            val title = jsonBody.get(TRACK_NAME).asString
            val artist = jsonBody.get(ARTIST_NAME).asString
            val album = jsonBody.get(ALBUM_NAME).asString
            val lyricsPlain = jsonBody.get(LYRICS_PLAN).asString
            val lyricsSynced = jsonBody.get(LYRICS_SYNCED).asString

            return Success(
                SongWithLyrics(
                    0,
                    title,
                    artist,
                    lyricsPlain,
                    lyricsSynced,
                    "https://lrclib.net/",
                    album,
                    null,
                    LyricsType.LRC,
                    getName()
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }
    }

    interface ApiService {
        @GET("search")
        fun search(@Query("q") query: String): Call<JsonElement>?

        @GET("get/{songId}")
        fun fetchSongInfo(@Path("songId") songId: Int): Call<JsonElement>?
    }
}
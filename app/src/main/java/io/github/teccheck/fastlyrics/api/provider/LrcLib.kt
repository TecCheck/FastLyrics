package io.github.teccheck.fastlyrics.api.provider

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.BuildConfig
import io.github.teccheck.fastlyrics.exceptions.GenericException
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.exceptions.ParseException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException

object LrcLib : LyricsProvider {
    private const val TAG = "LrclibProvider"

    private const val TRACK_NAME = "trackName"
    private const val ARTIST_NAME = "artistName"
    private const val ALBUM_NAME = "albumName"
    private const val DURATION = "duration"
    private const val ID = "id"
    private const val LYRICS_PLAN = "plainLyrics"
    private const val LYRICS_SYNCED = "syncedLyrics"

    private val apiService: ApiService

    init {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val newRequest: Request = chain.request().newBuilder().addHeader(
                "User-Agent",
                "FastLyrics v${BuildConfig.VERSION_NAME} (https://github.com/TecCheck/FastLyrics)"
            ).build()
            chain.proceed(newRequest)
        }.build()

        val gson = GsonBuilder().disableJdkUnsafe().create()

        val retrofit = Retrofit.Builder().baseUrl("https://lrclib.net/api/").client(client)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "lrclib"

    override fun search(songMeta: SongMeta): Result<List<SearchResult>, LyricsApiException> {
        Log.d(TAG, "search($songMeta)")

        try {
            if (songMeta.artist != null) {
                val call = apiService.get(
                    songMeta.title, songMeta.artist, songMeta.album, songMeta.duration?.div(1000)
                )

                call?.let { it.execute().body()?.asJsonObject }?.let {
                    val song = parseSongWithLyrics(it)
                    return Success(
                        listOf(
                            SearchResult(
                                song.title,
                                song.artist,
                                song.album,
                                song.artUrl,
                                song.sourceUrl,
                                it.get(ID).asLong,
                                this,
                                song
                            )
                        )
                    )
                }
            }

            val jsonBody = apiService.search(
                null, songMeta.title, songMeta.artist, songMeta.album
            )?.execute()?.body()?.asJsonArray

            return Success(parseSearchResults(jsonBody))

        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        } catch (e: JSONException) {
            Log.e(TAG, e.message, e)
            return Failure(ParseException())
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(GenericException())
        }
    }

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        Log.d(TAG, "search($searchQuery)")

        try {
            val jsonBody = apiService.search(searchQuery)?.execute()?.body()?.asJsonArray
            return Success(parseSearchResults(jsonBody))
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        } catch (e: JSONException) {
            Log.e(TAG, e.message, e)
            return Failure(ParseException())
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(GenericException())
        }
    }

    override fun fetchLyrics(songId: Long): Result<SongWithLyrics, LyricsApiException> {
        Log.d(TAG, "fetchLyrics($songId)")

        try {
            val json = apiService.get(songId)?.execute()?.body()?.asJsonObject ?: return Failure(
                ParseException()
            )

            val song = parseSongWithLyrics(json)
            return Success(song)
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        } catch (e: JSONException) {
            Log.e(TAG, e.message, e)
            return Failure(ParseException())
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(GenericException())
        }
    }

    private fun parseSearchResults(json: JsonArray?): List<SearchResult> {
        if (json == null) return emptyList()

        return json.mapNotNull {
            val jo = it.asJsonObject
            val song = parseSongWithLyrics(jo)
            SearchResult(
                song.title,
                song.artist,
                song.album,
                song.artUrl,
                song.sourceUrl,
                jo.get(ID).asLong,
                this,
                song
            )
        }
    }

    private fun parseSongWithLyrics(json: JsonObject): SongWithLyrics {
        return SongWithLyrics(
            0,
            json.get(TRACK_NAME).asString,
            json.get(ARTIST_NAME).asString,
            json.get(LYRICS_PLAN)?.asString,
            json.get(LYRICS_SYNCED)?.asString,
            "https://lrclib.net/",
            json.get(ALBUM_NAME).asString,
            null,
            json.get(DURATION).asLong * 1000,
            LyricsType.LRC,
            getName()
        )
    }

    interface ApiService {
        @GET("search")
        fun search(
            @Query("q") query: String?,
            @Query("track_name") trackName: String? = null,
            @Query("artist_name") artistName: String? = null,
            @Query("album_name") albumName: String? = null,
        ): Call<JsonElement>?

        @GET("get")
        fun get(
            @Query("track_name") trackName: String,
            @Query("artist_name") artistName: String,
            @Query("album_name") albumName: String? = null,
            @Query("duration") duration: Long? = null,
        ): Call<JsonElement>?

        @GET("get/{songId}")
        fun get(@Path("songId") songId: Long): Call<JsonElement>?
    }
}
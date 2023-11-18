package io.github.teccheck.fastlyrics.api.provider

import android.util.Log
import com.google.gson.GsonBuilder
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
import retrofit2.http.Query

object Netease : LyricsProvider {
    
    // TODO: Find out how to get a cover image

    private const val TAG = "Netease"

    private const val KEY_RESULT = "result"
    private const val KEY_SONGS = "songs"
    private const val KEY_ID = "id"
    private const val KEY_NAME = "name"
    private const val KEY_ALBUM = "album"
    private const val KEY_ARTISTS = "artists"
    private const val KEY_UNCOLLECTED = "uncollected"
    private const val KEY_LRC = "lrc"
    private const val KEY_LYRIC = "lyric"

    private val apiService: ApiService

    init {
        val gson = GsonBuilder().disableJdkUnsafe().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://music.163.com/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "netease"

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        try {
            val jsonBody = apiService.search(searchQuery).execute().body()
            val result = jsonBody?.get(KEY_RESULT)?.asJsonObject
            val songs =
                result?.get(KEY_SONGS)?.asJsonArray ?: return Failure(LyricsNotFoundException())

            val results = songs
                .filter { !it.asJsonObject.has(KEY_UNCOLLECTED) }
                .map { song ->
                    val songObject = song.asJsonObject

                    val id = songObject.get(KEY_ID).asInt
                    val title = songObject.get(KEY_NAME).asString
                    val artists = songObject.get(KEY_ARTISTS).asJsonArray
                    val artist = artists.first().asJsonObject.get(KEY_NAME).asString
                    val album = songObject.get(KEY_ALBUM).asJsonObject.get(KEY_NAME).asString
                    val url = "https://music.163.com/#/song?id=$id"

                    SearchResult(title, artist, album, null, url, id, null, this)
                }

            return Success(results)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }

    }

    override fun fetchLyrics(searchResult: SearchResult): Result<SongWithLyrics, LyricsApiException> {
        val songId = (searchResult.id as Int?) ?: return Failure(LyricsNotFoundException())

        val lyricsJson = apiService.query(songId).execute().body()?.asJsonObject ?: return Failure(
            NetworkException()
        )

        val lyrics = lyricsJson.get(KEY_LRC).asJsonObject.get(KEY_LYRIC).asString

        return Success(
            SongWithLyrics(
                0,
                searchResult.title,
                searchResult.artist,
                lyrics,
                searchResult.url!!,
                searchResult.album,
                null,
                LyricsType.LRC,
                getName()
            )
        )
    }

    interface ApiService {
        @GET("search/get")
        fun search(
            @Query("s") query: String,
            @Query("limit") csRfToken: Int = 6,
            @Query("type") type: Int = 1,
            @Query("offset") offset: Int = 0,
            @Query("total") total: Boolean = true,
        ): Call<JsonObject>

        @GET("song/lyric")
        fun query(
            @Query("id") id: Int,
            @Query("lv") lv: Int = -1,
            @Query("kv") kv: Int = -1,
            @Query("tv") tv: Int = -1,
        ): Call<JsonObject>
    }
}
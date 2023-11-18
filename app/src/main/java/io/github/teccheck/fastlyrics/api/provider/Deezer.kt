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
import io.github.teccheck.fastlyrics.exceptions.ParseException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

object Deezer : LyricsProvider {
    private const val TAG = "DeezerProvider"

    private const val KEY_DATA = "data"
    private const val KEY_ID = "id"
    private const val KEY_LINK = "link"
    private const val KEY_TITLE = "title"
    private const val KEY_ARTIST = "artist"
    private const val KEY_ALBUM = "album"
    private const val KEY_COVER = "cover"
    private const val KEY_NAME = "name"
    private const val KEY_JWT = "jwt"
    private const val KEY_TRACK = "track"
    private const val KEY_LYRICS = "lyrics"
    private const val KEY_SYNCED_LINES = "synchronizedLines"

    private val apiService: ApiService
    private var authToken: String? = null

    init {
        val gson = GsonBuilder().disableJdkUnsafe().create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "deezer"

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        Log.i(TAG, "Searching for \"$searchQuery\"")

        val jsonBody = apiService.search(searchQuery)?.execute()?.body()
        val jsonData = jsonBody?.get(KEY_DATA)?.asJsonArray ?: return Failure(ParseException())

        val results = jsonData.map { song ->
            val songObject = song.asJsonObject
            val id = songObject.get(KEY_ID).asInt
            val title = songObject.get(KEY_TITLE).asString
            val artist = songObject.get(KEY_ARTIST).asJsonObject.get(KEY_NAME).asString
            val album = songObject.get(KEY_ALBUM).asJsonObject.get(KEY_TITLE).asString
            val artUrl = songObject.get(KEY_ALBUM).asJsonObject.get(KEY_COVER).asString
            val link = songObject.get(KEY_LINK).asString

            return@map SearchResult(title, artist, album, artUrl, link, id, null,this)
        }

        return Success(results)
    }

    override fun fetchLyrics(searchResult: SearchResult): Result<SongWithLyrics, LyricsApiException> {
        val songId = (searchResult.id as Int?) ?: return Failure(LyricsNotFoundException())

        val query = getLyricsQuery(songId)
        val auth = "Bearer ${getAuthToken()}"

        val lyricsJson = apiService.query(query, auth)?.execute()?.body() ?: return Failure(
            NetworkException()
        )

        val lyrics = parseSyncedLyrics(lyricsJson)
        val trackJson =
            apiService.getTrackInfo(songId)?.execute()?.body() ?: return Failure(NetworkException())

        val title = trackJson.get(KEY_TITLE).asString
        val artist = trackJson.get(KEY_ARTIST).asJsonObject.get(KEY_NAME).asString
        val album = trackJson.get(KEY_ALBUM).asJsonObject.get(KEY_TITLE).asString
        val artUrl = trackJson.get(KEY_ALBUM).asJsonObject.get(KEY_COVER).asString
        val link = trackJson.get(KEY_LINK).asString

        return Success(
            SongWithLyrics(
                0,
                title,
                artist,
                lyrics,
                link,
                album,
                artUrl,
                LyricsType.LRC,
                getName()
            )
        )
    }

    private fun getAuthToken(): String? {
        if (authToken == null)
            authToken = apiService.auth()?.execute()?.body()?.get(KEY_JWT)?.asString

        return authToken
    }

    private fun parseSyncedLyrics(json: JsonObject): String {
        val track = json.get(KEY_DATA).asJsonObject.get(KEY_TRACK).asJsonObject
        val lines = track.get(KEY_LYRICS).asJsonObject.get(KEY_SYNCED_LINES).asJsonArray

        val lyrics = lines.joinToString(separator = "\n") {
            val line = it.asJsonObject
            "${line.get("lrcTimestamp").asString}${line.get("line").asString}"
        }

        return lyrics
    }

    private fun getLyricsQuery(songId: Int): JsonObject {
        val queryJson = JsonObject()
        queryJson.addProperty("operationName", "SynchronizedTrackLyrics")

        val variablesObject = JsonObject()
        variablesObject.addProperty("trackId", songId.toString())
        queryJson.add("variables", variablesObject)

        val query = """query SynchronizedTrackLyrics(${"$"}trackId: String!) {
  track(trackId: ${"$"}trackId) {
    ...SynchronizedTrackLyrics
    __typename
  }
}
fragment SynchronizedTrackLyrics on Track {
  id
  lyrics {
    ...Lyrics
    __typename
  }
  __typename
}
fragment Lyrics on Lyrics {
  id
  synchronizedLines {
    ...LyricsSynchronizedLines
    __typename
  }
  __typename
}
fragment LyricsSynchronizedLines on LyricsSynchronizedLine {
  lrcTimestamp
  line
  __typename
}
"""
        queryJson.addProperty("query", query)

        return queryJson
    }

    interface ApiService {
        @GET("https://auth.deezer.com/login/anonymous?jo=p")
        fun auth(): Call<JsonObject>?

        @GET("search")
        fun search(@Query("q") query: String): Call<JsonObject>?

        @GET("track/{trackId}")
        fun getTrackInfo(@Path("trackId") trackId: Int): Call<JsonObject>?

        @POST("https://pipe.deezer.com/api")
        fun query(
            @Body body: JsonObject,
            @Header("Authorization") authString: String
        ): Call<JsonObject>?
    }
}

package io.github.teccheck.fastlyrics.api.provider

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
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

// TODO: Use a graphql library for this
object Deezer : LyricsProvider {
    private const val TAG = "DeezerProvider"

    private const val KEY_DATA = "data"
    private const val KEY_SEARCH = "search"
    private const val KEY_TRACKS = "tracks"
    private const val KEY_RESULTS = "results"
    private const val KEY_ID = "id"
    private const val KEY_TITLE = "title"
    private const val KEY_LABEL = "label"
    private const val KEY_ALBUM = "album"
    private const val KEY_COVER = "cover"
    private const val KEY_NAME = "name"
    private const val KEY_JWT = "jwt"
    private const val KEY_TRACK = "track"
    private const val KEY_LYRICS = "lyrics"
    private const val KEY_CONTRIBUTORS = "contributors"
    private const val KEY_EDGES = "edges"
    private const val KEY_NODE = "node"
    private const val KEY_ROLES = "roles"
    private const val KEY_TEXT = "text"
    private const val KEY_URLS = "urls"
    private const val KEY_SYNCED_LINES = "synchronizedLines"

    private const val ROLE_MAIN = "MAIN"

    private val apiService: ApiService
    private var authToken: String? = null

    init {
        val gson = GsonBuilder().disableJdkUnsafe().create()

        val retrofit = Retrofit.Builder().baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create(gson)).build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "deezer"

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        Log.i(TAG, "Searching for \"$searchQuery\"")

        try {
            val query = getSearchRequest(searchQuery)
            val auth = "Bearer ${getAuthToken()}"

            val json =
                apiService.query(query, auth)?.execute()?.body()?.asJsonObject ?: return Failure(
                    NetworkException()
                )

            val dataJson = json.get(KEY_DATA).asJsonObject
            val tracksJson = dataJson.get(KEY_SEARCH).asJsonObject
                .get(KEY_RESULTS).asJsonObject
                .get(KEY_TRACKS).asJsonObject
                .get(KEY_EDGES).asJsonArray

            return parseSearchResults(tracksJson)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        return Failure(ParseException())
    }

    override fun fetchLyrics(songId: Long): Result<SongWithLyrics, LyricsApiException> {
        try {
            val query = getLyricsQuery(songId)
            val auth = "Bearer ${getAuthToken()}"

            val json =
                apiService.query(query, auth)?.execute()?.body()?.asJsonObject ?: return Failure(
                    NetworkException()
                )

            val dataJson = json.get(KEY_DATA).asJsonObject
            val trackJson = dataJson.get(KEY_TRACK).asJsonObject
            val song = parseTrackJson(trackJson) ?: return Failure(ParseException())

            return Success(song)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        return Failure(ParseException())
    }

    private fun parseSearchResults(json: JsonArray?): Result<List<SearchResult>, LyricsApiException> {
        if (json == null) return Failure(LyricsNotFoundException())

        val results = mutableListOf<SearchResult>()
        for (jsonResult in json) {
            val jo = jsonResult.asJsonObject.get(KEY_NODE).asJsonObject
            val song = parseTrackJson(jo) ?: continue
            val result = SearchResult(
                song.title,
                song.artist,
                song.album,
                song.artUrl,
                song.sourceUrl,
                jo.get(KEY_ID).asString.toLong(),
                this,
                song
            )
            results.add(result)
        }

        return Success(results)
    }

    private fun parseTrackJson(json: JsonObject): SongWithLyrics? {
        try {
            val lyricsJson = json.get(KEY_LYRICS).asJsonObject

            val lyricsSynced = lyricsJson.get(KEY_SYNCED_LINES)?.let {
                if (it.isJsonNull) return@let null
                parseSyncedLyrics(it.asJsonArray)
            }
            val lyricsPlain = lyricsJson.get(KEY_TEXT)?.asString

            val id = json.get(KEY_ID).asString
            val title = json.get(KEY_TITLE).asString
            val album = json.get(KEY_ALBUM).asJsonObject.get(KEY_LABEL).asString
            val contributors = json.get(KEY_CONTRIBUTORS).asJsonObject.get(KEY_EDGES).asJsonArray
            val artist = contributors.first {
                it.asJsonObject
                    .get(KEY_ROLES).asJsonArray
                    .any { it.asString.equals(ROLE_MAIN) }
            }.asJsonObject
                .get(KEY_NODE).asJsonObject
                .get(KEY_NAME).asString

            val artUrl =
                json.get(KEY_ALBUM).asJsonObject.get(KEY_COVER).asJsonObject.get(KEY_URLS).asJsonArray.first().asString
            val link = "https://www.deezer.com/track/$id"

            return SongWithLyrics(
                0,
                title,
                artist,
                lyricsPlain,
                lyricsSynced,
                link,
                album,
                artUrl,
                LyricsType.LRC,
                getName()
            )
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        return null
    }

    private fun parseSyncedLyrics(lines: JsonArray): String {
        return lines.joinToString(separator = "\n") {
            val line = it.asJsonObject
            "${line.get("lrcTimestamp").asString}${line.get("line").asString}"
        }
    }

    private fun getAuthToken(): String? {
        if (authToken == null) authToken =
            apiService.auth()?.execute()?.body()?.get(KEY_JWT)?.asString
        return authToken
    }

    private fun getLyricsQuery(songId: Long): JsonObject {
        // This is GraphQL by hand
        val queryJson = JsonObject()
        queryJson.addProperty("operationName", "Lyrics")

        val variablesObject = JsonObject()

        variablesObject.addProperty("trackId", songId.toString())

        val pictureRequest = JsonObject()
        pictureRequest.addProperty("width", 512)
        pictureRequest.addProperty("height", 512)
        variablesObject.add("pictureRequest", pictureRequest)

        queryJson.add("variables", variablesObject)

        val query = """
            query Lyrics(${"$"}trackId: String!, , ${"$"}pictureRequest: PictureRequest!) {
              track(trackId: ${"$"}trackId) {
                id
                title
                duration
                lyrics {
                  text
                  synchronizedLines {
                    lrcTimestamp
                    line
                  }
                }
                album {
                  label
                  cover {
                    urls(pictureRequest: ${"$"}pictureRequest)
                  }
                }
                contributors {
                  edges {
                    node {
                      ... on Artist {
                        name
                      }
                    }
                    roles
                  }
                }
              }
            }
            """.trimIndent()
        queryJson.addProperty("query", query)
        return queryJson
    }

    private fun getSearchRequest(searchQuery: String): JsonObject {
        val queryJson = JsonObject()
        queryJson.addProperty("operationName", "Search")

        val variablesObject = JsonObject()

        variablesObject.addProperty("query", searchQuery)

        val pictureRequest = JsonObject()
        pictureRequest.addProperty("width", 512)
        pictureRequest.addProperty("height", 512)
        variablesObject.add("pictureRequest", pictureRequest)

        queryJson.add("variables", variablesObject)

        val query = """
            query Search(${"$"}query: String, ${"$"}pictureRequest: PictureRequest!) {
              search(query: ${"$"}query) {
                results {
                  tracks {
                    edges {
                      node {
                        id
                        title
                        duration
                        lyrics {
                          text
                          synchronizedLines {
                            lrcTimestamp
                            line
                          }
                        }
                        album {
                          label
                          cover {
                            urls(pictureRequest: ${"$"}pictureRequest)
                          }
                        }
                        contributors {
                          edges {
                            node {
                              ... on Artist {
                                name
                              }
                            }
                            roles
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()
        queryJson.addProperty("query", query)
        return queryJson
    }

    interface ApiService {
        @GET("https://auth.deezer.com/login/anonymous?jo=p")
        fun auth(): Call<JsonObject>?

        @POST("https://pipe.deezer.com/api")
        fun query(
            @Body body: JsonObject, @Header("Authorization") authString: String
        ): Call<JsonObject>?
    }
}

package io.github.teccheck.fastlyrics.api.provider

import android.util.Base64
import android.util.Log
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.Tokens
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.exceptions.NetworkException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import okhttp3.ResponseBody
import org.w3c.dom.Document
import org.w3c.dom.Element
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import javax.xml.parsers.DocumentBuilderFactory

object PetitLyrics : LyricsProvider {

    // TODO: Find out how to get a cover image
    // TODO: Figure out how to get synced lyrics

    private const val TAG = "PetitLyrics"

    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://on.petitlyrics.com/api/").build()
        apiService = retrofit.create(ApiService::class.java)
    }

    override fun getName() = "petitlyrics"

    override fun search(songMeta: SongMeta): Result<List<SearchResult>, LyricsApiException> {
        try {
            val response = apiService.search(
                songMeta.title,
                songMeta.artist ?: "",
                songMeta.album ?: "",
                songMeta.duration,
            )?.execute()?.body() ?: return Failure(LyricsNotFoundException())

            return Success(wrapSearchResults(response.string()))
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }
    }

    override fun search(searchQuery: String): Result<List<SearchResult>, LyricsApiException> {
        try {
            val response = apiService.search(searchQuery)?.execute()?.body() ?: return Failure(
                LyricsNotFoundException()
            )

            return Success(wrapSearchResults(response.string()))
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return Failure(NetworkException())
        }
    }

    override fun fetchLyrics(songId: Long): Result<SongWithLyrics, LyricsApiException> {
        TODO("Not yet implemented")
    }

    private fun wrapSearchResults(response: String): List<SearchResult> {
        // Why are all java xml libraries so complicated? Now I'm just doing the unmarshalling myself
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = builder.parse(response.byteInputStream())
        doc.documentElement.normalize()

        Log.d(TAG, "Doc: ${doc.textContent}")

        val songs = doc.getElementsByTagName("song")

        val results = mutableListOf<SearchResult>()
        for (i in 0 until songs.length) {
            val song = songs.item(i) as Element
            val lyricsId = song.getElementsByTagName("lyricsId").item(0).textContent.toLong()
            val title = song.getElementsByTagName("title").item(0).textContent
            val artist = song.getElementsByTagName("artist").item(0).textContent
            val album = song.getElementsByTagName("album").item(0).textContent
            val lyricsType = song.getElementsByTagName("lyricsType").item(0).textContent.toInt()
            val lyricsData = song.getElementsByTagName("lyricsData").item(0).textContent

            val lyrics = String(Base64.decode(lyricsData, Base64.DEFAULT))

            val songWithLyrics = SongWithLyrics(
                0,
                title,
                artist,
                lyrics,
                null,
                "https://petitlyrics.com/lyrics/${lyricsId}",
                album,
                null,
                null,
                LyricsType.RAW_TEXT,
                getName()
            )

            results.add(
                SearchResult(
                    songWithLyrics.title,
                    songWithLyrics.artist,
                    songWithLyrics.album,
                    songWithLyrics.artUrl,
                    songWithLyrics.sourceUrl,
                    lyricsId,
                    this,
                    songWithLyrics
                )
            )
        }

        return results
    }

    interface ApiService {
        @POST("GetPetitLyricsData.php")
        @FormUrlEncoded
        fun search(
            @Field("key_title") title: String,
            @Field("key_artist") artist: String = "",
            @Field("key_album") album: String = "",
            @Field("key_duration") duration: Long? = null,
            @Field("lyricsType") lyricsType: Int = 1,
            @Field("maxCount") maxCount: Int? = null,
            @Field("terminalType") terminalType: Int = 10,
            @Field("clientAppId") clientAppId: String = Tokens.PETIT_LYRICS_API
        ): Call<ResponseBody>?
    }
}
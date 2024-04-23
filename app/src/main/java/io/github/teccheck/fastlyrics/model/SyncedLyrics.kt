package io.github.teccheck.fastlyrics.model

import android.text.format.DateUtils
import com.dirror.lyricviewx.LyricEntry
import java.util.TreeMap
import java.util.regex.Pattern

class SyncedLyrics private constructor(entries: List<Pair<Long, String>>) {

    private val entries: TreeMap<Long, Int> = TreeMap()

    private val lines = mutableListOf<String>()

    init {
        for (entry in entries.sortedBy { it.first }) {
            lines.add(entry.second)
            this.entries[entry.first] = lines.lastIndex
        }
    }

    fun getLineIndex(time: Long): Int? = entries.floorEntry(time)?.value

    fun getLineByIndex(index: Int) = lines[index]

    fun getFullText(): String = lines.joinToString("\n")

    fun getLineCount() = lines.size

    fun getList() = entries.map { LyricEntry(it.key, lines[it.value]) }

    companion object {
        private val PATTERN_LINE = Pattern.compile("((\\[\\d\\d:\\d\\d\\.\\d{2,3}\\])+)(.+)")
        private val PATTERN_TIME = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]")

        fun parseLrc(text: String): SyncedLyrics? {
            if (text.isEmpty()) return null

            val pairs = text.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }
                .map { parseLrcLine(it.trim()) }.flatten().sortedBy { it.first }

            return SyncedLyrics(pairs)
        }

        private fun parseLrcLine(line: String): List<Pair<Long, String>> {
            val entries: MutableList<Pair<Long, String>> = ArrayList()

            val lineMatcher = PATTERN_LINE.matcher(line)
            if (!lineMatcher.matches()) return entries

            val times = lineMatcher.group(1) ?: return entries
            val text = lineMatcher.group(3) ?: return entries

            val timeMatcher = PATTERN_TIME.matcher(times) ?: return entries
            while (timeMatcher.find()) {
                val min = timeMatcher.group(1)!!.toLong()
                val sec = timeMatcher.group(2)!!.toLong()

                val milString = timeMatcher.group(3)!!
                var mil = milString.toLong()
                if (milString.length == 2) mil *= 10

                val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
                entries.add(Pair(time, text))
            }

            return entries
        }
    }
}
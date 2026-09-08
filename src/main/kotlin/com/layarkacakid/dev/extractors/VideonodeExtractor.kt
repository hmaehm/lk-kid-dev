package com.layarkacakid.dev.extractors

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities

open class VideonodeExtractor : ExtractorApi() {
    override val name = "Videonode"
    override val mainUrl = "https://videonode.de"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Referer" to (referer ?: mainUrl),
            "Accept" to "*/*"
        )
        val res = app.get(url, headers = headers).text

        // Match direct m3u8 or mp4 links in scripts / configs
        val mediaRegex = Regex("""(?:file|source|src)\s*:\s*["']([^"']+\.(?:m3u8|mp4)[^"']*)["']""")
        mediaRegex.findAll(res).forEach { match ->
            val videoUrl = match.groupValues[1]
            val isM3u8 = videoUrl.contains(".m3u8")
            callback.invoke(
                ExtractorLink(
                    source = name,
                    name = name,
                    url = videoUrl,
                    referer = url,
                    quality = Qualities.Unknown.value,
                    isM3u8 = isM3u8
                )
            )
        }
    }
}

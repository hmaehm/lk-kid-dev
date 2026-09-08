package com.layarkacakid.dev.extractors

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import org.jsoup.Jsoup

open class DadadidiExtractor : ExtractorApi() {
    override val name = "Dadadidi"
    override val mainUrl = "https://dadadidi.de"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Referer" to (referer ?: mainUrl)
        )
        val res = app.get(url, headers = headers).text
        val doc = Jsoup.parse(res)

        // Find direct download or stream anchors
        doc.select("a[href*=.mp4], a[href*=.m3u8], a.btn-download, a[href*=/download/]").forEach { link ->
            val href = link.attr("abs:href").ifEmpty { link.attr("href") }
            if (href.isNotBlank()) {
                val quality = link.text().trim()
                callback.invoke(
                    ExtractorLink(
                        source = name,
                        name = "$name $quality".trim(),
                        url = href,
                        referer = url,
                        quality = Qualities.Unknown.value,
                        isM3u8 = href.contains(".m3u8")
                    )
                )
            }
        }
    }
}

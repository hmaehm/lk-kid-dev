package com.layarkacakid.dev.extractors

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.newExtractorLink
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
        for (link in doc.select("a[href*=.mp4], a[href*=.m3u8], a.btn-download, a[href*=/download/]")) {
            val href = link.attr("abs:href").ifEmpty { link.attr("href") }
            if (href.isNotBlank()) {
                val quality = link.text().trim()
                callback.invoke(
                    newExtractorLink(
                        source = name,
                        name = "$name $quality".trim(),
                        url = href,
                        type = if (href.contains(".m3u8")) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
                    ) {
                        this.referer = url
                    }
                )
            }
        }
    }
}

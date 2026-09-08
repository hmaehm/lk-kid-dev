package com.layarkacakid.dev.extractors

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.newExtractorLink

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
        val hostMatch = Regex("""videonode\.de/(?:iframe3|embed|player)/([^/]+)/([^/?#]+)""").find(url)
        val host = hostMatch?.groupValues?.get(1) ?: "p2p"
        val id = hostMatch?.groupValues?.get(2) ?: url.trimEnd('/').split("/").lastOrNull() ?: ""

        if (id.isBlank()) return

        val postHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Referer" to url,
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With" to "XMLHttpRequest"
        )

        try {
            val apiRes = app.post(
                "$mainUrl/api.php",
                headers = postHeaders,
                data = mapOf("host" to host, "id" to id)
            ).text

            val embedUrlMatch = Regex("""\"embedUrl\"\s*:\s*\"([^\"]+)\"""").find(apiRes)
            val embedUrl = embedUrlMatch?.groupValues?.get(1)?.replace("\\/", "/")

            if (!embedUrl.isNullOrBlank()) {
                if (embedUrl.contains("playcdn.de")) {
                    val slug = embedUrl.trimEnd('/').split("/").lastOrNull() ?: ""
                    if (slug.isNotBlank()) {
                        val verifyHeaders = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                            "Referer" to embedUrl,
                            "X-Requested-With" to "XMLHttpRequest"
                        )
                        val verifyRes = app.get("https://playcdn.de/verify/$slug", headers = verifyHeaders).text
                        val fileUrlMatch = Regex("""\"fileUrl\"\s*:\s*\"([^\"]+)\"""").find(verifyRes)
                        val fileUrl = fileUrlMatch?.groupValues?.get(1)?.replace("\\/", "/")
                        if (!fileUrl.isNullOrBlank()) {
                            callback.invoke(
                                newExtractorLink(
                                    source = "Videonode P2P",
                                    name = "Videonode P2P",
                                    url = fileUrl,
                                    type = ExtractorLinkType.M3U8
                                ) {
                                    this.referer = "https://playcdn.de/"
                                }
                            )
                        }
                    }
                } else if (embedUrl.contains("turbovid") || embedUrl.contains("turboviplay")) {
                    val turboRes = app.get(
                        embedUrl,
                        headers = mapOf(
                            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
                            "Referer" to "https://videonode.de/"
                        )
                    ).text
                    val m3u8Match = Regex("""https?://[^\s\"\'<>]+\.m3u8[^\s\"\'<>]*""").find(turboRes)
                    if (m3u8Match != null) {
                        callback.invoke(
                            newExtractorLink(
                                source = "Videonode TURBOVIP",
                                name = "Videonode TURBOVIP",
                                url = m3u8Match.value,
                                type = ExtractorLinkType.M3U8
                            ) {
                                this.referer = embedUrl
                            }
                        )
                    } else {
                        loadExtractor(embedUrl, "https://videonode.de/", subtitleCallback, callback)
                    }
                } else {
                    loadExtractor(embedUrl, "https://videonode.de/", subtitleCallback, callback)
                }
            }
        } catch (e: Exception) {
            // Fallback
        }
    }
}

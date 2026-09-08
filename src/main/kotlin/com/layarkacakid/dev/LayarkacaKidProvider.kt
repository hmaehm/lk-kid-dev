package com.layarkacakid.dev

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class LayarkacaKidProvider : MainAPI() {
    companion object {
        const val PREF_DOMAIN_KEY = "lk21_custom_domain"
        const val DEFAULT_DOMAIN = "https://tv12.lk21official.cc"
        const val ALT_DOMAIN = "https://lk21.de"
        const val DRAMA_DOMAIN = "https://dramamu.lk21.de"
    }

    override var name = "Layarkaca Kid Dev"
    override var lang = "id"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.AsianDrama)

    override var mainUrl: String
        get() = getKey<String>(PREF_DOMAIN_KEY)?.takeIf { it.isNotBlank() } ?: DEFAULT_DOMAIN
        set(value) {
            setKey(PREF_DOMAIN_KEY, value)
        }

    override val mainPage = mainPageOf(
        "/latest" to "Film Terbaru",
        "/populer" to "Populer",
        "/rating" to "Rating Tertinggi",
        "/nontondrama?page=latest-series" to "Series Terbaru"
    )

    private val defaultHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val targetUrl = if (page <= 1) {
            "${mainUrl}${request.data}"
        } else {
            if (request.data.contains("?")) {
                "${mainUrl}${request.data}&p=$page"
            } else {
                "${mainUrl}${request.data}/page/$page"
            }
        }

        val res = app.get(targetUrl, headers = defaultHeaders).text
        val doc = Jsoup.parse(res)
        val homeItems = parseMediaCards(doc)

        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = homeItems,
                isHorizontalImages = false
            ),
            hasNext = homeItems.isNotEmpty()
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrls = listOf(
            "$mainUrl/search?s=$query",
            "$mainUrl/?s=$query"
        )

        for (url in searchUrls) {
            try {
                val res = app.get(url, headers = defaultHeaders).text
                val doc = Jsoup.parse(res)
                val results = parseMediaCards(doc)
                if (results.isNotEmpty()) {
                    return results
                }
            } catch (e: Exception) {
                // Continue trying fallback
            }
        }
        return emptyList()
    }

    private fun parseMediaCards(doc: Element): List<SearchResponse> {
        val items = mutableListOf<SearchResponse>()
        // Match common LK21 article cards or linked cards
        val cardElements = doc.select("article, .movie-item, .item, .grid-item, div.article")

        if (cardElements.isNotEmpty()) {
            for (el in cardElements) {
                val aTag = el.selectFirst("a[href]") ?: continue
                val href = fixUrl(aTag.attr("href"))
                val title = el.selectFirst(".title, h2, h3, .entry-title")?.text()?.trim()
                    ?: aTag.attr("title").ifEmpty { aTag.text() }.trim()
                if (title.isBlank() || href.isBlank()) continue

                val poster = el.selectFirst("img")?.let { img ->
                    img.attr("data-src").ifEmpty { img.attr("src") }
                }
                val rating = el.selectFirst(".rating, .score")?.text()?.trim()
                val isSeries = href.contains("nontondrama") ||
                        el.text().contains("EPS", ignoreCase = true) ||
                        el.text().contains("Series", ignoreCase = true)

                if (isSeries) {
                    items.add(newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                        this.posterUrl = poster
                        addRating(rating)
                    })
                } else {
                    items.add(newMovieSearchResponse(title, href, TvType.Movie) {
                        this.posterUrl = poster
                        addRating(rating)
                    })
                }
            }
        } else {
            // Fallback: parse direct links matching year slug pattern (e.g., /movie-name-2026)
            doc.select("a[href*=-202], a[href*=-199], a[href*=-201], a[href*=-200]").forEach { a ->
                val href = fixUrl(a.attr("href"))
                val title = a.attr("title").ifEmpty { a.text() }.trim()
                if (title.isNotBlank() && !href.contains("/genre/") && !href.contains("/country/")) {
                    val poster = a.selectFirst("img")?.let { it.attr("data-src").ifEmpty { it.attr("src") } }
                    val isSeries = href.contains("nontondrama") || a.text().contains("EPS")
                    if (isSeries) {
                        items.add(newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
                            this.posterUrl = poster
                        })
                    } else {
                        items.add(newMovieSearchResponse(title, href, TvType.Movie) {
                            this.posterUrl = poster
                        })
                    }
                }
            }
        }

        return items.distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse {
        var currentUrl = fixUrl(url)
        val res = app.get(currentUrl, headers = defaultHeaders).text
        var doc = Jsoup.parse(res)

        // Check if this page redirects to nontondrama (Series)
        val redirectBtn = doc.selectFirst("a[href*='dramamu'], a[href*='nontondrama']")
        if (redirectBtn != null && doc.text().contains("dialihkan", ignoreCase = true)) {
            val redirectedUrl = redirectBtn.attr("href")
            currentUrl = fixUrl(redirectedUrl)
            val seriesRes = app.get(currentUrl, headers = defaultHeaders).text
            doc = Jsoup.parse(seriesRes)
        }

        val title = doc.selectFirst("h1, .entry-title")?.text()
            ?.replace("Lk21", "")
            ?.replace("Nonton", "")
            ?.replace("Sub Indo", "")
            ?.replace("Series", "")
            ?.trim() ?: "Unknown Title"

        val poster = doc.selectFirst(".poster img, .content-poster img, img.attachment-post-thumbnail")?.let {
            it.attr("data-src").ifEmpty { it.attr("src") }
        }

        val plot = doc.selectFirst(".synopsis, .entry-content p, .deskripsi, p.storyline")?.text()?.trim()
        val year = doc.selectFirst("a[href*='/year/']")?.text()?.trim()?.toIntOrNull()
        val rating = doc.selectFirst(".rating, .score")?.text()?.trim()
        val tags = doc.select("a[href*='/genre/']").map { it.text().trim() }
        val actors = doc.select("a[href*='/artist/']").map { it.text().trim() }

        val isSeries = currentUrl.contains("dramamu") ||
                currentUrl.contains("nontondrama") ||
                doc.select(".episode-list, .season-list, a[href*='episode-']").isNotEmpty()

        if (isSeries) {
            val episodes = mutableListOf<Episode>()
            val epElements = doc.select(".episode-list a, .episodes a, a[href*='episode-']")
            if (epElements.isNotEmpty()) {
                epElements.forEachIndexed { idx, epEl ->
                    val epHref = fixUrl(epEl.attr("href"))
                    val epName = epEl.text().trim().ifEmpty { "Episode ${idx + 1}" }
                    episodes.add(
                        newEpisode(epHref) {
                            this.name = epName
                            this.episode = idx + 1
                        }
                    )
                }
            } else {
                // Single episode fallback
                episodes.add(
                    newEpisode(currentUrl) {
                        this.name = "Episode 1"
                        this.episode = 1
                    }
                )
            }

            return newTvSeriesLoadResponse(title, currentUrl, TvType.TvSeries, episodes) {
                this.posterUrl = poster
                this.year = year
                this.plot = plot
                this.tags = tags
                this.actors = actors
                addRating(rating)
            }
        }

        return newMovieLoadResponse(title, currentUrl, TvType.Movie, currentUrl) {
            this.posterUrl = poster
            this.year = year
            this.plot = plot
            this.tags = tags
            this.actors = actors
            addRating(rating)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val watchUrl = fixUrl(data)
        val res = app.get(watchUrl, headers = defaultHeaders).text
        val doc = Jsoup.parse(res)

        // 1. Check embedded player iframes or server buttons
        val playerLinks = mutableListOf<String>()

        doc.select("iframe[src]").forEach { iframe ->
            val src = iframe.attr("src")
            if (src.isNotBlank()) playerLinks.add(fixUrl(src))
        }

        doc.select("a[href*='videonode.de'], a[href*='dadadidi.de'], a[href*='iframe']").forEach { a ->
            val href = a.attr("href")
            if (href.isNotBlank()) playerLinks.add(fixUrl(href))
        }

        // 2. Load links via registered extractors
        for (link in playerLinks.distinct()) {
            try {
                loadExtractor(link, watchUrl, subtitleCallback, callback)
            } catch (e: Exception) {
                // Ignore failure for individual mirrors
            }
        }

        return playerLinks.isNotEmpty()
    }

    override fun fixUrl(url: String): String {
        if (url.startsWith("//")) {
            return "https:$url"
        }
        if (url.startsWith("/")) {
            return "$mainUrl$url"
        }
        return url
    }
}

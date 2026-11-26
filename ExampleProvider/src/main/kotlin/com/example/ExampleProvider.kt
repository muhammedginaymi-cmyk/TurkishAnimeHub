package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.HomePageList
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrlNull
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newTvSeriesSearchResponse

class ExampleProvider : MainAPI() { // All providers must be an instance of MainAPI
    override var mainUrl = "https://krmzi.org" 
    override var name = "krmzi تركي"
    override val supportedTypes = setOf(TvType.TvSeries)

    override var lang = "ar"

    // Enable this when your provider has a main page
    override val hasMainPage = true

    // This function gets called when you search for something
    override suspend fun search(query: String): List<SearchResponse> {
        override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get(mainUrl).document
        val items = ArrayList<HomePageList>()

        val cards = document.select("article, .post, .episode-box, a[href]")
        val latestEpisodes = cards.mapNotNull { element ->
            val linkEl = if (element.tagName() == "a") element else element.selectFirst("a[href]")
            val href = fixUrlNull(linkEl?.attr("href")) ?: return@mapNotNull null

            val rawTitle = element.text().trim()
                .ifEmpty { linkEl?.attr("title")?.trim() ?: "" }
            if (rawTitle.isEmpty()) return@mapNotNull null

            val epNumber = Regex("""حلقة\s*(\d+)""").find(rawTitle)?.groupValues?.get(1)?.toIntOrNull()

            val poster = element.selectFirst("img")?.let { img ->
                fixUrlNull(img.attr("data-src").ifEmpty { img.attr("src") })
            }

            val displayTitle = if (epNumber != null && !rawTitle.contains("حلقة")) {
                "$rawTitle - حلقة $epNumber"
            } else {
                rawTitle
            }

            newTvSeriesSearchResponse(displayTitle, href, TvType.TvSeries) {
                this.posterUrl = poster
            }
        }.distinctBy { it.url }.take(30)

        if (latestEpisodes.isNotEmpty()) {
            items.add(HomePageList("أحدث الحلقات", latestEpisodes))
        }

        return newHomePageResponse(items)
    }
    
        return listOf()
    }
}
package com.layarkacakid.dev

import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.layarkacakid.dev.extractors.DadadidiExtractor
import com.layarkacakid.dev.extractors.VideonodeExtractor

@CloudstreamPlugin
class LayarkacaKidPlugin : BasePlugin() {
    override fun load() {
        // Register main API provider
        registerMainAPI(LayarkacaKidProvider())

        // Register custom video link extractors
        registerExtractorAPI(VideonodeExtractor())
        registerExtractorAPI(DadadidiExtractor())
    }
}

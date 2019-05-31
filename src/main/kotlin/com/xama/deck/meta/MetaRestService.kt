package com.xama.deck.meta

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MetaRestService {
    @GetMapping("/version")
    fun version() = CustomInfoContributor.VERSION_DATA
}
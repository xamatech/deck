package com.xama.deck.meta

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import java.io.File

@Component
class CustomInfoContributor: InfoContributor {
    companion object {
        // file is created while Docker image is built on Circle CI
        val VERSION_DATA =  File("/meta/version").let { if(it.isFile) it.readText() else "not available" }
    }

    override fun contribute(builder: Info.Builder) {
        builder.withDetail("version", VERSION_DATA)
    }
}
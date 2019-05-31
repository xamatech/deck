package com.xama.deck

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
import org.springframework.cloud.netflix.zuul.EnableZuulProxy


@EnableZuulProxy
@EnableEurekaServer
@SpringBootApplication
class DeckApplication


fun main(args: Array<String>) {
	runApplication<DeckApplication>(*args)
}

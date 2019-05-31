package com.xama.deck

import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Profile(AppProfile.SECURITY)
@EnableWebSecurity
internal class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().ignoringAntMatchers("/eureka/**")
        super.configure(http)
    }
}


@Profile("!${AppProfile.SECURITY}")
@EnableWebSecurity
internal class NoSecurityConfig : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().ignoringAntMatchers("/eureka/**")
            .and()
            .authorizeRequests().anyRequest().permitAll()
    }
}
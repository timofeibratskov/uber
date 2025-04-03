package com.example.rating_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@EnableDiscoveryClient
@SpringBootApplication
class RatingServiceApplication

fun main(args: Array<String>) {
	runApplication<RatingServiceApplication>(*args)
}

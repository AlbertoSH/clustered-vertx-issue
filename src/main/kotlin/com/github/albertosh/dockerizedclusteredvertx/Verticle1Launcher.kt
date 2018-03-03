package com.github.albertosh.dockerizedclusteredvertx

import io.vertx.core.VertxOptions
import io.vertx.reactivex.core.Vertx

fun main(args: Array<String>) {
    Vertx.rxClusteredVertx(VertxOptions())
            .flatMap { it.rxDeployVerticle(Verticle1::class.qualifiedName) }
            .subscribe()
}
package com.github.albertosh.dockerizedclusteredvertx

import io.vertx.core.Future
import io.vertx.ext.healthchecks.Status
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler
import io.vertx.reactivex.ext.web.Router

class Verticle2 : AbstractVerticle() {

    companion object {
        const val address = "verticle2"
    }

    private val health by lazy {
        HealthCheckHandler.create(vertx)
                .register("self (v2)") {
                    it.complete(Status.OK())
                }
                .register("other (v1)") { healthFuture ->
                    vertx.eventBus()
                            .rxSend<String>(Verticle1.address, "Hello")
                            .subscribe(
                                    { healthFuture.complete(Status.OK()) },
                                    { healthFuture.complete(Status.KO()) }
                            )
                }
    }

    override fun start(startFuture: Future<Void>) {

        vertx.eventBus().consumer<String>(Verticle2.address) { it.reply("World") }

        val router = Router.router(vertx).apply {
            route().handler(health)
        }

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(9001)
                .subscribe(
                        { startFuture.complete() },
                        { startFuture.fail(it) }
                )
    }
}
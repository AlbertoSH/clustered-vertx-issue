package com.github.albertosh.dockerizedclusteredvertx

import io.vertx.core.Future
import io.vertx.ext.healthchecks.Status
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.healthchecks.HealthCheckHandler
import io.vertx.reactivex.ext.web.Router

class Verticle1 : AbstractVerticle() {

    companion object {
        const val address = "verticle1"
    }

    private val health by lazy {
        HealthCheckHandler.create(vertx)
                .register("self (v1)") {
                    it.complete(Status.OK())
                }
                .register("other (v2)") { healthFuture ->
                    vertx.eventBus()
                            .rxSend<String>(Verticle2.address, "Hello")
                            .subscribe(
                                    { healthFuture.complete(Status.OK()) },
                                    { healthFuture.complete(Status.KO()) }
                            )
                }
    }

    override fun start(startFuture: Future<Void>) {

        vertx.eventBus().consumer<String>(Verticle1.address) { it.reply("World") }

        val router = Router.router(vertx).apply {
            route().handler(health)
        }

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .rxListen(9000)
                .subscribe(
                        { startFuture.complete() },
                        { startFuture.fail(it) }
                )
    }
}
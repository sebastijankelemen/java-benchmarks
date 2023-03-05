package org.sk.bench;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public final class Launcher
{
    private final static Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
    private Launcher() {}

    public static void main( String[] args )
    {
        LOGGER.info("Running web app example");
        Vertx vertx = Vertx.vertx();
        Future<String> dbFuture = vertx.deployVerticle(new DatabaseVerticle());
        Future<String> httpFuture = vertx.deployVerticle(new HttpServerVerticle());
        Future<CompositeFuture> compositeFuture = CompositeFuture.all(dbFuture, httpFuture);
        compositeFuture.onSuccess(handler -> LOGGER.info("Verticles deployed"));
        compositeFuture.onFailure(throwable -> LOGGER.error("Error", throwable));
    }
}

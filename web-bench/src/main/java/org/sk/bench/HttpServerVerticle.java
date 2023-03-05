package org.sk.bench;

import com.github.rjeschke.txtmark.Processor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private static final String EMPTY_PAGE_MARKDOWN =
            "# A new page\n" +
                    "\n" +
                    "Feel-free to write in Markdown!\n";
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private FreeMarkerTemplateEngine templateEngine;

    @Override
    public void start(Promise<Void> startPromise) {
        initHttpServer(startPromise);
    }

    private void initHttpServer(Promise<Void> startPromise) {
        templateEngine = FreeMarkerTemplateEngine.create(vertx);
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.get("/").handler(this::indexHandler);
        router.get("/wiki/:page").handler(this::pageRenderingHandler);
        router.post().handler(BodyHandler.create());
        router.post("/save").handler(this::pageUpdateHandler);
        router.post("/create").handler(this::pageCreateHandler);
        router.post("/delete").handler(this::pageDeletionHandler);
        server.requestHandler(router);
        Future<HttpServer> serverFuture = server.listen(PORT, HOST);
        serverFuture.onSuccess(handler -> {
            LOGGER.info("HTTP server running");
            startPromise.complete();
        });
        serverFuture.onFailure(throwable -> {
            LOGGER.error("Error", throwable);
            startPromise.fail(throwable);
        });
    }

    private void pageDeletionHandler(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        JsonObject request = new JsonObject().put("id", id);
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "delete-page");
        EventBus bus = vertx.eventBus();
        String address = DatabaseVerticle.class.getName();
        Future<Message<Object>> requestFuture = bus.request(address, request, options);
        requestFuture.onSuccess(handler -> {
            routingContext.response().setStatusCode(303);
            routingContext.response().putHeader("Location", "/");
            routingContext.response().end();
        });
        requestFuture.onFailure(throwable -> LOGGER.error("Error", throwable));
    }

    private void pageCreateHandler(RoutingContext routingContext) {
        String pageName = routingContext.request().getParam("name");
        String location = "/wiki/" + pageName;
        if (pageName == null || pageName.isEmpty()) {
            location = "/";
        }
        routingContext.response().setStatusCode(303);
        routingContext.response().putHeader("Location", location);
        routingContext.response().end();
    }

    private void pageUpdateHandler(RoutingContext routingContext) {
        String title = routingContext.request().getParam("title");
        JsonObject request = new JsonObject();
        request.put("id", routingContext.request().getParam("id"));
        request.put("title", title);
        request.put("markdown", routingContext.request().getParam("markdown"));
        DeliveryOptions options = new DeliveryOptions();
        if ("yes".equals(routingContext.request().getParam("newPage"))) {
            options.addHeader("action", "create-page");
        } else {
            options.addHeader("action", "save-page");
        }
        EventBus bus = vertx.eventBus();
        String address = DatabaseVerticle.class.getName();
        Future<Message<Object>> requestFuture = bus.request(address, request, options);
        requestFuture.onSuccess(handler -> {
            routingContext.response().setStatusCode(303);
            routingContext.response().putHeader("Location", "/wiki/" + title);
            routingContext.response().end();
        });
        requestFuture.onFailure(throwable -> LOGGER.error("Error", throwable));
    }

    private void pageRenderingHandler(RoutingContext routingContext) {
        String requestedPage = routingContext.request().getParam("page");
        JsonObject request = new JsonObject().put("page", requestedPage);
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "get-page");
        EventBus bus = vertx.eventBus();
        String address = DatabaseVerticle.class.getName();
        Future<Message<Object>> requestFuture = bus.request(address, request, options);
        requestFuture.onSuccess(handler -> {
            JsonObject body = (JsonObject) handler.body();
            boolean found = body.getBoolean("found");
            String rawContent = body.getString("rawContent", EMPTY_PAGE_MARKDOWN);
            context.put("title", requestedPage);
            context.put("id", body.getInteger("id", -1));
            context.put("newPage", found ? "no" : "yes");
            context.put("rawContent", rawContent);
            context.put("content", Processor.process(rawContent));
            context.put("timestamp", Instant.now().getEpochSecond());
            JsonObject context = routingContext.body().asJsonObject();
            Future<Buffer> renderFuture = templateEngine.render(context, "templates/page.ftl");
            renderFuture.onSuccess(result -> {
                routingContext.response().putHeader("Content-Type", "text/html");
                routingContext.response().end(result);
            });
            renderFuture.onFailure(throwable -> LOGGER.error("Error", throwable));

        });
        requestFuture.onFailure(throwable -> LOGGER.error("Error", throwable));
    }

    private void indexHandler(RoutingContext routingContext) {
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "all-pages");
        String address = DatabaseVerticle.class.getName();
        EventBus bus = vertx.eventBus();
        Future<Message<Object>> requestFuture = bus.request(address, new JsonObject(), options);
        requestFuture.onSuccess(handler -> {
            JsonObject body = (JsonObject) handler.body();
            context.put("title", "Wiki home");
            context.put("pages", body.getJsonArray("pages").getList());
            JsonObject context = routingContext.body().asJsonObject();
            System.out.println(context);
            Future<Buffer> renderFuture = templateEngine.render(context, "templates/index.ftl");
            renderFuture.onSuccess(result -> {
                routingContext.response().putHeader("Content-Type", "text/html");
                routingContext.response().end(result);
            });
            renderFuture.onFailure(throwable -> LOGGER.error("Error", throwable));
        });
        requestFuture.onFailure(throwable -> LOGGER.error("Error", throwable));



    }
}

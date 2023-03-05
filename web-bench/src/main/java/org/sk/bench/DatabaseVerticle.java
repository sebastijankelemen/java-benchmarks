package org.sk.bench;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public final class DatabaseVerticle extends AbstractVerticle {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatabaseVerticle.class);

    private static final String SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key, Name varchar(255) unique, Content clob)";
    private static final String SQL_GET_PAGE = "select Id, Content from Pages where Name = ?";
    private static final String SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)";
    private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";
    private static final String SQL_ALL_PAGES = "select Name from Pages";
    private static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";

    private JDBCClient client;

    @Override
    public void start(Promise<Void> startPromise) {
        initDatabase(startPromise);
    }

    private JsonObject getDatabaseConfig() {
        JsonObject config = new JsonObject();
        config.put("url", "jdbc:hsqldb:file:db/db-data");
        config.put("driver_class", "org.hsqldb.jdbcDriver");
        config.put("max_pool_size", 10);
        return config;
    }

    private void initDatabase(Promise<Void> startPromise) {
        client = JDBCClient.create(vertx, getDatabaseConfig());
        client.getConnection(handler -> {
            if (handler.failed()) {
                LOGGER.error("Error", handler.cause());
                startPromise.fail(handler.cause());
            } else {
                createPages(handler.result(), startPromise);
            }
        });

    }

    private void createPages(SQLConnection connection, Promise<Void> startPromise) {
        try(connection) {
            connection.execute(SQL_CREATE_PAGES_TABLE, result -> {
                if(result.failed()) {
                    LOGGER.error("Error", result.cause());
                    startPromise.fail(result.cause());
                } else {
                    vertx.eventBus().consumer(DatabaseVerticle.class.getName(), this::onMessage);
                    startPromise.complete();
                }
            });
        }
    }

    private void onMessage(Message<JsonObject> message) {
        String action = message.headers().get("action");
        switch (action) {
            case "all-pages" -> fetchAllPages(message);
            case "get-page" -> fetchPage(message);
            case "create-page" -> createPage(message);
            case "save-page" -> savePage(message);
            case "delete-page" -> deletePage(message);
            default -> message.fail(2, "Bad action: " + action);
        }
    }

    private void deletePage(Message<JsonObject> message) {
        JsonArray data = new JsonArray().add(message.body().getString("id"));
        client.updateWithParams(SQL_DELETE_PAGE, data, result -> {
            if (result.succeeded()) {
                message.reply("ok");
            } else {
                LOGGER.error("Error", result.cause());
            }
        });
    }

    private void savePage(Message<JsonObject> message) {
        JsonObject request = message.body();
        JsonArray data = new JsonArray()
                .add(request.getString("markdown"))
                .add(request.getString("id"));
        client.updateWithParams(SQL_SAVE_PAGE, data, result -> {
            if (result.succeeded()) {
                message.reply("ok");
            } else {
                LOGGER.error("Error", result.cause());
            }
        });
    }

    private void createPage(Message<JsonObject> message) {
        JsonObject request = message.body();
        JsonArray data = new JsonArray()
                .add(request.getString("title"))
                .add(request.getString("markdown"));
        client.updateWithParams(SQL_CREATE_PAGE, data, result -> {
            if (result.succeeded()) {
                message.reply("ok");
            } else {
                LOGGER.error("Error", result.cause());
            }
        });
    }

    private void fetchPage(Message<JsonObject> message) {
        String requestedPage = message.body().getString("page");
        JsonArray params = new JsonArray().add(requestedPage);
        client.queryWithParams(SQL_GET_PAGE, params, result -> {
            if (result.succeeded()) {
                JsonObject response = new JsonObject();
                ResultSet resultSet = result.result();
                if (resultSet.getNumRows() == 0) {
                    response.put("found", false);
                } else {
                    response.put("found", true);
                    JsonArray row = resultSet.getResults().get(0);
                    response.put("id", row.getInteger(0));
                    response.put("rawContent", row.getString(1));
                }
                message.reply(response);
            } else {
                LOGGER.error("Error", result.cause());
            }
        });
    }

    private void fetchAllPages(Message<JsonObject> message) {
        client.query(SQL_ALL_PAGES, result -> {
            if(result.failed()) {
                LOGGER.error("Error", result.cause());
            } else {
                List<String> pages = result.result()
                        .getResults()
                        .stream()
                        .map(json -> json.getString(0))
                        .sorted()
                        .collect(Collectors.toList());
                message.reply(new JsonObject().put("pages", new JsonArray(pages)));
            }
        });
    }
}

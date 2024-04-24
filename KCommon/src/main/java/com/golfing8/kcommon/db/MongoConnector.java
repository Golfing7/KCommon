package com.golfing8.kcommon.db;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A connector to the Mongo database.
 */
public class MongoConnector implements Closeable {
    private static final String DATABASE_NAME = "kcommon";
    private static final String CONNECTION_URI_FORMAT = "mongodb://%s:%d/";

    private String username;
    private String password;
    private String serverAddress;
    private int port;
    private final String databaseName;
    private String connectionString;

    @Getter
    private MongoClient client;
    @Getter
    private MongoDatabase database;
    public MongoConnector(String connectionString, String databaseName) {
        this.databaseName = databaseName;
        this.connectionString = connectionString;
    }

    public MongoConnector(String username, String password, String serverAddress, int port, String database) {
        this.username = username;
        this.password = password;
        this.serverAddress = serverAddress;
        this.port = port;
        this.databaseName = database;
    }

    /**
     * Connects to the database.
     */
    public void connect() {
        if (this.client != null)
            throw new IllegalStateException("Already connected");

        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder();
        if (this.connectionString == null) {
            if (!username.isEmpty() && !password.isEmpty()) {
                MongoCredential credential = MongoCredential.createCredential(this.username, DATABASE_NAME, password.toCharArray());
                settingsBuilder.credential(credential);
            }

            settingsBuilder.applyConnectionString(new ConnectionString(String.format(CONNECTION_URI_FORMAT, this.serverAddress, this.port)));
        } else {
            settingsBuilder.applyConnectionString(new ConnectionString(this.connectionString));
        }
        this.client = MongoClients.create(settingsBuilder.build());
        this.database = this.client.getDatabase(databaseName);
    }

    @Override
    public void close() {
        if (this.client == null)
            return;

        this.client.close();
        this.client = null;
    }
}

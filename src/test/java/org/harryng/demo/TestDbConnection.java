package org.harryng.demo;

import io.r2dbc.spi.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

//@SpringBootTest
@Slf4j
public class TestDbConnection {

    private ConnectionFactory getConnectionFactory() {
        ConnectionFactoryOptions options = builder()
                .option(DRIVER, "sqlserver")
                .option(HOST, "localhost")
                .option(PORT, 1433)  // optional, defaults to 1433
                .option(USER, "sa")
                .option(PASSWORD, "123456")
                .option(DATABASE, "test_db") // optional
                .option(SSL, false) // optional, defaults to false
                .option(Option.valueOf("applicationName"), "test_db1") // optional
                .option(Option.valueOf("preferCursoredExecution"), false) // optional
                .option(Option.valueOf("connectionId"), UUID.randomUUID()) // optional
                .build();
//        ConnectionFactory connectionFactory = ConnectionFactories.get("r2dbc:mssql://localhost:1433/test_db");
        return ConnectionFactories.get(options);
    }

    @Test
    public void select() throws InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        // Publisher<? extends Connection> connectionPublisher = connectionFactory.create();
        // Alternative: Creating a Mono using Project Reactor
        final Map<String, Object> statesMap = Collections.synchronizedMap(new LinkedHashMap<>());
        Mono.from(connectionFactory.create())
                .flatMap(connection -> {
                    statesMap.put("connection", connection);
                    return Mono.from(connection.setAutoCommit(false));
                }).flatMap(v -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    return Mono.from(connection.beginTransaction());
                }).flatMap(v -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    log.info("Creating the stmt...");
                    Statement statement = connection.createStatement("select * from user_");
                    return Mono.from(statement.execute());
                }).flatMapMany(result -> Flux.from(result.map((row, rowMetadata) -> {
                    log.info(String.format("Name[]: %s", row.get("name_")));
                    return row;
                }))).flatMap(v -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    return Flux.from(connection.commitTransaction());
                }).onErrorContinue((ex, obj) -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    Flux.from(connection.rollbackTransaction()).subscribe();
                }).doFinally(signalType -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    Flux.from(connection.close()).subscribe();
                }).subscribe(unused -> log.info("Start select..."));
//        connectionMono.flux().flatMap(result -> Flux.from(result.map((row, rowMetadata) -> {
//            log.info(String.format("Name[%s]: %s", "name_", row.get("name_")));
//            return row;
//        }))).subscribe(connection -> log.info("Start select..."));
        Thread.sleep(5_000);
    }

    @Test
    public void select1() throws InterruptedException {
        ConnectionFactory connectionFactory = getConnectionFactory();
        // Publisher<? extends Connection> connectionPublisher = connectionFactory.create();
        // Alternative: Creating a Mono using Project Reactor
        final Map<String, Object> statesMap = Collections.synchronizedMap(new LinkedHashMap<>());
        Mono.from(connectionFactory.create())
                .flatMap(connection -> {
                    statesMap.put("connection", connection);
                    log.info("Creating the stmt...");
                    Statement statement = connection.createStatement("select * from user_");
                    return Mono.from(statement.execute());
                }).flatMapMany(result -> Flux.from(result.map((row, rowMetadata) -> {
                    log.info(String.format("Name[]: %s", row.get("name_")));
                    return row;
                }))).doFinally(signalType -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    Flux.from(connection.close()).subscribe();
                }).subscribe(unused -> log.info("Start select..."));
        Thread.sleep(5_000);
    }

    @Test
    public void test() {
        Mono.just("1").flux().flatMap(itm -> {
            log.info("test:" + itm);
            return Flux.just(itm);
        }).subscribe(s -> log.info("Start test..."));
    }
}

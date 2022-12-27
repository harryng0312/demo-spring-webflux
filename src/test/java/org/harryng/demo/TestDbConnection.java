package org.harryng.demo;

import io.r2dbc.spi.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

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
                .option(Option.valueOf("applicationName"), "test_db") // optional
                .option(Option.valueOf("preferCursoredExecution"), false) // optional
                .option(Option.valueOf("connectionId"), UUID.randomUUID()) // optional
                .build();
//        ConnectionFactory connectionFactory = ConnectionFactories.get("r2dbc:mssql://localhost:1433/test_db");
        return ConnectionFactories.get(options);
    }

    @Test
    public void select() {
        ConnectionFactory connectionFactory = getConnectionFactory();
        // Publisher<? extends Connection> connectionPublisher = connectionFactory.create();
        // Alternative: Creating a Mono using Project Reactor
        final Map<String, Object> statesMap = Collections.synchronizedMap(new LinkedHashMap<>());
        Flux<Result> connectionFlux = Flux.from(connectionFactory.create())
                .flatMap(connection -> {
//                    statesMap.put("connection", connection);
//                    return Flux.from(connection.setAutoCommit(false));
//                }).flatMap(v -> {
//                    Connection connection = (Connection) statesMap.get("connection");
//                    return Flux.from(connection.beginTransaction());
//                }).flatMap(v -> {
//                    Connection connection = (Connection) statesMap.get("connection");
                    Statement statement = connection.createStatement("select * from user_");
                    return Flux.from(statement.execute());
//                }).flatMap(result -> Flux.from(result.map((row, rowMetadata) -> {
//                    log.info("Name[]: %s", row.get("name_"));
//                    return null;
//                }))).flatMap(v -> {
//                    Connection connection = (Connection) statesMap.get("connection");
//                    return Flux.from(connection.commitTransaction());
//                }).onErrorContinue((ex, obj) -> {
//                    Connection connection = (Connection) statesMap.get("connection");
//                    Flux.from(connection.rollbackTransaction()).subscribe();
//                }).doFinally(signalType -> {
//                    Connection connection = (Connection) statesMap.get("connection");
//                    Flux.from(connection.close()).subscribe();
                });
        connectionFlux.flatMapSequential(result -> Flux.from(result.map((row, rowMetadata) -> {
            log.info("Name[]: %s", row.get("name_"));
            return row;
        }))).subscribe(connection -> log.info("Start select..."), throwable -> {
        });
    }

    @Test
    public void test() {
        Flux.just("1").map(itm -> {
            log.info("test:" + itm);
            return itm;
        }).subscribe(s -> log.info("Start test..."), throwable -> {
        });
    }
}

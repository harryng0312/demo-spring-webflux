package org.harryng.demo;

import io.r2dbc.spi.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@SpringBootTest
@Slf4j
public class TestDbConnectionPool {

    @Resource
    private ConnectionFactory connectionFactory;

    @Test
    public void select() throws InterruptedException {
        // Publisher<? extends Connection> connectionPublisher = connectionFactory.create();
        // Alternative: Creating a Mono using Project Reactor
        final Map<String, Object> statesMap = Collections.synchronizedMap(new LinkedHashMap<>());
        Mono.from(connectionFactory.create())
                .flatMap(connection -> {
                    log.info("Starting query ...");
                    statesMap.put("connection", connection);
                    return Mono.from(connection.setAutoCommit(false)).thenReturn(connection);
                }).flatMap(connection -> {
                    log.info("Begin trans ...");
                    return Mono.from(connection.beginTransaction()).thenReturn(connection);
                }).flatMap(connection -> {
                    log.info("Creating the stmt...");
                    Statement statement = connection.createStatement("select * from user_");
                    return Mono.from(statement.execute());
                })
                .onErrorResume(throwable -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    log.error("Rolling back first ...", throwable);
                    return Mono.from(connection.rollbackTransaction()).flatMap(unused -> Mono.empty());
                })
                .flatMapMany(result -> Flux.from(result.map((row, rowMetadata) -> {
                    log.info(String.format("Name[]: %s", row.get("name_")));
                    return row;
                })))
                .collectList()
//                .map(rows -> {
//                    rows.forEach(row -> log.info(String.format("List: Name[]: %s", row.get("name_"))));
//                    return rows;
//                })
                .flatMap(v -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    log.info("Committing ...");
                    return Mono.from(connection.commitTransaction()).thenReturn(1);
                }).onErrorResume((ex) -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    log.info("Rolling back ...");
                    return Mono.from(connection.rollbackTransaction()).thenReturn(0);
                })
                .doOnSuccess(integer -> {})
                .flatMap(s -> {
                    log.info("Closed conn ...");
                    Connection connection = (Connection) statesMap.get("connection");
                    return Mono.from(connection.close()).then();
                })
                .block();
//                .subscribe(unused -> log.info("Start select..."));
//        Thread.sleep(5_000);
    }

    @Test
    public void select1() throws InterruptedException {
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
                }))).collectList().map(rows -> rows).doFinally(signalType -> {
                    Connection connection = (Connection) statesMap.get("connection");
                    Mono.from(connection.close());
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

package org.harryng.demo;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
@Slf4j
public class TestDbTransactionManager {

    @Resource
    private DatabaseClient dbClient;

    @Resource
    private ReactiveTransactionManager reactiveTransactionManager;

    @Test
    public void testSelectInTrans() {
        final Map<String, Object> statusMap = Collections.synchronizedMap(new LinkedHashMap<>());
        TransactionalOperator transactionalOperator = TransactionalOperator.create(reactiveTransactionManager);
        dbClient.sql("select * from user_")
                .fetch().all()
                .collectList()
                .map(maps -> {
                    maps.forEach(resultMap -> {
                        final StringBuilder rsBuilder = new StringBuilder();
                        resultMap.forEach((s, o) -> rsBuilder.append("[").append(s).append("]:").append(o).append("\n"));
                        log.info("-----\n{}", rsBuilder);
                    });
                    return maps;
                })
                .as(transactionalOperator::transactional)
                .block();
    }

}

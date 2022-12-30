package org.harryng.demo.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Duration;
import java.util.UUID;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;
import static io.r2dbc.spi.ConnectionFactoryOptions.SSL;

@Configuration
@EnableR2dbcRepositories
public class DbConfig {

    //    @Bean
    public ConnectionFactory getConnectionFactory() {
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

    @Bean
    public ConnectionPool getConnectionPool() {
        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(getConnectionFactory())
                .initialSize(5)
                .maxSize(10)
                .maxIdleTime(Duration.ofMinutes(5)).build();
        return new ConnectionPool(poolConfiguration);
    }

    //    @Bean
    public ReactiveTransactionManager getTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}

package org.harryng.demo.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;

import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableR2dbcRepositories
public class DbConfig {

    //    @Bean
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "sqlserver")
                .option(ConnectionFactoryOptions.HOST, "localhost")
                .option(ConnectionFactoryOptions.PORT, 1433)  // optional, defaults to 1433
                .option(ConnectionFactoryOptions.USER, "sa")
                .option(ConnectionFactoryOptions.PASSWORD, "123456")
                .option(ConnectionFactoryOptions.DATABASE, "test_db") // optional
                .option(ConnectionFactoryOptions.SSL, false) // optional, defaults to false
                .option(Option.valueOf("applicationName"), "test_db1") // optional
                .option(Option.valueOf("preferCursoredExecution"), false) // optional
                .option(Option.valueOf("connectionId"), UUID.randomUUID()) // optional
                .build();
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

    @Bean
    public DatabaseClient getDatabaseClient(){
        return DatabaseClient.create(getConnectionPool());
    }

    //    @Bean
    public ReactiveTransactionManager getTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}

package org.kikidis;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
public class SqlConfig {

    @Bean
    public Connection conn(DataSource db) throws SQLException {
        return db.getConnection();
    }

    @Bean
    public DataSource sqlDataSource(Environment env) {
        MysqlDataSource db = new MysqlDataSource();
        db.setURL(env.getProperty("db"));
        return db;
    }
}

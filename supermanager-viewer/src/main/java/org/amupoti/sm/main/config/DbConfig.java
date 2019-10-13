package org.amupoti.sm.main.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Marcel on 01/11/2015.
 */
@Configuration
public class DbConfig {

    private static final Log LOG = LogFactory.getLog(DbConfig.class);

    @Bean
    public DataSource dataSource() throws URISyntaxException {

        //TODO: set login/pass + url into properties file
        URI dbUri;
        String username = "postgres";
        String password = "admin";
        String url = "jdbc:postgresql://localhost/postgres";
        String dbProperty = System.getenv("DATABASE_URL");

        if (dbProperty != null) {
            dbUri = new URI(dbProperty);

            username = dbUri.getUserInfo().split(":")[0];
            password = dbUri.getUserInfo().split(":")[1];
            url = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
        }

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);

        LOG.info("Creating datasource trying to connect to URL: " + url + " with username: " + username + " and password " + password);

        return dataSourceBuilder.build();

    }
}
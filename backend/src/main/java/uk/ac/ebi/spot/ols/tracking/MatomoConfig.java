package uk.ac.ebi.spot.ols.tracking;

import org.matomo.java.tracking.MatomoTracker;
import org.matomo.java.tracking.TrackerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

import java.net.URI;

@Configuration
public class MatomoConfig {

    @Value("${matomo.auth-token}")
    private String authToken;

    @Bean
    public MatomoTracker matomoTracker() {
        TrackerConfiguration configuration = TrackerConfiguration.builder()
                .apiEndpoint(URI.create("https://matomo.zbmed.de/matomo.php"))
                .defaultSiteId(16)
                .defaultAuthToken(authToken)
                .build();
        return new MatomoTracker(configuration);
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }
}
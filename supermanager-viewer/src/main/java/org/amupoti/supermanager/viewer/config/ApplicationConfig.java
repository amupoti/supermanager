package org.amupoti.supermanager.viewer.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.amupoti.supermanager.acb.application.port.in.BuyPlayerUseCase;
import org.amupoti.supermanager.acb.application.port.in.CancelAllChangesUseCase;
import org.amupoti.supermanager.acb.application.port.in.LoadUserTeamsUseCase;
import org.amupoti.supermanager.acb.application.port.in.SellPlayerUseCase;
import org.amupoti.supermanager.acb.application.port.in.UndoChangeUseCase;
import org.amupoti.supermanager.acb.application.port.out.AuthenticationPort;
import org.amupoti.supermanager.acb.application.port.out.MarketDataPort;
import org.amupoti.supermanager.acb.application.port.out.PlayerChangePort;
import org.amupoti.supermanager.acb.application.port.out.PlayerStatsPort;
import org.amupoti.supermanager.acb.application.port.out.TeamDataPort;
import org.amupoti.supermanager.acb.application.service.BuyPlayerService;
import org.amupoti.supermanager.acb.application.service.CancelAllChangesService;
import org.amupoti.supermanager.acb.application.service.ComputeTeamStatsService;
import org.amupoti.supermanager.acb.application.service.FindCandidateService;
import org.amupoti.supermanager.acb.application.service.LoadUserTeamsService;
import org.amupoti.supermanager.acb.application.service.SellPlayerService;
import org.amupoti.supermanager.acb.application.service.UndoChangeService;
import org.amupoti.supermanager.rdm.application.port.in.GetAllSchedulesUseCase;
import org.amupoti.supermanager.rdm.application.port.in.GetTeamScheduleUseCase;
import org.amupoti.supermanager.rdm.application.port.out.ScheduleScrapingPort;
import org.amupoti.supermanager.rdm.application.service.TeamScheduleService;
import org.amupoti.supermanager.viewer.application.port.in.ViewCalendarUseCase;
import org.amupoti.supermanager.viewer.application.port.in.ViewPrivateLeagueUseCase;
import org.amupoti.supermanager.viewer.application.service.PrivateLeagueService;
import org.amupoti.supermanager.viewer.application.service.ViewCalendarService;
import org.amupoti.supermanager.viewer.application.port.in.ViewUserTeamsUseCase;
import org.amupoti.supermanager.viewer.application.service.ViewUserTeamsService;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Composition root — wires all application use cases and infrastructure adapters.
 */
@Configuration
@PropertySource("classpath:urls.properties")
@ComponentScan({
    "org.amupoti.supermanager.acb.adapter.out",
    "org.amupoti.supermanager.rdm.adapter.out",
    "org.amupoti.supermanager.viewer.adapter.out",
    "org.amupoti.supermanager.viewer.adapter.in"
})
@Slf4j
public class ApplicationConfig {

    @Value("${http.connect-timeout-ms:30000}")
    private int connectTimeoutMs;

    @Value("${http.request-timeout-ms:30000}")
    private int requestTimeoutMs;

    @Value("${http.socket-timeout-ms:60000}")
    private int socketTimeoutMs;

    @Value("${scraping.next-matches:5}")
    private int nextMatches;

    @Value("${private.league.teams:}")
    private String privateLeagueTeams;

    @Autowired
    CloseableHttpClient httpClient;

    @Bean
    public PropertySourcesPlaceholderConfigurer getPropertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                caffeineCache("TeamData",    Duration.ofHours(25)),
                caffeineCache("NextMatch",   Duration.ofHours(6)),
                caffeineCache("RdmTeamData", Duration.ofHours(25)),
                caffeineCache("teamsPage",   Duration.ofMinutes(30)),
                caffeineCache("marketPage",  Duration.ofMinutes(30)),
                caffeineCache("playerStats", Duration.ofHours(6))
        ));
        return manager;
    }

    private CaffeineCache caffeineCache(String name, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .build());
    }

    @Bean(name = "acbFetchExecutor")
    public ExecutorService acbFetchExecutor() {
        return Executors.newFixedThreadPool(20);
    }

    @Bean(name = "rdmFetchExecutor")
    public ExecutorService rdmFetchExecutor() {
        return Executors.newFixedThreadPool(3);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(clientHttpRequestFactory());
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient());
        return factory;
    }

    @Bean
    public CloseableHttpClient httpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(requestTimeoutMs))
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                .setResponseTimeout(Timeout.ofMilliseconds(socketTimeoutMs))
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.ZERO_MILLISECONDS) {
                    @Override
                    public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
                        if (exception instanceof org.apache.hc.core5.http.NoHttpResponseException) {
                            log.warn("No response from server on {} call", execCount);
                            return execCount <= 3;
                        }
                        if (execCount > 3) {
                            log.warn("Maximum tries reached for client http pool");
                        }
                        return false;
                    }
                })
                .build();
    }

    // --- RDM application services (wired explicitly; no @Service annotation) ---

    @Bean
    public TeamScheduleService teamScheduleService(ScheduleScrapingPort scrapingPort) {
        return new TeamScheduleService(scrapingPort);
    }

    // --- ACB application services ---

    @Bean
    public ComputeTeamStatsService computeTeamStatsService() {
        return new ComputeTeamStatsService();
    }

    @Bean
    public FindCandidateService findCandidateService() {
        return new FindCandidateService();
    }

    @Bean
    public LoadUserTeamsUseCase loadUserTeamsUseCase(AuthenticationPort authPort,
                                                      TeamDataPort teamDataPort,
                                                      MarketDataPort marketDataPort,
                                                      PlayerStatsPort playerStatsPort) {
        return new LoadUserTeamsService(authPort, teamDataPort, marketDataPort, playerStatsPort,
                computeTeamStatsService(), findCandidateService(), acbFetchExecutor());
    }

    @Bean
    public BuyPlayerUseCase buyPlayerUseCase(PlayerChangePort playerChangePort) {
        return new BuyPlayerService(playerChangePort);
    }

    @Bean
    public SellPlayerUseCase sellPlayerUseCase(PlayerChangePort playerChangePort) {
        return new SellPlayerService(playerChangePort);
    }

    @Bean
    public UndoChangeUseCase undoChangeUseCase(PlayerChangePort playerChangePort) {
        return new UndoChangeService(playerChangePort);
    }

    @Bean
    public CancelAllChangesUseCase cancelAllChangesUseCase(PlayerChangePort playerChangePort) {
        return new CancelAllChangesService(playerChangePort);
    }

    // --- Viewer application services ---

    @Bean
    public ViewPrivateLeagueUseCase viewPrivateLeagueUseCase(
            @Value("${private.league.teams:}") String privateLeagueTeams) {
        return new PrivateLeagueService(privateLeagueTeams);
    }

    @Bean
    public ViewCalendarUseCase viewCalendarUseCase(TeamScheduleService scheduleService) {
        return new ViewCalendarService(scheduleService, scheduleService);
    }

    @Bean
    public ViewUserTeamsUseCase viewUserTeamsUseCase(LoadUserTeamsUseCase loadUserTeamsUseCase,
                                                      TeamScheduleService scheduleService) {
        return new ViewUserTeamsService(loadUserTeamsUseCase, scheduleService, nextMatches);
    }
}

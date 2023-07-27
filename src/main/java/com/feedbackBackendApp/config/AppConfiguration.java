package com.feedbackBackendApp.config;

import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import com.feedbackBackendApp.services.DummySessionStore;

@Configuration
@EnableAsync
public class AppConfiguration {
	
	@Value("${spring.datasource1.url}")
	private String url;
	
	@Value("${spring.datasource1.username}")
	private String username;
	
	@Value("${spring.datasource1.password}")
	private String dbPassword;


	@Value("${spring.datasource2.url}")
	private String url2;
	
	@Value("${spring.datasource2.username}")
	private String username2;
	
	@Value("${spring.datasource2.password}")
	private String dbPassword2;
	

	@Autowired
	DummySessionStore boradcast;

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
	
    @Bean(name = "sseEmitterExecutorService",destroyMethod = "shutdown")
    public Executor threadPoolTaskExecutor() {
    	ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    	taskExecutor.setMaxPoolSize(4);
    	taskExecutor.setThreadNamePrefix("-[THREAD_SSE]-");
        return taskExecutor;
    }
    
    @Primary
    @Bean("dbsource")
    public DataSource dbsource() {
    	DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    	dataSourceBuilder.username(username);
    	dataSourceBuilder.url(url);
    	dataSourceBuilder.password(dbPassword);
    	return dataSourceBuilder.build();
    }
    
    
    @Bean("dbsource2")
    public DataSource dbsource2() {
    	DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
    	dataSourceBuilder.username(username2);
    	dataSourceBuilder.url(url2);
    	dataSourceBuilder.password(dbPassword2);
    	return dataSourceBuilder.build();
    }
	

	@EventListener(ContextClosedEvent.class)
	public void clearAllSseEmitters() {
		try {
			boradcast.sseStore.forEach((session,emitter)->{
			emitter.complete();
		});}
		catch(NullPointerException nullPointerException) {
		}
	}
	
}

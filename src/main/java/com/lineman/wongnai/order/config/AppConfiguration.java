package com.lineman.wongnai.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
@EnableMongoAuditing
public class AppConfiguration {

	
	@Bean
    public ObjectMapper objectMapper() {

        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.findAndRegisterModules();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper;
    }
	
	/*@Bean
	public CorsConfigurationSource corsConfigurationSource(ServiceCorsConfiguration serviceCorsConfiguration) {
	   
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(serviceCorsConfiguration.getOrigins());
	    config.setAllowedMethods(serviceCorsConfiguration.getMethods());
	    config.setAllowedHeaders(serviceCorsConfiguration.getHeaders());
	  
	    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);

	    return source;
	}*/

	/*@Bean("taskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Async-");
        return executor;
    }*/
	
}

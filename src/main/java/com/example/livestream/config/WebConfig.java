package com.example.livestream.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

		public void addCorsMapping(CorsRegistry registry) {
			registry.addMapping("/api/**")
			        .allowedOrigins(
			        		"http://localhost:8080",
                				"http://[ip]:8080"
			        )
			        .allowedMethods("GET", "POST", "PUT", "DELETE")
			        .allowCredentials(true);
		}
}

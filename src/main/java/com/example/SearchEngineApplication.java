package com.example;


import com.example.page.UrlProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(UrlProperties.class)
public class SearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(SearchEngineApplication.class, args);
	}

}

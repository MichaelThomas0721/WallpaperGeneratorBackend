package com.thomas.WallpaperGenerator;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WallpaperGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(WallpaperGeneratorApplication.class);
        app.setDefaultProperties(Collections
          .singletonMap("server.port", "4269"));
        app.run(args);
	}

}

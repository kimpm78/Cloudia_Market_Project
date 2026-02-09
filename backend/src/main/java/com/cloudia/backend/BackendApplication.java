package com.cloudia.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
	// .envファイルを読み込み
	Dotenv dotenv = Dotenv.configure()
					.directory("src/main/resources/")
					.ignoreIfMissing()
					.load();

		if (dotenv != null) {
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
		}

		SpringApplication.run(BackendApplication.class, args);
	}
}
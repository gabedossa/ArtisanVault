package com.dossa.ArtisanVault.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.dossa.ArtisanVault.project")
public class ArtisanVaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArtisanVaultApplication.class, args);
	}

}

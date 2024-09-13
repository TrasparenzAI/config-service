package it.cnr.anac.transparency.config_server;

import it.cnr.anac.transparency.configserver.ConfigServiceApplication;
import org.springframework.boot.SpringApplication;

public class TestConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.from(ConfigServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

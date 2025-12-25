package vn.web.fashionshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FashionshopApplication {

	public static void main(String[] args) {
		SpringApplication.run(FashionshopApplication.class, args);
	}

}

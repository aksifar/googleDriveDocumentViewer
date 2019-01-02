package com.aksifar.documentviewer.gdrive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
@EnableSwagger2
@SpringBootApplication
public class GdriveApplication {

	public static void main(String[] args) {
		SpringApplication.run(GdriveApplication.class, args);
	}
	
	@Bean
    public Docket docket()
    {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage(getClass().getPackage().getName()))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(generateApiInfo());
    }


    @SuppressWarnings("deprecation")
	private ApiInfo generateApiInfo()
    {
        return new ApiInfo("Google Drive Document Viewer", "This service can be used to view files and folders in google drive after Authentication", "Version 0.1",
            "urn:tos", "ankit_rch@yahoo.co.in", "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0");
    }
}


package com.stc.inspireu.configs;

import static springfox.documentation.builders.PathSelectors.regex;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@PropertySource("classpath:custom-swagger.properties")
public class CustomSwaggerConfig {

	@Value("${api.version}")
	private String apiVersion;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Bean
	public Docket mainApi() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("Main").select()
				.apis(RequestHandlerSelectors.any()).paths(regex("/api/" + apiVersion + ".*"))
				.paths(regex("/api/" + apiVersion + "/publicForms.*").negate()).build().apiInfo(metaData())
				.forCodeGeneration(true).genericModelSubstitutes(ResponseEntity.class)
				.ignoredParameterTypes(Pageable.class).ignoredParameterTypes(java.sql.Date.class)
				.directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
				.directModelSubstitute(java.time.LocalDateTime.class, Date.class)
				.securityContexts(Arrays.asList(securityContext())).securitySchemes(Arrays.asList(apiKey()))
				.useDefaultResponseMessages(false);

		return docket;
	}

	@Bean
	public Docket otherApi() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("Other").select()
				.apis(RequestHandlerSelectors.any()).paths(regex("/private.*")).build().apiInfo(metaData())
				.forCodeGeneration(true).genericModelSubstitutes(ResponseEntity.class)
				.ignoredParameterTypes(Pageable.class).ignoredParameterTypes(java.sql.Date.class)
				.directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
				.directModelSubstitute(java.time.LocalDateTime.class, Date.class)
				.useDefaultResponseMessages(false);

		return docket;
	}

	@Bean
	public Docket publicFormApi() {
		Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("PublicForm").select()
				.apis(RequestHandlerSelectors.any()).paths(regex("/api/" + apiVersion + "/publicForms.*")).build()
				.apiInfo(metaData()).forCodeGeneration(true).genericModelSubstitutes(ResponseEntity.class)
				.ignoredParameterTypes(Pageable.class).ignoredParameterTypes(java.sql.Date.class)
				.directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
				.directModelSubstitute(java.time.LocalDateTime.class, Date.class)
				.useDefaultResponseMessages(false);

		return docket;
	}

	private ApiInfo metaData() {
		return new ApiInfoBuilder().title("STC InspireU API doc").description("rest api for stc inspireu")
				.version(apiVersion).license("").licenseUrl("").contact(new Contact("", "", "")).build();
	}

	private ApiKey apiKey() {
		return new ApiKey("JWT", "Authorization", "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder().securityReferences(defaultAuth())
				.forPaths(PathSelectors.regex("/api/" + apiVersion + ".*")).build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Arrays.asList(new SecurityReference("JWT", authorizationScopes));
	}

}

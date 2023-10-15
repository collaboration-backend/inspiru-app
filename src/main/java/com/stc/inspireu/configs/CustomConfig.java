package com.stc.inspireu.configs;

import java.lang.invoke.MethodHandles;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.catalina.Context;
import org.apache.tomcat.util.http.LegacyCookieProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.stc.inspireu.services.CommonService;
import com.stc.inspireu.services.PushNotificationService;

import io.minio.MinioClient;
import okhttp3.OkHttpClient;

@Configuration
@EnableScheduling
public class CustomConfig implements WebMvcConfigurer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Autowired
	private Environment env;

	@Autowired
	private AuthenticationFilter authenticationFilter;

	@Autowired
	private AssetFilter assetFilter;

	@Autowired
	private CommonService commonService;

	@Autowired
	private AuthorizationInterceptor authorizationInterceptor;

	@Autowired
	private SwaggerCustomFilter swaggerCssFilter;

	@Autowired
	private PushNotificationService pushNotificationService;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/websocket/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

	@Bean(name = "asyncExecutor")
	@Primary
	public TaskExecutor workExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("Async-");
		threadPoolTaskExecutor.setCorePoolSize(3);
		threadPoolTaskExecutor.setMaxPoolSize(3);
		threadPoolTaskExecutor.setQueueCapacity(600);
		threadPoolTaskExecutor.afterPropertiesSet();
		LOGGER.info("ThreadPoolTaskExecutor set");
		return threadPoolTaskExecutor;
	}

	@Bean
	public FilterRegistrationBean<SwaggerCustomFilter> customSwaggerCssRegistration() {
		FilterRegistrationBean<SwaggerCustomFilter> registration = new FilterRegistrationBean<SwaggerCustomFilter>();
		registration.setFilter(swaggerCssFilter);
		registration.addUrlPatterns("/swagger-ui/*");
		registration.setName("SwaggerCss");
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<AssetFilter> customAssetFilterRegistration() {
		FilterRegistrationBean<AssetFilter> registration = new FilterRegistrationBean<AssetFilter>();
		registration.setFilter(assetFilter);
		registration.addUrlPatterns("/private/*");
		registration.setName("AssetFilter");
		registration.setOrder(2);
		return registration;
	}

	@Bean
	public FilterRegistrationBean<AuthenticationFilter> customAuthenticationFilterRegistration() {
		FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<AuthenticationFilter>();
		registration.setFilter(authenticationFilter);
		registration.addUrlPatterns("/api/*");
		registration.addInitParameter("paramName", "paramValue");
		registration.setName("AuthenticationFilter");
		registration.setOrder(4);
		return registration;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authorizationInterceptor).addPathPatterns("/api/**");
	}

	@Bean
	public MinioClient minioClient() {

		MinioClient minioClient = MinioClient.builder().endpoint(env.getProperty("minio.endpoint"))
				.credentials(env.getProperty("minio.accessKey"), env.getProperty("minio.secretKey"))
				.httpClient(getUnsafeOkHttpClient()).build();

		return minioClient;
	}

	private OkHttpClient getUnsafeOkHttpClient() {
		try {
			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[] {};
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});

			OkHttpClient okHttpClient = builder.build();
			return okHttpClient;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
		return new WebServerFactoryCustomizer<TomcatServletWebServerFactory>() {
			@Override
			public void customize(TomcatServletWebServerFactory tomcatServletWebServerFactory) {
				tomcatServletWebServerFactory.addContextCustomizers(new TomcatContextCustomizer() {
					@Override
					public void customize(Context context) {
						context.setCookieProcessor(new LegacyCookieProcessor());
					}
				});
			}
		};
	}

	@Async("asyncExecutor")
	@Scheduled(fixedDelay = 300000, initialDelay = 10000)
	public void fixed5Min() {
		LOGGER.debug("fixed5Min " + System.currentTimeMillis() / 1000);
		pushNotificationService.killDeadSSE();
	}

	@Async("asyncExecutor")
	@Scheduled(cron = "0 0 0 * * *", zone = "UTC")
	public void dailyMidnight() {
		LOGGER.debug("dailyMidnight " + System.currentTimeMillis() / 1000);
		commonService.dailyMidnight();
	}

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setCacheSeconds(10); //reload messages every 10 seconds
        return messageSource;
    }

}

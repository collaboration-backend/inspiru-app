package com.stc.inspireu.utils;

import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.model.KeyStoreSettings;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class SamlSettings {

	@Autowired
	private Environment environment;

	@Autowired
	private ResourceLoader resourceLoader;

	public Map<String, Object> getSamlSettings()
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, Error {

		String samlKeystoreLocation = environment.getProperty("saml.keystore.location");
		String samlKeystorePassword = environment.getProperty("saml.keystore.password");
		String samlKeystoreAlias = environment.getProperty("saml.keystore.alias");
		String samlConfigFileName = environment.getProperty("samlConfigFileName");

		SettingsBuilder builder = new SettingsBuilder();

		Saml2Settings settings;

		Resource resource = resourceLoader.getResource("classpath:" + samlConfigFileName);

		Properties properties = new Properties();

		properties.load(resource.getInputStream());

		Map<String, Object> samlProperties = new HashMap<String, Object>();

		for (final String name : properties.stringPropertyNames()) {
			samlProperties.put(name, properties.getProperty(name));
		}

		if (environment instanceof ConfigurableEnvironment) {
			for (PropertySource<?> propertySource : ((ConfigurableEnvironment) environment).getPropertySources()) {
				if (propertySource instanceof EnumerablePropertySource) {
					for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
						if (key.startsWith("onelogin.")) {
							samlProperties.put(key, (String) propertySource.getProperty(key));
						}
					}
				}
			}
		}

		if (samlKeystoreLocation != null) {
			DefaultResourceLoader loader = new DefaultResourceLoader();

			Resource storeFile = loader.getResource(samlKeystoreLocation);

			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

			keystore.load(storeFile.getInputStream(), samlKeystorePassword.toCharArray());

			KeyStoreSettings keyStoreSettings = new KeyStoreSettings(keystore, samlKeystoreAlias, samlKeystorePassword);

//            settings = builder.fromFile(samlConfigFileName, keyStoreSettings).build();
			settings = builder.fromValues(samlProperties, keyStoreSettings).build();

		} else {
//            settings = builder.fromFile(samlConfigFileName).build();
			settings = builder.fromValues(samlProperties).build();
		}

		Map<String, Object> ss = new HashMap<String, Object>();

		ss.put("settings", settings);
		ss.put("originalUrl",
				samlProperties.containsKey("onelogin.saml2.sp.assertion_consumer_service.url")
						? samlProperties.get("onelogin.saml2.sp.assertion_consumer_service.url")
						: null);

		return ss;

	}

}

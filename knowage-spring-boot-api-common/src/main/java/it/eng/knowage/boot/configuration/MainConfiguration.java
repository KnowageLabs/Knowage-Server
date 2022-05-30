package it.eng.knowage.boot.configuration;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateRequestCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.orm.jpa.LocalEntityManagerFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.factory.SecurityServiceFactory;
import it.eng.knowage.boot.utils.ConfigSingleton;

@Configuration
@Profile("production")
@ComponentScan({"it.eng.knowage.boot"})
public class MainConfiguration {

	@Bean("knowage-config")
	public LocalEntityManagerFactoryBean entityManagerFactoryForWidgetConfig() {
		LocalEntityManagerFactoryBean factoryBean = new LocalEntityManagerFactoryBean();
		factoryBean.setPersistenceUnitName("knowage-config");
		return factoryBean;
	}

	@Lazy
	@Bean
	public SecurityServiceFactory securityService() {
		return new SecurityServiceFactory();
	}

	@Bean
	@Scope(scopeName = "thread", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public BusinessRequestContext businessRequestContext(@Value("${application.version}") String version) {
		return new BusinessRequestContext(version);
	}

	@Bean
	public static BeanFactoryPostProcessor beanFactoryPostProcessor() {
		return new BeanFactoryPostProcessor() {
			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				beanFactory.registerScope("thread", new SimpleThreadScope());
			}
		};
	}

	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
		multipartResolver.setMaxUploadSize(100000);
		return multipartResolver;
	}

	@Bean
	public Context context() throws NamingException {
		return new InitialContext();
	}

	/**
	 * REST template for BE2BE call using the user token from {@link BusinessRequestContext}.
	 */
	@Bean
	@Qualifier("perUserBe2Be")
	public RestTemplate perUserBe2BeRestTemplate(RestTemplateBuilder builder, Context context, PerUserBe2BeRequestCustomizer customizer) throws NamingException, MalformedURLException {

		String serviceUrl = (String) context.lookup("java:comp/env/service_url");

		URL serviceUrlAsURL = new URL(serviceUrl);
		serviceUrlAsURL = new URL(serviceUrlAsURL.getProtocol(),
				serviceUrlAsURL.getHost(),
				serviceUrlAsURL.getPort(),
				"",
				null);

		RestTemplate template = builder.rootUri(serviceUrlAsURL.toString())
			.additionalRequestCustomizers(customizer)
			.requestFactory(() -> clientHttpRequestFactoryWithoutCookies())
			.build();

		return template;
	}

	public ClientHttpRequestFactory clientHttpRequestFactoryWithoutCookies() {
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

		factory.setConnectTimeout(5000);
		factory.setReadTimeout(10000);

		HttpClient httpClient = HttpClientBuilder.create()
				.disableCookieManagement()
				.build();
		factory.setHttpClient(httpClient);

		return factory;
	}

}

@Component
class PerUserBe2BeRequestCustomizer implements RestTemplateRequestCustomizer<ClientHttpRequest> {

	private static final Logger LOGGER = LogManager.getLogger(PerUserBe2BeRequestCustomizer.class);

	@Autowired
	private BusinessRequestContext brc;

	@Override
	public void customize(ClientHttpRequest request) {
		String authorizationHeaderName = ConfigSingleton.getInstance().getAuthorizationHeaderName();
		String userToken = brc.getUserToken();
		String correlationId = brc.getCorrelationId().toString();
		HttpHeaders headers = request.getHeaders();

		LOGGER.debug("Customize BE2BE call using " + authorizationHeaderName + " header with value " + userToken);

		headers.add(authorizationHeaderName, userToken);
		headers.add("X-Kn-Correlation-Id", correlationId);

		// TODO : Add kn.lang cookie
	}

}

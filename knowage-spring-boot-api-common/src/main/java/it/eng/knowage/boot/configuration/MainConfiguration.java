package it.eng.knowage.boot.configuration;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
			.build();

		return template;
	}

}

@Component
class PerUserBe2BeRequestCustomizer implements RestTemplateRequestCustomizer<ClientHttpRequest> {

	@Autowired
	private BusinessRequestContext brc;

	@Override
	public void customize(ClientHttpRequest request) {
		String authorizationHeaderName = ConfigSingleton.getInstance().getAuthorizationHeaderName();
		String userToken = brc.getUserToken();
		HttpHeaders headers = request.getHeaders();

		headers.add(authorizationHeaderName, userToken);

		// TODO : Add kn.lang cookie
	}

}

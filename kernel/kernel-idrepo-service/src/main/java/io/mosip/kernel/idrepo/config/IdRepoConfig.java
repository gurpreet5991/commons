package io.mosip.kernel.idrepo.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.zaxxer.hikari.HikariDataSource;

import io.mosip.kernel.core.idrepo.constant.IdRepoConstants;
import io.mosip.kernel.core.idrepo.spi.ShardDataSourceResolver;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * The Class IdRepoConfig.
 *
 * @author Manoj SP
 */
@Configuration
@ConfigurationProperties("mosip.kernel.idrepo")
@EnableTransactionManagement
public class IdRepoConfig implements WebMvcConfigurer {

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(IdRepoConfig.class);

	/** The env. */
	@Autowired
	private Environment env;

	/** The interceptor. */
	@Autowired
	private Interceptor interceptor;

	/** The db. */
	private Map<String, Map<String, String>> db;

	/** The status. */
	private List<String> status;

	/** The allowed bio types. */
	private List<String> allowedBioAttributes;
	
	private List<String> bioAttributes;

	private List<String> allowedTypes;

	/** The id. */
	private Map<String, String> id;

	/**
	 * Gets the db.
	 *
	 * @return the db
	 */
	public Map<String, Map<String, String>> getDb() {
		return db;
	}

	/**
	 * Sets the db.
	 *
	 * @param db
	 *            the db
	 */
	public void setDb(Map<String, Map<String, String>> db) {
		this.db = db;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public List<String> getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status
	 *            the status
	 */
	public void setStatus(List<String> status) {
		this.status = status;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Map<String, String> getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id
	 *            the id
	 */
	public void setId(Map<String, String> id) {
		this.id = id;
	}

	/**
	 * Gets the allowed bio types.
	 *
	 * @return the allowed bio types
	 */
	public List<String> getAllowedBioAttributes() {
		return allowedBioAttributes;
	}

	/**
	 * Sets the allowed bio types.
	 *
	 * @param allowedBioTypes
	 *            the new allowed bio types
	 */
	public void setAllowedBioAttributes(List<String> allowedBioAttributes) {
		this.allowedBioAttributes = allowedBioAttributes;
	}

	public List<String> getBioAttributes() {
		return bioAttributes;
	}

	public void setBioAttributes(List<String> bioAttributes) {
		this.bioAttributes = bioAttributes;
	}

	public List<String> getAllowedTypes() {
		return allowedTypes;
	}

	public void setAllowedTypes(List<String> allowedTypes) {
		this.allowedTypes = allowedTypes;
	}

	/**
	 * Setup.
	 */
	@PostConstruct
	public void setup() {
		status.add(env.getProperty(IdRepoConstants.ACTIVE_STATUS.getValue()));
	}

	/**
	 * Gets the shard data source resolver.
	 *
	 * @return the shard data source resolver
	 */
	@Bean
	public ShardDataSourceResolver getShardDataSourceResolver() {
		ShardDataSourceResolver resolver = new ShardDataSourceResolver();
		resolver.setLenientFallback(false);
		resolver.setTargetDataSources(db.entrySet().parallelStream()
				.collect(Collectors.toMap(Map.Entry::getKey, value -> buildDataSource(value.getValue()))));
		return resolver;
	}

	/**
	 * Id.
	 *
	 * @return the map
	 */
	@Bean
	public Map<String, String> id() {
		return Collections.unmodifiableMap(id);
	}

	/**
	 * Allowed bio types.
	 *
	 * @return the list
	 */
	@Bean
	public List<String> allowedBioAttributes() {
		return Collections.unmodifiableList(allowedBioAttributes);
	}
	
	@Bean
	public List<String> bioAttributes() {
		return Collections.unmodifiableList(bioAttributes);
	}

	@Bean
	public List<String> allowedTypes() {
		return Collections.unmodifiableList(allowedTypes);
	}

	/**
	 * Status.
	 *
	 * @return the map
	 */
	@Bean
	public List<String> status() {
		return Collections.unmodifiableList(status);
	}
	
	/**
	 * Entity manager factory.
	 *
	 * @param dataSource
	 *            the data source
	 * @return the local container entity manager factory bean
	 */
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource);
		em.setPackagesToScan("io.mosip.kernel.idrepo.*");

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		em.setJpaPropertyMap(additionalProperties());

		return em;
	}

	/**
	 * Transaction manager.
	 *
	 * @param emf
	 *            the emf
	 * @return the platform transaction manager
	 */
	@Bean
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}

	/**
	 * Additional properties.
	 *
	 * @return the properties
	 */
	private Map<String, Object> additionalProperties() {
		Map<String, Object> jpaProperties = new HashMap<>();
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL92Dialect");
		jpaProperties.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
		jpaProperties.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
		jpaProperties.put("hibernate.ejb.interceptor", interceptor);

		return jpaProperties;
	}

	/**
	 * Builds the data source.
	 *
	 * @param dataSourceValues
	 *            the data source values
	 * @return the data source
	 */
	private DataSource buildDataSource(Map<String, String> dataSourceValues) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(dataSourceValues.get("url"));
		dataSource.setUsername(dataSourceValues.get("username"));
		dataSource.setPassword(dataSourceValues.get("password"));
		dataSource.setDriverClassName(dataSourceValues.get("driverClassName"));
		return dataSource;
	}

}

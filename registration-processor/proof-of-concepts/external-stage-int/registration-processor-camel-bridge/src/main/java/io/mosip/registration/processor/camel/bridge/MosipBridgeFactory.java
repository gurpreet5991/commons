package io.mosip.registration.processor.camel.bridge;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.vertx.VertxComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RoutesDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.camel.bridge.util.BridgeUtil;
import io.mosip.registration.processor.camel.bridge.util.PropertyFileUtil;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.vertx.camel.CamelBridge;
import io.vertx.camel.CamelBridgeOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;

/**
 * This class provides.
 *
 * @author Mukul Puspam
 */
public class MosipBridgeFactory extends AbstractVerticle {

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(MosipBridgeFactory.class);

	/**
	 * Gets the event bus.
	 *
	 * @return the event bus
	 */
	public static void getEventBus() {
		String igniteFileName = BridgeUtil.getPropertyFromConfigServer("ignite.cluster.manager.file.name");
		String igniteUrl = PropertyFileUtil.getProperty(MosipBridgeFactory.class, "bootstrap.properties", "config.server.url");
		igniteUrl = igniteUrl + "/*/" + BridgeUtil.getActiveProfile() + "/" + BridgeUtil.getCloudConfigLabel() + "/"
				+ igniteFileName;
		URL url = null;
		try {
			url = new URL(igniteUrl);
		} catch (MalformedURLException e1) {
			regProcLogger.error("", "", "", e1.getMessage());
		}
		ClusterManager clusterManager = new IgniteClusterManager(url);
		VertxOptions options = new VertxOptions().setClusterManager(clusterManager).setHAEnabled(true)
				.setClustered(true);

		Vertx.clusteredVertx(options, vertx -> {
			if (vertx.succeeded()) {
				vertx.result().deployVerticle(MosipBridgeFactory.class.getName(),
						new DeploymentOptions().setHa(true).setWorker(true));
			} else {
				regProcLogger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), "failed : ", vertx.cause().toString());
			}
		});
	}

	@Override
	public void start() throws Exception {
		ApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("spring.xml");
		JndiRegistry registry=null;
		    String[] beanNames=classPathXmlApplicationContext.getBeanDefinitionNames();
		    if (beanNames != null) {
		      Map<String,String> enviroment= new HashMap<>();
		      enviroment.put("java.naming.factory.initial", "org.apache.camel.util.jndi.CamelInitialContextFactory");
		      registry= new JndiRegistry(enviroment);
		      for (String name : beanNames) {
		            registry.bind(name,classPathXmlApplicationContext.getBean(name));
		      }
		    }
		CamelContext camelContext = new DefaultCamelContext(registry);
		camelContext.setStreamCaching(true);
		VertxComponent vertxComponent = new VertxComponent();
		vertxComponent.setVertx(vertx);
//		RestTemplate restTemplate = new RestTemplate();
//		String camelRoutesFileName = BridgeUtil.getPropertyFromConfigServer("camel.routes.file.name");
//		String camelRoutesUrl = PropertyFileUtil.getProperty(MosipBridgeFactory.class, "bootstrap.properties", "config.server.url");
//		camelRoutesUrl = camelRoutesUrl + "/*/" + BridgeUtil.getActiveProfile() + "/" + BridgeUtil.getCloudConfigLabel()
//				+ "/" + camelRoutesFileName;
//		ResponseEntity<Resource> responseEntity = restTemplate.exchange(camelRoutesUrl, HttpMethod.GET, null,
//				Resource.class);
//		RoutesDefinition routes = camelContext.loadRoutesDefinition(responseEntity.getBody().getInputStream());
		RoutesDefinition routes = camelContext.loadRoutesDefinition(ClassLoader.getSystemResourceAsStream("camel-routes.xml"));
		camelContext.addRouteDefinitions(routes.getRoutes());
		camelContext.addComponent("vertx", vertxComponent);
		camelContext.start();
		CamelBridge.create(vertx, new CamelBridgeOptions(camelContext)).start();
	}
}

package io.mosip.registration.processor.camel.bridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.camel.CamelContext;
import org.apache.camel.component.vertx.VertxComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;

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
		CamelContext camelContext = new DefaultCamelContext();
		VertxComponent vertxComponent = new VertxComponent();
		vertxComponent.setVertx(vertx);
//		RestTemplate restTemplate = new RestTemplate();
//		String camelRoutesFileName = BridgeUtil.getPropertyFromConfigServer("camel.routes.file.name");
//		String camelRoutesUrl = PropertyFileUtil.getProperty(MosipBridgeFactory.class, "bootstrap.properties", "config.server.url");
//		camelRoutesUrl = camelRoutesUrl + "/*/" + BridgeUtil.getActiveProfile() + "/" + BridgeUtil.getCloudConfigLabel()
//				+ "/" + camelRoutesFileName;
//		ResponseEntity<Resource> responseEntity = restTemplate.exchange(camelRoutesUrl, HttpMethod.GET, null,
//				Resource.class);
		File file=new File("C:\\registration-processor-camel-routes-dev.xml");
		InputStream is=new FileInputStream(file);
		RoutesDefinition routes = camelContext.loadRoutesDefinition(is);
		camelContext.addRouteDefinitions(routes.getRoutes());
		camelContext.addComponent("vertx", vertxComponent);
		camelContext.start();
		CamelBridge.create(vertx, new CamelBridgeOptions(camelContext)).start();
	}
}

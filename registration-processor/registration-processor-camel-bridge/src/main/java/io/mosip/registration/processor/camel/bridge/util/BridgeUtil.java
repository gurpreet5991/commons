package io.mosip.registration.processor.camel.bridge.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * This class interacts with config server and gets the required values
 * 
 * @author Pranav Kumar
 * @since 0.0.1
 *
 */
public class BridgeUtil {

	private static String URL = "http://104.211.212.28:51000/registration-processor-camel-bridge/dev/DEV";

	private static JsonObject bridgeConfiguration = null;

	/**
	 * This method gets all values from config server as a JsonObject and stores it
	 * locally
	 */
	public static void getConfiguration() {

		CompletableFuture<JsonObject> configuration = new CompletableFuture<>();

		ConfigStoreOptions configStoreOptions = new ConfigStoreOptions().setType("spring-config-server")
				.setConfig(new JsonObject().put("url", URL).put("timeout", 70000));

		ConfigRetriever configRetriever = ConfigRetriever.create(Vertx.vertx(),
				new ConfigRetrieverOptions().addStore(configStoreOptions));

		configRetriever.getConfig(config -> {
			if (config.succeeded()) {
				configuration.complete(config.result());
			}
		});

		// TODO Exception handling properly
		try {
			BridgeUtil.bridgeConfiguration = configuration.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * This method returns the camel endpoint along with component
	 * 
	 * @param messageBusAddress
	 *            The address to be used for endpoint
	 * @return The address as per the configured camel component
	 */
	public static String getEndpoint(MessageBusAddress messageBusAddress) {
		if (BridgeUtil.bridgeConfiguration == null) {
			getConfiguration();
		}
		return BridgeUtil.bridgeConfiguration.getString("registration.processor.component")
				+ messageBusAddress.getAddress();
	}

	/**
	 * This method returns the address for localhost
	 * 
	 * @return The address for localhost
	 */
	public static String getLocalHost() {
		if (BridgeUtil.bridgeConfiguration == null) {
			getConfiguration();
		}
		return BridgeUtil.bridgeConfiguration.getString("registration.processor.localhost");
	}

	/**
	 * This method returns the configured IP and port range for Vertx clustering
	 * 
	 * @return the configured IP and port range for Vertx clustering as a List
	 */
	public static List<String> getIpAddressPortRange() {
		if (BridgeUtil.bridgeConfiguration == null) {
			getConfiguration();
		}
		String ipPortRange = BridgeUtil.bridgeConfiguration.getString("registration.processor.vertx.ip.range");
		ArrayList<String> ipPort = new ArrayList<>();
		ipPort.add(ipPortRange);
		return ipPort;
	}
}

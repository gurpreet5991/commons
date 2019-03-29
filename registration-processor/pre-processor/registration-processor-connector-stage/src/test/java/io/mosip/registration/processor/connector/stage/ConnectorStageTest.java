//package io.mosip.registration.processor.connector.stage;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import io.mosip.registration.processor.connector.ConnectorApplication;
//import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
//import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
//import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
//import io.vertx.core.Handler;
//import io.vertx.core.MultiMap;
//import io.vertx.core.Vertx;
//import io.vertx.core.buffer.Buffer;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.http.HttpServerRequest;
//import io.vertx.core.http.HttpServerResponse;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.auth.User;
//import io.vertx.ext.web.Cookie;
//import io.vertx.ext.web.FileUpload;
//import io.vertx.ext.web.Locale;
//import io.vertx.ext.web.ParsedHeaderValues;
//import io.vertx.ext.web.Route;
//import io.vertx.ext.web.RoutingContext;
//import io.vertx.ext.web.Session;
//@RunWith(SpringRunner.class)
//public class ConnectorStageTest {
//	private RoutingContext ctx;
//	private Boolean responseObject;
//	
//	ConnectorStage connectorStage = new ConnectorStage() {
//		
//		@Override
//		public void setResponse(RoutingContext ctx, Object object) {
//			responseObject = Boolean.TRUE;
//		}
//		
//		@Override
//		public void send(MosipEventBus mosipEventBus, MessageBusAddress toAddress, MessageDTO message) {
//		}
//		
//		@Override
//		public MosipEventBus getEventBus(Object verticleName, String url, int instanceNumber) {
//			return null;
//		}
//	};
//	
//	@Before
//	public void setup() {
//
//		ctx = setContext();
//		
//		ConnectorApplication.main(null);
//	}
//	@Test
//	public void processallTests() throws ClientProtocolException, IOException{
//		processURLTest();
//		connectorTest();
//		healthCheckTest();
//	}
//	private void healthCheckTest() throws ClientProtocolException, IOException {
//		HttpGet httpGet = new HttpGet("http://localhost:8091/registration-connector/health");
//		HttpClient client = HttpClientBuilder.create().build();
//		HttpResponse getResponse = client.execute(httpGet);
//		assertEquals(200, getResponse.getStatusLine().getStatusCode());
//		
//	}
//	public void processURLTest() {
//		connectorStage.processURL(ctx);
//		assertTrue(responseObject);
//	}
//	public void connectorTest() throws ClientProtocolException, IOException{
//		HttpPost httpPost = new HttpPost("http://localhost:8091/registration-connector/registration-processor/connector/v1.0");
//
//	    String json = "{'rid':'27847657360002520181208183004','isValid':'true','internalError':'false','messageBusAddress':null,'retryCount':0}";
//	    StringEntity entity = new StringEntity(json);
//	    httpPost.setEntity(entity);
//	    httpPost.setHeader("Content-type", "application/json");
//		
//	    CloseableHttpResponse response = HttpClients.createDefault().execute(httpPost);
//	    assertEquals(response.getStatusLine().getStatusCode(), 200);
//	}
//	private RoutingContext setContext() {
//		return new RoutingContext() {
//
//			@Override
//			public Set<FileUpload> fileUploads() {
//				return null;
//			}
//
//			@Override
//			public Vertx vertx() {
//				return null;
//			}
//
//			@Override
//			public User user() {
//				return null;
//			}
//
//			@Override
//			public int statusCode() {
//				return 0;
//			}
//
//			@Override
//			public void setUser(User user) {
//			}
//
//			@Override
//			public void setSession(Session session) {
//			}
//
//			@Override
//			public void setBody(Buffer body) {
//			}
//
//			@Override
//			public void setAcceptableContentType(String contentType) {
//			}
//
//			@Override
//			public Session session() {
//				return null;
//			}
//
//			@Override
//			public HttpServerResponse response() {
//				return null;
//			}
//
//			@Override
//			public void reroute(HttpMethod method, String path) {
//			}
//
//			@Override
//			public HttpServerRequest request() {
//				return null;
//			}
//
//			@Override
//			public boolean removeHeadersEndHandler(int handlerID) {
//				return false;
//			}
//
//			@Override
//			public Cookie removeCookie(String name, boolean invalidate) {
//				return null;
//			}
//
//			@Override
//			public boolean removeBodyEndHandler(int handlerID) {
//				return false;
//			}
//
//			@Override
//			public <T> T remove(String key) {
//				return null;
//			}
//
//			@Override
//			public MultiMap queryParams() {
//				return null;
//			}
//
//			@Override
//			public List<String> queryParam(String query) {
//				return null;
//			}
//
//			@Override
//			public RoutingContext put(String key, Object obj) {
//				return null;
//			}
//
//			@Override
//			public Map<String, String> pathParams() {
//				return null;
//			}
//
//			@Override
//			public String pathParam(String name) {
//				return null;
//			}
//
//			@Override
//			public ParsedHeaderValues parsedHeaders() {
//				return null;
//			}
//
//			@Override
//			public String normalisedPath() {
//				return null;
//			}
//
//			@Override
//			public void next() {
//			}
//
//			@Override
//			public String mountPoint() {
//				return null;
//			}
//
//			@Override
//			public Cookie getCookie(String name) {
//				return null;
//			}
//
//			@Override
//			public String getBodyAsString(String encoding) {
//				return null;
//			}
//
//			@Override
//			public String getBodyAsString() {
//				return null;
//			}
//
//			@Override
//			public JsonArray getBodyAsJsonArray() {
//				return null;
//			}
//
//			@Override
//			public JsonObject getBodyAsJson() {
//				JsonObject obj= new JsonObject();
//				obj.put("rid", "51130282650000320190117144316");
//				obj.put("isValid", true);
//				obj.put("internalError", false);
//				return obj;
//			}
//
//			@Override
//			public Buffer getBody() {
//				return null;
//			}
//
//			@Override
//			public String getAcceptableContentType() {
//				return null;
//			}
//
//			@Override
//			public <T> T get(String key) {
//				return null;
//			}
//
//			@Override
//			public Throwable failure() {
//				return null;
//			}
//
//			@Override
//			public boolean failed() {
//				return false;
//			}
//
//			@Override
//			public void fail(Throwable throwable) {
//			}
//
//			@Override
//			public void fail(int statusCode) {
//			}
//
//			@Override
//			public Map<String, Object> data() {
//				return null;
//			}
//
//			@Override
//			public Route currentRoute() {
//				return null;
//			}
//
//			@Override
//			public Set<Cookie> cookies() {
//				return null;
//			}
//
//			@Override
//			public int cookieCount() {
//				return 0;
//			}
//
//			@Override
//			public void clearUser() {
//			}
//
//			@Override
//			public int addHeadersEndHandler(Handler<Void> handler) {
//				return 0;
//			}
//
//			@Override
//			public RoutingContext addCookie(Cookie cookie) {
//				return null;
//			}
//
//			@Override
//			public int addBodyEndHandler(Handler<Void> handler) {
//				return 0;
//			}
//
//			@Override
//			public List<Locale> acceptableLocales() {
//				return null;
//			}
//		};
//	}
//}

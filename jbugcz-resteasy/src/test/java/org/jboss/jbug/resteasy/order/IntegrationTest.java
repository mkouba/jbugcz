package org.jboss.jbug.resteasy.order;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

/**
 * 
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class IntegrationTest {

	@Deployment(testable = false)
	public static WebArchive createTestArchive() {
		return ShrinkWrap
				.create(WebArchive.class)
				.addClasses(Order.class, OrderService.class,
						OrderResource.class, LoggerProducer.class,
						TokenResource.class, TokenStore.class,
						SecurityInterceptor.class)
				// Enable JAX-RS
				.setWebXML(new File("src/main/webapp/WEB-INF/web.xml"))
				// We could also use ShrinkWrap Descriptors
				// .setWebXML(
				// new StringAsset(Descriptors
				// .create(WebAppDescriptor.class)
				// .createServletMapping()
				// .servletName("javax.ws.rs.core.Application")
				// .urlPattern("/rest/*").up().exportAsString()))
				// Enable CDI
				.addAsWebInfResource(
						new File("src/main/webapp/WEB-INF/beans.xml"))
				// .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				// Add slf4j module dependency (JBoss AS7 specific)
				.addAsManifestResource(
						new File("src/main/resources/MANIFEST.MF"));
	}

	@ArquillianResource
	private URL contextPath;

	@Test
	public void testUnauthorizedAccess() throws Exception {

		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);

		Page page = webClient.getPage(createGetRequest("rest/order/01?token=", "", APPLICATION_JSON));
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), page.getWebResponse()
				.getStatusCode());

		page = webClient.getPage(createGetRequest("rest/order/01?token=", "012345", APPLICATION_JSON));
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), page.getWebResponse()
				.getStatusCode());

		WebRequest webRequest = new WebRequest(new URL(contextPath
				+ "rest/order"), HttpMethod.POST);
		webRequest.setRequestBody("{\"id\":\"01\"}");
		webRequest.setAdditionalHeader("Content-Type", APPLICATION_JSON);
		page = webClient.getPage(webRequest);
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), page.getWebResponse()
				.getStatusCode());
	}

	@Test
	public void testTokenResource() throws Exception {

		WebClient webClient = new WebClient();

		TextPage tokenPage = webClient.getPage(new WebRequest(new URL(
				contextPath + "rest/token?password=Bar"), HttpMethod.POST));
		assertTrue(StringUtils.isBlank(tokenPage.getContent()));

		tokenPage = webClient.getPage(new WebRequest(new URL(contextPath
				+ "rest/token?password=Foo"), HttpMethod.POST));
		assertFalse(StringUtils.isBlank(tokenPage.getContent()));
	}

	@Test
	public void testOrderResource() throws Exception {

		WebClient webClient = new WebClient();

		// Get token (login)
		TextPage tokenPage = webClient.getPage(new WebRequest(new URL(
				contextPath + "rest/token?password=Foo"), HttpMethod.POST));
		assertFalse(StringUtils.isBlank(tokenPage.getContent()));
		String token = tokenPage.getContent();

		// Create order
		Page jsonOrderPage = null;

		WebRequest webRequest = new WebRequest(new URL(contextPath
				+ "rest/order?token=" + token), HttpMethod.POST);
		webRequest.setAdditionalHeader("Content-Type", APPLICATION_JSON);
		webRequest.setRequestBody("{\"id\":\"01\"}");
		jsonOrderPage = webClient.getPage(webRequest);
		assertEquals(Status.OK.getStatusCode(), jsonOrderPage.getWebResponse()
				.getStatusCode());

		// Get order in JSON format
		jsonOrderPage = webClient.getPage(createGetRequest("rest/order/01?token=", token, APPLICATION_JSON));
		assertEquals("{\"id\":\"01\"}", jsonOrderPage.getWebResponse()
				.getContentAsString());

		// Get order in plain text
		TextPage txtOrderPage = webClient.getPage(createGetRequest(
				"rest/order/01?token=", token, TEXT_PLAIN));
		assertEquals(new Order("01").toString(), txtOrderPage.getContent());
	}

	@Test
	public void testTokenValidInterval() throws Exception {

		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);

		// Get token (login)
		TextPage tokenPage = webClient.getPage(new WebRequest(new URL(
				contextPath + "rest/token?password=Foo"), HttpMethod.POST));
		assertFalse(StringUtils.isBlank(tokenPage.getContent()));
		String token = tokenPage.getContent();

		// Create order
		Page jsonOrderPage = null;

		WebRequest webRequest = new WebRequest(new URL(contextPath
				+ "rest/order?token=" + token), HttpMethod.POST);
		webRequest.setAdditionalHeader("Content-Type", APPLICATION_JSON);
		webRequest.setRequestBody("{\"id\":\"01\"}");
		jsonOrderPage = webClient.getPage(webRequest);
		assertEquals(Status.OK.getStatusCode(), jsonOrderPage.getWebResponse()
				.getStatusCode());

		// Get order in JSON format
		jsonOrderPage = webClient.getPage(createGetRequest("rest/order/01?token=", token, APPLICATION_JSON));
		assertEquals("{\"id\":\"01\"}", jsonOrderPage.getWebResponse()
				.getContentAsString());

		// Wait
		Thread.sleep((TokenStore.DEFAULT_VALID_INTERVAL_SECONDS + 1) * 1000);

		// Get order in JSON format
		jsonOrderPage = webClient.getPage(createGetRequest("rest/order/01?token=", token, APPLICATION_JSON));
		assertEquals(Status.UNAUTHORIZED.getStatusCode(), jsonOrderPage
				.getWebResponse().getStatusCode());

	}

	private WebRequest createGetRequest(String path, String token,
			String acceptHeader) throws Exception {

		WebRequest request = new WebRequest(new URL(contextPath
				+ "rest/order/01?token=" + token), HttpMethod.GET);
		if (acceptHeader != null) {
			request.setAdditionalHeader("Accept", acceptHeader);
		}
		return request;
	}

}

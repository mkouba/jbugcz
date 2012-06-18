package org.jboss.jbug.resteasy.order;

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyWriterInterceptor;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.slf4j.Logger;

/**
 * Security interceptor implemented as RESTEasy {@link PreProcessInterceptor}. 
 * 
 * @author Martin Kouba
 * @see PostProcessInterceptor
 * @see MessageBodyReaderInterceptor
 * @see MessageBodyWriterInterceptor
 */
@Provider
@ServerInterceptor
public class SecurityInterceptor implements PreProcessInterceptor,
		AcceptedByMethod {

	@Context
	HttpServletRequest httpRequest;
	
	@Inject
	private Logger logger;

	@Inject
	private TokenStore tokenStore;
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean accept(Class declaring, Method method) {
		// Intercept resource methods on OrderResource only 
		return declaring.equals(OrderResource.class);
	}

	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod method)
			throws Failure, WebApplicationException {

		// Extract token from HTTP request
		String token = httpRequest.getParameter("token");

		if (token == null || !tokenStore.isTokenValid(token)) {
			// Log and return HTTP 401
			logger.warn("Access not allowed {}", request.getPreprocessedPath());
			return (ServerResponse) Response.status(Status.UNAUTHORIZED)
					.build();
		}
		
		// Shift token valid until
		tokenStore.shiftValidUntil(token);
		
		// Back to normal processing
		return null;
	}
	
	/**
	 * Interceptor is application scoped CDI bean.
	 */
	@PostConstruct
	public void postConstruct() {
		logger.info("SecurityInterceptor constructed");
	}

}

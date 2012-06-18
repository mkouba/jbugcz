package org.jboss.jbug.resteasy.order;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;

/**
 * Note that since we use EE 6 the resteasy-cdi module asks the CDI container
 * for the managed instance of a JAX-RS component instead of creating a new
 * instance.
 * 
 * @author Martin Kouba
 */
@Path("/order")
public class OrderResource {
	
	@Inject
	private Logger logger;
	
	@Inject
	private OrderService orderService;
	
	/**
	 * Matches <code>GET /order/123</code>.
	 * 
	 * @param id
	 * @return found order in JSON format or plain text
	 */
	@GET
	@Path("/{id}")
	@Produces({APPLICATION_JSON, TEXT_PLAIN})
	public Order getOrderJson(@PathParam("id") String id, @HeaderParam("Accept") String accept) {
		logger.info("Get order {} accepting {}", id, accept);
		return orderService.getOrder(id);
	}
	
	/**
	 * Matches <code>POST /order</code>.
	 * 
	 * @param order JSON data (we use Jackson provider for automatic deserialization)
	 */
	@POST
	@Consumes(APPLICATION_JSON)
	public void createOrder(Order order) {
		
		if(order.getId() == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		
		logger.info("Create new order");
		orderService.createOrder(order);		
	}
	
	
}

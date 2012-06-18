package org.jboss.jbug.resteasy.order;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * Simple persistence simulation.  
 *  
 * @author Martin Kouba
 */
@ApplicationScoped
public class OrderService {
	
	private Map<String, Order> orders = Collections.synchronizedMap(new HashMap<String, Order>());
	
	/**
	 * @param id
	 * @return order with specified id or <code>null</code> if no such order exist
	 */
	public Order getOrder(String id) {
		return orders.get(id);
	}
	
	/**
	 * 
	 * @param order
	 */
	public void createOrder(Order order) {
		orders.put(order.getId(), order);
	}

}

package org.jboss.jbug.resteasy.order;

/**
 * Simple POJO. Represents order entity.
 * 
 * @author Martin Kouba
 */
public class Order {

	private String id;

	public Order() {
	}
	
	public Order(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return String.format("Order [id=%s]", id);
	}

}

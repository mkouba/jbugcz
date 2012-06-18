package org.jboss.jbug.resteasy.order;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

/**
 * Token store. Cleanup is not implemented.
 * 
 * @author Martin Kouba
 */
@ApplicationScoped
public class TokenStore {

	/**
	 * Too simple authentication :-) 
	 */
	private static final String SECRET = "Foo";

	public static final int DEFAULT_VALID_INTERVAL_SECONDS = 2;

	@Inject
	private Logger logger;
	
	private Map<String, Date> tokenMap = Collections
			.synchronizedMap(new HashMap<String, Date>());
	
	private int validIntervalSeconds = DEFAULT_VALID_INTERVAL_SECONDS;

	/**
	 * 
	 * @return new token if secrect matches
	 */
	public String generateNewToken(String secret) {

		if (!SECRET.equals(secret))
			return "";

		String token = UUID.randomUUID().toString();
		resetToken(token);
		return token;
	}

	/**
	 * 
	 * @param token
	 */
	public void shiftValidUntil(String token) {

		if (isTokenValid(token)) {
			logger.info("Shift token valid interval {}", token);
			resetToken(token);
		}
	}

	/**
	 * 
	 * @param token
	 * @return <code>true</code> if token is valid, <code>false</code> otherwise
	 */
	public boolean isTokenValid(String token) {

		Date validUntil = tokenMap.get(token);

		if (validUntil != null
				&& (System.currentTimeMillis() < validUntil.getTime()))
			return true;

		return false;
	}
	
	private void resetToken(String token) {

		Calendar validUntil = Calendar.getInstance();
		validUntil.add(Calendar.SECOND, validIntervalSeconds);
		tokenMap.put(token, validUntil.getTime());

		logger.info("Set token {}, valid until {}", token, validUntil.getTime());
	}
	
	@PostConstruct
	public void init() {
		String intValue = System.getProperty("validIntervalSeconds");
		if(intValue != null && NumberUtils.isDigits(intValue)) {
			validIntervalSeconds = Integer.valueOf(intValue);
		}
	}

}

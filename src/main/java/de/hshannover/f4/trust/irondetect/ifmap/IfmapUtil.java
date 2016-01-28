package de.hshannover.f4.trust.irondetect.ifmap;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.config.BasicAuthConfig;
import de.hshannover.f4.trust.ifmapj.config.CertAuthConfig;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irondetect.Main;
import de.hshannover.f4.trust.irondetect.util.Configuration;

public class IfmapUtil {
	
	private static final Logger logger = Logger.getLogger(IfmapUtil.class);	
	
	private static Properties CONFIG = Main.getConfig();
	
	/**
	 * Create {@link SSRC}.
	 * 
	 * @throws FileNotFoundException
	 * @throws InitializationException
	 */
	public static SSRC initSsrc(String username, String password) throws FileNotFoundException,
			InitializationException {
		String authMethod = CONFIG.getString(Configuration.KEY_IFMAP_AUTH_METHOD, Configuration.DEFAULT_VALUE_IFMAP_AUTH_METHOD);
		
		String basicUrl = CONFIG.getString(Configuration.KEY_IFMAP_BASIC_URL, Configuration.DEFAULT_VALUE_IFMAP_BASIC_URL);
		String certUrl = CONFIG.getString(Configuration.KEY_IFMAP_CERT_URL, Configuration.DEFAULT_VALUE_IFMAP_CERT_URL);
		String trustStorePath = CONFIG.getString(Configuration.KEY_IFMAP_TRUSTSTORE_PATH, Configuration.DEFAULT_VALUE_IFMAP_TRUSTSTORE_PATH);
		String trustStorePassword = CONFIG.getString(Configuration.KEY_IFMAP_TRUSTSTORE_PASSWORD, Configuration.DEFAULT_VALUE_IFMAP_TRUSTSTORE_PASSWORD);
		
		if (authMethod.equalsIgnoreCase("basic")) {
			logger.info("Creating SSRC using basic authentication.");
			BasicAuthConfig basicConfig = new BasicAuthConfig(basicUrl, username, password, trustStorePath, trustStorePassword);
			return IfmapJ.createSsrc(basicConfig);
		}
		else if (authMethod.equalsIgnoreCase("cert")) {
			logger.info("Creating SSRC using certificate-based authentication.");
			CertAuthConfig certConfig = new CertAuthConfig(certUrl, trustStorePath, trustStorePassword, trustStorePath, trustStorePassword);
			return IfmapJ.createSsrc(certConfig);
		}
		else {
			throw new IllegalArgumentException("unknown authentication method '" + authMethod + "'");
		}
	}
}

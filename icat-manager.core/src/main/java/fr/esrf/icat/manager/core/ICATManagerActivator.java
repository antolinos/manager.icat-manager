package fr.esrf.icat.manager.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.manager.core.icatserver.ICATServer;

public class ICATManagerActivator implements BundleActivator {
	
	private final static Logger LOG = LoggerFactory.getLogger(ICATManagerActivator.class);

	private final File configfile = new File(System.getProperty("user.home"), ".icat_servers");
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager starting in context " + context.toString());
		
		LOG.debug("Configuration file at: " + configfile.getAbsolutePath());
		
		if(configfile.exists()) {
			final ICATDataService service = ICATDataService.getInstance();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(configfile));
				String line;
				while((line = reader.readLine()) != null) {
					service.addServer(new ICATServer(line.trim()));
				}
				LOG.debug("Loaded " + service.getServerList().size() + " server URLs");
			} catch (IOException e) {
				LOG.error("Error reading server file " + configfile.getAbsolutePath(), e);
			} finally {
				if(null != reader) {
					try {
						reader.close();
					} catch (IOException e) {
						LOG.warn("Error closing file " + configfile.getAbsolutePath(), e);
					}
				}
			}
		} else {
			LOG.debug(configfile.getAbsolutePath() + " file not found");
		}
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager stopping");
		final ICATDataService service = ICATDataService.getInstance();
		service.stop();
		final List<ICATServer> servers = service.getServerList();
		if(servers.isEmpty()) {
			LOG.debug("No server URLs to save");
		} else {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(configfile));
				for(ICATServer server : servers) {
					writer.write(server.getServerURL());
					writer.newLine();
				}
				LOG.debug("Saved " + servers.size() + " server URLs");
			} catch (IOException e) {
				LOG.error("Error reading server file " + configfile.getAbsolutePath(), e);
			} finally {
				if(null != writer) {
					try {
						writer.close();
					} catch (IOException e) {
						LOG.warn("Error closing file " + configfile.getAbsolutePath(), e);
					}
				}
			}
		}
	}

}

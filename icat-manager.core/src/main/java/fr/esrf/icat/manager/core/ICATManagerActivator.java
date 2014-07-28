package fr.esrf.icat.manager.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.manager.core.icatserver.ICATServer;

public class ICATManagerActivator implements BundleActivator {
	
	private final static Logger LOG = LoggerFactory.getLogger(ICATManagerActivator.class);

	private ICATDataService service;
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager starting in context " + context.toString());
		service = ICATDataService.getInstance();
		service.addServer(new ICATServer("https://ovm-icat-sandbox.esrf.fr:8181"));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager stopping");
		service.stop();
	}

}

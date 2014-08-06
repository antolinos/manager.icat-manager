package fr.esrf.icat.manager.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ICATManagerActivator implements BundleActivator {
	
	private final static Logger LOG = LoggerFactory.getLogger(ICATManagerActivator.class);
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager starting in context " + context.toString());
		ICATDataService.getInstance().init();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager stopping");
		ICATDataService.getInstance().stop();
	}

}

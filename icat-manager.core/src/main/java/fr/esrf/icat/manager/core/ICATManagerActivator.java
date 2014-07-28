package fr.esrf.icat.manager.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import fr.esrf.icat.manager.core.icatserver.ICATServer;

public class ICATManagerActivator implements BundleActivator {

	private ICATDataService service;
	
	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("ICAT Manager starting in context " + context.toString());
		service = ICATDataService.getInstance();
		service.addServer(new ICATServer("https://ovm-icat-sandbox.esrf.fr:8181"));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("ICAT Manager stopping");
		service.stop();
	}

}

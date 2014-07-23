package fr.esrf.icat.manager.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ICATManagerActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("ICAT Manager starting in context " + context.toString());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("ICAT Manager stopping");
	}

}

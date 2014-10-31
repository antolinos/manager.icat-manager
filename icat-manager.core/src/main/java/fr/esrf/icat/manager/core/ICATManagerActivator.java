package fr.esrf.icat.manager.core;

/*
 * #%L
 * icat-manager :: core
 * %%
 * Copyright (C) 2014 ESRF - The European Synchrotron
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class ICATManagerActivator implements BundleActivator {
	
	private final static Logger LOG = LoggerFactory.getLogger(ICATManagerActivator.class);
	public static final String UPDATE_FLAG_FILENAME = ".need_clear_persisted_state";
	private final static String[] PATHS = new String[]{".metadata", ".plugins", "org.eclipse.e4.workbench", "workbench.xmi"};
	
	@Override
	public void start(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager starting in context " + context.toString());
		ICATDataService.getInstance().init();
		ICATManagerActivator.logProxySettings();
		ICATManagerActivator.configureProxyUsingSpecificSystemProperties();
		ICATManagerActivator.logProxySettings();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LOG.debug("ICAT Manager stopping");
		ICATDataService.getInstance().stop();
	}
	
	@PostContextCreate
	void postContextCreate() {
		try {
			File flagfile = new File(new URL(Platform.getInstanceLocation().getURL(),UPDATE_FLAG_FILENAME).toURI());
			if (flagfile.exists()) { 
				LOG.debug("Cleaning persisted state after update");
				final File xmifile = Paths.get(flagfile.getParentFile().getPath(), PATHS).toFile();
				xmifile.delete();
				flagfile.delete();
			}
		} catch (MalformedURLException | URISyntaxException e) {
			LOG.error("Error cleaning persisted state", e);
		}
		
	}

	 public static void logProxySettings() {
		 // Skip the whole thing if debug not logged
		 if(!LOG.isDebugEnabled()) return;
		 
		 final BundleContext bc = FrameworkUtil.getBundle(ICATManagerActivator.class).getBundleContext();
		 final ServiceReference<?> serviceReference = bc.getServiceReference(IProxyService.class.getName());
		 
		 LOG.debug("IcatManager proxy properties:");
		 LOG.debug("HTTP   : " + System.getProperty("icatmanager.http.proxyHost") + ":" + System.getProperty("icatmanager.http.proxyPort"));
		 LOG.debug("HTTPS  : " + System.getProperty("icatmanager.https.proxyHost") + ":" + System.getProperty("icatmanager.https.proxyPort"));
		 LOG.debug("NOPROXY: " + System.getProperty("icatmanager.http.nonProxyHosts"));
		 
		 LOG.debug("System proxy properties:");
		 LOG.debug("HTTP   : " + System.getProperty("http.proxyHost") + ":" + System.getProperty("http.proxyPort"));
		 LOG.debug("HTTPS  : " + System.getProperty("https.proxyHost") + ":" + System.getProperty("https.proxyPort"));
		 LOG.debug("NOPROXY: " + System.getProperty("http.nonProxyHosts"));

		 try {
			 LOG.debug("Eclipse proxy properties:");
			 final IProxyService proxyService = (IProxyService) bc.getService(serviceReference);
			 
			 LOG.debug("proxiesEnabled=" + proxyService.isProxiesEnabled());
			 
			 final boolean hasSystemProxies = proxyService.hasSystemProxies();
			 final boolean systemProxiesEnabled = proxyService.isSystemProxiesEnabled();

			 LOG.debug("hasSystemProxies=" + hasSystemProxies + ", systemProxiesEnabled=" + systemProxiesEnabled);
			 
			 for (final IProxyData proxyData : proxyService.getProxyData()) {
				 LOG.debug("Type=" + proxyData.getType() + ", host=" + proxyData.getHost() + ", port=" + proxyData.getPort());
			 }
			 LOG.debug("nonProxiedHosts=" + Arrays.toString(proxyService.getNonProxiedHosts()));
			 
		 } finally {
			 bc.ungetService(serviceReference);
		 }
	 }

	 private static void configureProxyUsingSpecificSystemProperties() throws CoreException {
		 LOG.debug("Configuring eclipse proxy using IcatManager proxy settings ...");
		 final BundleContext bc = FrameworkUtil.getBundle(ICATManagerActivator.class).getBundleContext();
		 final ServiceReference<?> serviceReference = bc.getServiceReference(IProxyService.class.getName());
		 try {
			 final IProxyService proxyService = (IProxyService) bc.getService(serviceReference);
			 final String httpProxyHost  = System.getProperty("icatmanager.http.proxyHost");
			 final String httpProxyPort  = System.getProperty("icatmanager.http.proxyPort");
			 final String httpsProxyHost = System.getProperty("icatmanager.https.proxyHost");
			 final String httpsProxyPort = System.getProperty("icatmanager.https.proxyPort");
			 final String nonProxyHosts  = System.getProperty("icatmanager.http.nonProxyHosts");
			 if (httpProxyHost != null) {
				 setProxyData(proxyService, IProxyData.HTTP_PROXY_TYPE, httpProxyHost, httpProxyPort);
			 }
			 if (httpsProxyHost != null) {
				 setProxyData(proxyService, IProxyData.HTTPS_PROXY_TYPE, httpsProxyHost, httpsProxyPort);
			 }
			 if (nonProxyHosts != null) {
				 proxyService.setNonProxiedHosts(nonProxyHosts.trim().split("\\|"));
			 }
			 proxyService.setSystemProxiesEnabled(false);
			 proxyService.setProxiesEnabled(true);
		 } finally {
			 bc.ungetService(serviceReference);
		 }
	 }

	 private static void setProxyData(final IProxyService proxyService, final String proxyType, final String proxyHost, final String proxPort) throws CoreException {
		 final IProxyData proxyData = proxyService.getProxyData(proxyType);
		 proxyData.setHost(proxyHost);
		 int port = -1;
		 try {
			 port = Integer.parseInt(proxPort);
		 } catch (final NumberFormatException e) {
			 // ignore and use default (port 8080)
		 }
		 if (port < 0 || port > 65535) {
			 port = 8080;
		 }
		 proxyData.setPort(port);
		 proxyService.setProxyData(new IProxyData[] { proxyData });
	 }
}

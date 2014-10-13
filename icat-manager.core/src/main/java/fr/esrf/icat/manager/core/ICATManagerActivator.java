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

import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
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

}

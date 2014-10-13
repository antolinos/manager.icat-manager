package fr.esrf.icat.manager.core.part;

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


import java.io.IOException;
import java.net.URL;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class BrowserHelp {

	public static final String HELP_URL = "html/ICATManagerHelp.html";
	
	@PostConstruct
	public void postConstruct(final Composite parent) throws IOException {
		Browser browser = new Browser(parent, SWT.NONE);
	    Bundle bundle = FrameworkUtil.getBundle(BrowserHelp.class);
	    URL url = FileLocator.find(bundle, new Path(HELP_URL), null);
		browser.setUrl(FileLocator.toFileURL(url).toString());
	}
	
}

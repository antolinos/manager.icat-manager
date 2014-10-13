package fr.esrf.icat.manager.core.part;

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

 
package fr.esrf.icat.manager.core.part;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.viewers.TreeViewer;

import fr.esrf.icat.manager.core.icatserver.IcatServerContentProvider;
import fr.esrf.icat.manager.core.icatserver.IcatServerLabelProvider;

public class ServerPart {
	
	private TreeViewer viewer;
	
	@Inject
	public ServerPart() {
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
	    viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	    viewer.setContentProvider(new IcatServerContentProvider());
	    viewer.setLabelProvider(new IcatServerLabelProvider());
	}
	
	@PreDestroy
	public void preDestroy() {
	}
	
	@Persist
	public void save() {
	}
	
}
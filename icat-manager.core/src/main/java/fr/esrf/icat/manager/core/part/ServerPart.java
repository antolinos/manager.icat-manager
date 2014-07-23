 
package fr.esrf.icat.manager.core.part;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import fr.esrf.icat.manager.core.icatserver.ICATServer;
import fr.esrf.icat.manager.core.icatserver.IcatServerContentProvider;
import fr.esrf.icat.manager.core.icatserver.IcatServerLabelProvider;

public class ServerPart {
	
	private TreeViewer viewer;
	private IcatServerContentProvider icatContentProvider;
	
	@Inject
	private EPartService partService;
	
	@Inject
	private EModelService modelService;
	
	@Inject
	private MWindow window;
	
	@Inject
	public ServerPart() {
	}
	
	@PostConstruct
	public void postConstruct(final Composite parent) {
	    viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
	    icatContentProvider = new IcatServerContentProvider();
		viewer.setContentProvider(icatContentProvider);
	    viewer.setLabelProvider(new IcatServerLabelProvider());
	    viewer.setInput(icatContentProvider.getRoot());
	    viewer.addDoubleClickListener(new IDoubleClickListener() {
	    	  @Override
	    	  public void doubleClick(DoubleClickEvent event) {
	    	    TreeViewer viewer = (TreeViewer) event.getViewer();
	    	    IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection(); 
	    	    Object selectedNode = thisSelection.getFirstElement(); 
	    	    if(selectedNode instanceof ICATServer) {
	    	    	viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
	    	    } else {
	    	    	MPartStack partstack = (MPartStack) modelService.find("icat-manager.core.partstack.mainstack", window);
	    	    	MPart mPart = modelService.createModelElement(MPart.class);
	    	    	mPart.setCloseable(true);
	    	        mPart.setLabel("Testing");
	    	        mPart.setElementId("icat-manager.core.part.data:");
	    	        mPart.setContributionURI("bundleclass://icat-manager.core/fr.esrf.icat.manager.core.part.DataPart");
	    	        partstack.getChildren().add(mPart);
	    	        partService. showPart(mPart, PartState.ACTIVATE);
	    	        System.out.println(mPart.getObject().getClass());
	    	    }
	    	  }
	    	});
	}
	
	@PreDestroy
	public void preDestroy() {
	}
	
	@Persist
	public void save() {
	}
	
}
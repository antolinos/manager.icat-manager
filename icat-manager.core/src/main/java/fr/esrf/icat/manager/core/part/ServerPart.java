 
package fr.esrf.icat.manager.core.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;

import javax.annotation.PreDestroy;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.handlers.ExitHandler;
import fr.esrf.icat.manager.core.handlers.OpenEntityHandler;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;
import fr.esrf.icat.manager.core.icatserver.ICATServer;
import fr.esrf.icat.manager.core.icatserver.IcatServerContentProvider;
import fr.esrf.icat.manager.core.icatserver.IcatServerLabelProvider;

@SuppressWarnings("restriction")
public class ServerPart implements PropertyChangeListener {
	
	private final static Logger LOG = LoggerFactory.getLogger(ServerPart.class);

	private TreeViewer viewer;
	private IcatServerContentProvider icatContentProvider;
	private ICATDataService service;
	
	@PostConstruct
	public void postConstruct(final Composite parent, final EMenuService menuService,
			final EPartService partService, final EModelService modelService,
			final ESelectionService selectionService, final MWindow window) {

		// only place I could think of for registering that
        window.getContext().set(IWindowCloseHandler.class, new ExitHandler(partService));

        // listening to server changes
		service = ICATDataService.getInstance();
	    service.addPropertyChangeListener(this);

	    // our viewer
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
	    icatContentProvider = new IcatServerContentProvider();
		viewer.setContentProvider(icatContentProvider);
	    viewer.setLabelProvider(new IcatServerLabelProvider());
	    viewer.setInput(service);
	    
	    // make selection available
	    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
	    	  @Override
	    	  public void selectionChanged(SelectionChangedEvent event) {
	    	    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
	    	    selectionService.setSelection(selection.getFirstElement());
	    	  }
    	}); 
	    
	    // toggle / connect / open entity on dbl click
	    viewer.addDoubleClickListener(new IDoubleClickListener() {
	    	  @Override
	    	  public void doubleClick(DoubleClickEvent event) {
	    	    TreeViewer viewer = (TreeViewer) event.getViewer();
	    	    IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection(); 
	    	    Object selectedNode = thisSelection.getFirstElement(); 
	    	    if(selectedNode instanceof ICATServer) {
	    	    	ICATServer server = (ICATServer)selectedNode;
	    	    	if(!server.isConnected()) {
	    	    		service.connect(server, parent.getShell());
	    	    	}
	    	    	if(server.isConnected()) {
		    	    	viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
	    	    	}
	    	    } else if (selectedNode instanceof ICATEntity){
	    	    	OpenEntityHandler.openEntityPart(partService, modelService, window, (ICATEntity)selectedNode);
	    	    } else {
	    	        LOG.warn("Selected element is neither an ICAT server nor an ICAT entity !");
	    	    }
    	    }
    	});
	    
	    // context menu
	    menuService.registerContextMenu(viewer.getControl(), "icat-manager.core.popupmenu.server");
	}
	
	@PreDestroy
	public void preDestroy() {
		service.removePropertyChangeListener(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		viewer.refresh();
	}
	
}
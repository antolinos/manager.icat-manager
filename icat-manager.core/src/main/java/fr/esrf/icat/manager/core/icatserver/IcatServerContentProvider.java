package fr.esrf.icat.manager.core.icatserver;

import java.util.List;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import fr.esrf.icat.manager.core.ICATDataService;

public class IcatServerContentProvider implements ITreeContentProvider {

	ICATDataService service;
	
	public IcatServerContentProvider() {
		super();
		service = ICATDataService.getInstance();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List<?>) inputElement).toArray();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return (parentElement instanceof ICATServer) && ((ICATServer)parentElement).isConnected() 
						? service.getEntityList((ICATServer)parentElement).toArray()
						: null;
	}

	@Override
	public Object getParent(Object element) {
		return element instanceof ICATEntity ? ((ICATEntity)element).getServer() : null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof ICATServer) && ((ICATServer)element).isConnected();
	}

}

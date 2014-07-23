package fr.esrf.icat.manager.core.icatserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class IcatServerContentProvider implements ITreeContentProvider {

	private List<ICATServer> _icatList;
	
	private Map<String, Class<?>> _managedEntities; 
	
	public IcatServerContentProvider() {
		super();
		_icatList = new ArrayList<ICATServer>();
		_icatList.add(new ICATServer("https://ovm-icat-sandbox.esrf.fr:8181"));
		_managedEntities = new HashMap<String, Class<?>>();
		try {
			_managedEntities.put("Instrument", Class.forName("fr.esrf.icat.manager.entities.InstrumentManager"));
			_managedEntities.put("Parameter", Class.forName("fr.esrf.icat.manager.entities.ParameterManager"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
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
		return _managedEntities.keySet().toArray();
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (element instanceof ICATServer);
	}

	public Object getRoot() {
		return _icatList;
	}

}

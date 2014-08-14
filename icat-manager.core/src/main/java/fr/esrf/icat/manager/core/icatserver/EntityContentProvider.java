package fr.esrf.icat.manager.core.icatserver;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;

public class EntityContentProvider implements  IStructuredContentProvider {

	private final static Logger LOG = LoggerFactory.getLogger(EntityContentProvider.class);
	private static final String DEFAULT_INCLUDE = "INCLUDE 1";
	private static final String ORDER_CLAUSE = "ORDER BY";
	private static final String ORDER_UP = "ASC";
	private static final String ORDER_DOWN = "DESC";

	private String sortingField;
	private int sortingOrder;
	
	public EntityContentProvider() {
		super();
		this.sortingField = null;
		this.sortingOrder = SWT.DOWN;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(null == inputElement || !(inputElement instanceof ICATEntity)) {
			return null;
		}
		ICATEntity entity = (ICATEntity) inputElement;
		if(LOG.isDebugEnabled()) {
			LOG.debug("Getting content of " + entity.getEntityName() + " from " + entity.getServer().getServerURL());
		}
		try {
			SimpleICATClient client = ICATDataService.getInstance().getClient(entity.getServer());
			List<WrappedEntityBean> search = client.search(makeSearchString(entity));
			return null == search ? null : search.toArray();
		} catch (ICATClientException e) {
			LOG.error("Unable to load entity content for entity " + entity.getEntityName(), e);
			return null;
		}
	}

	private String makeSearchString(final ICATEntity entity) {
		StringBuilder sb = new StringBuilder();
		sb.append(entity.getEntityName());
		sb.append(' ');
		sb.append(DEFAULT_INCLUDE);
		if(sortingField != null) {
			sb.append(' ');
			sb.append(ORDER_CLAUSE);
			sb.append(' ');
			sb.append(sortingField);
			sb.append(' ');
			if(sortingOrder == SWT.UP) {
				sb.append(ORDER_UP);
			} else {
				sb.append(ORDER_DOWN);
			}
		}
		return sb.toString();
	}

	public void toggleSortingColumn(final String field) {
		if(field.equals(sortingField)) {
			if(sortingOrder == SWT.UP) {
				sortingOrder = SWT.DOWN;
			} else {
				sortingOrder = SWT.UP;
			}
		} else {
			sortingField = field;
			sortingOrder = SWT.DOWN;
		}
	}

	public String getSortingField() {
		return sortingField;
	}
	
	public int getSortingOrder() {
		return sortingOrder;
	}
	
	

}

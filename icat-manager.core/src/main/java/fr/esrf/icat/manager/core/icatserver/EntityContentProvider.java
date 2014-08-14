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
	private static final int DEFAULT_PAGE_SIZE = 50;
	
	private String sortingField;
	private int sortingOrder;
	private int offset;
	private int pageSize;
	private int currentPageSize;
	private ICATEntity entity;
	
	public EntityContentProvider() {
		super();
		this.sortingField = ICATEntity.ID_FIELD;
		this.sortingOrder = SWT.DOWN;
		this.offset = 0;
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.currentPageSize = 0;
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
		entity = (ICATEntity) inputElement;
		final String searchString = makeSearchString(entity);
		if(LOG.isDebugEnabled()) {
			LOG.debug("Getting content of " + entity.getEntityName() + " from " + entity.getServer().getServerURL());
			LOG.debug(searchString);
		}
		try {
			SimpleICATClient client = ICATDataService.getInstance().getClient(entity.getServer());
			List<WrappedEntityBean> search = client.search(searchString);
			currentPageSize = null == search ? 0 : search.size();
			return null == search ? null : search.toArray();
		} catch (ICATClientException e) {
			LOG.error("Unable to load entity content for entity " + entity.getEntityName(), e);
			return null;
		}
	}

	private String makeSearchString(final ICATEntity entity) {
		StringBuilder sb = new StringBuilder();
		sb.append(offset);
		sb.append(',');
		sb.append(pageSize);
		sb.append(' ');
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

	public void toggleSortingField(final String field) {
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
		offset = 0;
	}

	public String getSortingField() {
		return sortingField;
	}
	
	public int getSortingOrder() {
		return sortingOrder;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getOffset() {
		return offset;
	}
	
	public void nextPage() {
		if(currentPageSize == pageSize) {
			offset += pageSize;
		}
	}
	
	public void previousPage() {
		offset = Math.max(0, offset - pageSize);
	}

	public boolean isFirstPage() {
		return offset == 0;
	}
	
	public boolean isLastPage() {
		return currentPageSize < pageSize;
	}
	
	public String getPaginationLabelText() {
		StringBuilder sb = new StringBuilder();
		sb.append(entity.getEntityName());
		sb.append(" from ");
		sb.append(offset);
		sb.append(" to ");
		sb.append(offset + currentPageSize - 1);
		return sb.toString();
	}

}

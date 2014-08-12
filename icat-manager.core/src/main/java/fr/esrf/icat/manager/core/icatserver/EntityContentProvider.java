package fr.esrf.icat.manager.core.icatserver;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;

public class EntityContentProvider implements  IStructuredContentProvider {

	private final static Logger LOG = LoggerFactory.getLogger(EntityContentProvider.class);
	private static final String QUERY_SUFFIX = " INCLUDE 1";

	private ICATEntity entity;
	private Object[] content;
	
	public EntityContentProvider(ICATEntity entity) {
		super();
		this.entity = entity;
		loadContent();
	}

	public void loadContent() {
		try {
			SimpleICATClient client = ICATDataService.getInstance().getClient(this.entity.getServer());
			List<WrappedEntityBean> search = client.search(this.entity.getEntityName() + QUERY_SUFFIX);
			content = null == search ? null : search.toArray();
		} catch (ICATClientException e) {
			LOG.error("Unable to load entity content for entity " + entity.getEntityName(), e);
		}
	}
	
	public WrappedEntityBean getExampleData() {
		if(content != null && content.length > 0) {
			return (WrappedEntityBean) content[0];
		}
		try {
			return ICATDataService.getInstance().getClient(entity.getServer()).create(entity.getEntityName());
		} catch (ICATClientException e) {
			LOG.error("Error creating " + entity.getEntityName(), e);
			return null;
		}
	}

	@Override
	public void dispose() {
		content = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return content;
	}

}

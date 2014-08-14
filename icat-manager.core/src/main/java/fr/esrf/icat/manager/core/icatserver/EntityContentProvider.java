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
	
	public EntityContentProvider() {
		super();
		this.entity = null;
		this.content = null;
	}

	@Override
	public void dispose() {
		this.entity = null;
		this.content = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(null == inputElement || !(inputElement instanceof ICATEntity)) {
			return null;
		}
		this.entity = (ICATEntity) inputElement;
		if(LOG.isDebugEnabled()) {
			LOG.debug("Getting content of " + entity.getEntityName() + " from " + entity.getServer().getServerURL());
		}
		try {
			SimpleICATClient client = ICATDataService.getInstance().getClient(this.entity.getServer());
			List<WrappedEntityBean> search = client.search(this.entity.getEntityName() + QUERY_SUFFIX);
			content = null == search ? null : search.toArray();
			return content;
		} catch (ICATClientException e) {
			LOG.error("Unable to load entity content for entity " + entity.getEntityName(), e);
			return null;
		}
	}

}

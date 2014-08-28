package fr.esrf.icat.manager.core.icatserver;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class EntityListProposalContentProvider implements IContentProposalProvider {

	private final static Logger LOG = LoggerFactory.getLogger(EntityListProposalContentProvider.class);

	private SimpleICATClient client;
	private String entityName;
	private WrappedEntityBean initialBean;
	private EntityLabelProvider lblprovider;
	private List<WrappedEntityBean> currentItems;
	
	public EntityListProposalContentProvider(SimpleICATClient client, String simpleName, WrappedEntityBean initialValue) {
		super();
		this.client = client;
		this.entityName = simpleName;
		this.initialBean = initialValue;
		this.lblprovider = new EntityLabelProvider();
		this.currentItems = new LinkedList<>();
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		currentItems.clear();
		if(null != initialBean) {
			currentItems.add(initialBean);
		}
		try {
			currentItems.addAll(client.search(makeSearchString(contents)));
		} catch (ICATClientException e) {
			LOG.error("Unable to load entity content for entity " + entityName, e);
		}
		IContentProposal[] props = new IContentProposal[currentItems.size()];
		String[] items = new String[currentItems.size()];
		int i = 0;
		for (WrappedEntityBean bean : currentItems) {
			final String text = lblprovider.getText(bean);
			props[i] = new ContentProposal(text);
			items[i] = text;
			i++;
		}
		return props;
	}

	public String[] getInitialItems() {
		currentItems.clear();
		if(null != initialBean) {
			currentItems.add(initialBean);
		}
		try {
			currentItems.addAll(client.search(makeSearchString(null)));
		} catch (ICATClientException e) {
			LOG.error("Unable to load entity content for entity " + entityName, e);
		}
		return getCurrentItems();
	}
	
	private String makeSearchString(String contents) {
		StringBuilder query = new StringBuilder();
		query.append("0,50 ");
		query.append(entityName);
		query.append(" ORDER BY id DESC");
		if(null != contents && !contents.isEmpty()) {
			query.append(" [name LIKE '%");
			query.append(contents);
			query.append("%']");
		}
		LOG.debug(query.toString());
		return query.toString();
	}

	public String[] getCurrentItems(){
		final String[] items = new String[currentItems.size()];
		int i = 0;
		for (WrappedEntityBean bean : currentItems) {
			items[i++] = lblprovider.getText(bean);
		}
		return items;
	}
	
	public Object[] getCurrentObjects() {
		return currentItems.toArray();
	}

}

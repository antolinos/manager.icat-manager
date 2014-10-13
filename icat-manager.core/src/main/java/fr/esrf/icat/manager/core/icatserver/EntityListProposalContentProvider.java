package fr.esrf.icat.manager.core.icatserver;

/*
 * #%L
 * icat-manager :: core
 * %%
 * Copyright (C) 2014 ESRF - The European Synchrotron
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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

	public final static String INITIAL_FILTER = "(max 50)";
	
	private SimpleICATClient client;
	private String entityName;
	private WrappedEntityBean initialBean;
	private EntityLabelProvider lblprovider;
	private List<WrappedEntityBean> currentItems;
	private boolean hasName = false;
	private boolean hasFullName = false;
	private boolean nameChecked = false;
	private String currentText;
	
	public EntityListProposalContentProvider(SimpleICATClient client, String simpleName, WrappedEntityBean initialValue) {
		super();
		this.client = client;
		this.entityName = simpleName;
		this.initialBean = initialValue;
		this.lblprovider = new EntityLabelProvider();
		this.currentItems = new LinkedList<>();
		if(null != this.initialBean) {
			this.hasName = this.initialBean.exists(ICATEntity.NAME_FIELD);
			this.hasFullName = this.initialBean.exists(ICATEntity.FULLNAME_FIELD);
			this.nameChecked = true;
		}
		
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {
		currentItems.clear();
		if(null != initialBean) {
			currentItems.add(initialBean);
		}
		currentText = contents;
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
		if(!nameChecked && currentItems.size() > 0) {
			final WrappedEntityBean w = currentItems.get(0);
			hasName = w.exists(ICATEntity.NAME_FIELD);
			hasFullName = w.exists(ICATEntity.FULLNAME_FIELD);
			nameChecked = true;
		}
		return getCurrentItems();
	}
	
	private String makeSearchString(String contents) {
		StringBuilder query = new StringBuilder();
		query.append("0,50 ");
		query.append(entityName);
		query.append(" ORDER BY id DESC");
		if(null != contents && !contents.isEmpty()) {
			if(hasName) {
				query.append(" [name LIKE '%");
				query.append(contents);
				query.append("%'");
				if(hasFullName) {
					query.append(" OR fullName LIKE '%");
					query.append(contents);
					query.append("%'");
				}
				query.append("]");
			} else if(hasFullName) {
				query.append(" [fullName LIKE '%");
				query.append(contents);
				query.append("%']");
			} else {
				try {
					Integer.parseInt(contents);
					query.append(" [id = ");
					query.append(contents);
					query.append("]");
				} catch (NumberFormatException e) {
					//pass
				}
			}
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

	public String getCurrentFilter() {
		StringBuilder sb = new StringBuilder();
		if(null != currentText && !currentText.isEmpty()) {
			if(hasName) {
				sb.append("like '");
				sb.append(currentText);
				sb.append("' ");
			} else {
				sb.append("id:");
				sb.append(currentText);
				sb.append(" ");
			}
		}
		sb.append(INITIAL_FILTER);
		return sb.toString();
	}

}

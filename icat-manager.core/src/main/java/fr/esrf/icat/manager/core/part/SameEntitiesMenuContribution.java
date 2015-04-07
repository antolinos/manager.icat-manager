 
package fr.esrf.icat.manager.core.part;

/*
 * #%L
 * icat-manager :: core
 * %%
 * Copyright (C) 2014 - 2015 ESRF - The European Synchrotron
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


import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MCommandsFactory;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;

public class SameEntitiesMenuContribution {
	
	private static final String ICAT_MANAGER_CORE_COMMAND_OPENENTITY = "icat-manager.core.command.openentity";

	private final static Logger LOG = LoggerFactory.getLogger(SameEntitiesMenuContribution.class);

	private final static SimpleDateFormat QUERY_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	@Inject
    protected MApplication application;

	@AboutToShow
	public void aboutToShow(
			final List<MMenuElement> items,
			final @Named(IServiceConstants.ACTIVE_PART) MPart activePart,
			final @Named(IServiceConstants.ACTIVE_SELECTION)@Optional  List<WrappedEntityBean> selection) {
		
		if(null == selection || selection.isEmpty()) {
			return;
		}
		
	    MCommand command = null;
	    for(MCommand c : application.getCommands()) {
	    	if(ICAT_MANAGER_CORE_COMMAND_OPENENTITY.equals(c.getElementId())) {
	    		command = c;
	    		break;
	    	}
	    }
	    
	    if(null == command) {
	    	LOG.error("Command {} not found", ICAT_MANAGER_CORE_COMMAND_OPENENTITY);
	    	return;
	    }

		DataPart part;
		if(activePart instanceof DataPart) {
			part = (DataPart) activePart;
		} else {
			part = (DataPart) activePart.getObject();
		}
		
	    final WrappedEntityBean firstEntityBean = selection.get(0);
		final List<String> fields = new LinkedList<>(firstEntityBean.getMutableFields());
		fields.removeAll(firstEntityBean.getEntityFields());
		for(String fieldName: fields) {
			String entityFilter = makeFieldFilter(selection, fieldName);
			if(null == entityFilter) {
				continue;
			}
			MHandledMenuItem dynamicItem = MMenuFactory.INSTANCE.createHandledMenuItem();
			dynamicItem.setCommand(command);
			dynamicItem.setLabel(StringUtils.capitalize(fieldName));
		    MParameter p = MCommandsFactory.INSTANCE.createParameter();
		    p.setName("icat-manager.core.commandparameter.filter");
			p.setValue(entityFilter);
		    dynamicItem.getParameters().add(p);
		    MParameter pEntity = MCommandsFactory.INSTANCE.createParameter();
		    pEntity.setName("icat-manager.core.commandparameter.entity");
		    pEntity.setValue(firstEntityBean.getEntityName());
		    dynamicItem.getParameters().add(pEntity);
		    MParameter pServer = MCommandsFactory.INSTANCE.createParameter();
		    pServer.setName("icat-manager.core.commandparameter.server");
		    pServer.setValue(part.getEntity().getServer().getServerURL());
		    dynamicItem.getParameters().add(pServer);
		    dynamicItem.setEnabled(true);
		    dynamicItem.setToBeRendered(true);
		    dynamicItem.setVisible(true);
		    items.add(dynamicItem);
		}
		
		if(items.size() > 0) {
			final MMenuSeparator separator = MMenuFactory.INSTANCE.createMenuSeparator();
			separator.setToBeRendered(true);
			separator.setVisible(true);
			items.add(separator);
		}
		for(String fieldName : firstEntityBean.getEntityFields()) {
		    final String entityName = firstEntityBean.getReturnType(fieldName).getSimpleName();
			String entityFilter = makeFKEntityFilter(selection, fieldName);
			if(null == entityFilter) {
				continue;
			}
			final MHandledMenuItem dynamicItem = MMenuFactory.INSTANCE.createHandledMenuItem();
			dynamicItem.setCommand(command);
			dynamicItem.setLabel(entityName);
		    MParameter p = MCommandsFactory.INSTANCE.createParameter();
		    p.setName("icat-manager.core.commandparameter.filter");
			p.setValue(entityFilter);
		    dynamicItem.getParameters().add(p);
		    MParameter pEntity = MCommandsFactory.INSTANCE.createParameter();
		    pEntity.setName("icat-manager.core.commandparameter.entity");
		    pEntity.setValue(firstEntityBean.getEntityName());
		    dynamicItem.getParameters().add(pEntity);
		    MParameter pServer = MCommandsFactory.INSTANCE.createParameter();
		    pServer.setName("icat-manager.core.commandparameter.server");
		    pServer.setValue(part.getEntity().getServer().getServerURL());
		    dynamicItem.getParameters().add(pServer);
		    dynamicItem.setEnabled(true);
		    dynamicItem.setToBeRendered(true);
		    dynamicItem.setVisible(true);
		    items.add(dynamicItem);
		}
	}

	private String makeFieldFilter(final List<WrappedEntityBean> selection, final String fieldName) {
		final List<Object> values = new LinkedList<>();
		for(WrappedEntityBean bean : selection) {
			try {
				final Object v = bean.get(fieldName);
				if(null != v) {
					values.add(v);
				}
			} catch (Exception e) {
				String simpleName = null;
				try {
					simpleName = bean.getEntityName();
					LOG.error("Error getting field {} from {}", fieldName, simpleName, e);
				} catch (IllegalArgumentException e1) {
					LOG.error("Error getting field {} from {}: {}", fieldName, simpleName, "ERROR getting name: " + e1.getMessage(), e);
				}
			}
		}
		if(values.size() == 0) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for(Object v : values) {
			if(first) {
				first = false;
			} else {
				b.append(" OR ");
			}
			if(v instanceof String) {
				b.append("(");
				b.append(fieldName);
				b.append("=");
				b.append("'");
				b.append(v);
				b.append("'");
				b.append(")");
			} else if (v instanceof Boolean) {
				b.append("(");
				b.append(fieldName);
				b.append("=");
				b.append(v.toString().toUpperCase());
				b.append(")");
			} else if (v instanceof XMLGregorianCalendar) {
				final GregorianCalendar calendar = ((XMLGregorianCalendar)v).toGregorianCalendar();
				QUERY_DATE_FORMAT.setTimeZone(calendar.getTimeZone());
				b.append("(");
				b.append(fieldName);
				b.append(">");
				b.append("{ts ");
				b.append(QUERY_DATE_FORMAT.format(calendar.getTime()));
				b.append("}");
				b.append(" AND ");
				b.append(fieldName);
				b.append("<");
				b.append("{ts ");
				calendar.roll(Calendar.SECOND, true);
				b.append(QUERY_DATE_FORMAT.format(calendar.getTime()));
				b.append("}");
				b.append(")");
			} else {
				b.append("(");
				b.append(fieldName);
				b.append("=");
				b.append(v);
				b.append(")");
			}
		}
		return b.toString();
	}

	private String makeFKEntityFilter(final List<WrappedEntityBean> selection, final String fieldName) {
		final Set<Long> idSet = new HashSet<>();
		for(WrappedEntityBean bean : selection) {
			WrappedEntityBean o = null;
			try {
				o = (WrappedEntityBean) bean.get(fieldName);
			} catch (Exception e) {
				try {
					LOG.error("Error getting entity {} from bean {}[{}]", fieldName,
							bean.getEntityName(), bean.get(ICATEntity.ID_FIELD).toString(), e);
				} catch (Exception e1) {
					LOG.error("Error getting entity {} from bean {}[{}]", fieldName, 
							bean.getEntityName(), "ERROR getting ID: " + e1.getMessage(), e);
				}
			}
			if(null != o) {
				try {
					idSet.add((Long) o.get(ICATEntity.ID_FIELD));
				} catch (Exception e) {
					String simpleName = null;
					try {
						simpleName = o.getEntityName();
						LOG.error("Error getting id from {} {}", simpleName, o.get(ICATEntity.NAME_FIELD), e);
					} catch (NoSuchMethodException | IllegalAccessException
							| IllegalArgumentException | InvocationTargetException e1) {
						LOG.error("Error getting id from {} {}", simpleName, "ERROR getting name: " + e1.getMessage(), e);
					}
				}
			}
		}
		if(idSet.size() == 0) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		b.append(fieldName);
		b.append(".id IN(");
		b.append(StringUtils.join(idSet, ','));
		b.append(")");
		return b.toString();
	}
		
}
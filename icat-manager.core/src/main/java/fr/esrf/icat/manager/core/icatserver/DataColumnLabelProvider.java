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


import java.util.Collection;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class DataColumnLabelProvider extends ColumnLabelProvider {

	private final static Logger LOG = LoggerFactory.getLogger(DataColumnLabelProvider.class);

	private static final String STRING_NONE = "None";
	private static final String READ_ERROR = "READ ERROR";
	private static final String STRING_ENTITIES = " Entities";

	private final String field;

	public DataColumnLabelProvider(String field) {
		super();
		this.field = field;
	}

	@Override
	public String getText(Object element) {
		try {
			final WrappedEntityBean wrapped = (WrappedEntityBean)element;
			final Object object = wrapped.get(field);
			if(null == object) {
				return ICATEntity.EMPTY_STRING;
			}
			// Associations
			if(wrapped.isAssociation(field)) {
				Collection<?> col = (Collection<?>) object;
				if(col.isEmpty()) {
					return STRING_NONE;
				}
				return col.size() + STRING_ENTITIES;
			}
			// Entity
			if(wrapped.isEntity(field)) {
				WrappedEntityBean entity = (WrappedEntityBean) object;
				if(entity.exists(ICATEntity.NAME_FIELD)) {
					return entity.get(ICATEntity.NAME_FIELD).toString();
				}
				return entity.get(ICATEntity.ID_FIELD).toString();
			}
			// all others
			return object.toString();
		} catch (Exception e) {
			LOG.error("Unable to fetch value of " + field, e);
			return READ_ERROR;
		}
	}

	@Override
	public Color getBackground(Object element) {
		return ((WrappedEntityBean)element).isImmutable(field) ?
				Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND) : 
				super.getBackground(element); 
	}
}
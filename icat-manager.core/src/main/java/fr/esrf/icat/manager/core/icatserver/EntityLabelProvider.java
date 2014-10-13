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


import org.eclipse.jface.viewers.LabelProvider;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;

public class EntityLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if(null == element || !(element instanceof WrappedEntityBean)) {
			return super.getText(element);
		}
		final WrappedEntityBean w = (WrappedEntityBean) element;
		final StringBuilder builder = new StringBuilder();
		boolean hasFullName = false;
		if(w.exists(ICATEntity.FULLNAME_FIELD)) {
			try {
				final Object name = w.get(ICATEntity.FULLNAME_FIELD);
				builder.append(name.toString());
				hasFullName = true;
			} catch (Exception e) {
			}
		}
		if(w.exists(ICATEntity.NAME_FIELD)) {
			try {
				final Object name = w.get(ICATEntity.NAME_FIELD);
				if(hasFullName) {
					builder.append(" (");
				}
				builder.append(name.toString());
				if(hasFullName) {
					builder.append(')');
				}
			} catch (Exception e) {
			}
		}
		if(w.exists(ICATEntity.ID_FIELD)) {
			try {
				final Object id = w.get(ICATEntity.ID_FIELD);
				if(builder.length() > 0) {
					builder.append(' ');
				}
				builder.append("[id: ");
				builder.append(id.toString());
				builder.append("]");
			} catch (Exception e) {
			}
		}
		return builder.toString();
	}


}

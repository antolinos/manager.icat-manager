package fr.esrf.icat.manager.core.icatserver;

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
	private static final String EMPTY_STRING = "";
	private static final String STRING_ENTITIES = " Entities";

	private static final String NAME_FIELD = "name";
	private static final String ID_FIELD = "id";
	

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
				return EMPTY_STRING;
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
				if(entity.exists(NAME_FIELD)) {
					return entity.get(NAME_FIELD).toString();
				}
				return entity.get(ID_FIELD).toString();
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
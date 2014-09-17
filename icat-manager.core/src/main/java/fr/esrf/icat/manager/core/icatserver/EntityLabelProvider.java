package fr.esrf.icat.manager.core.icatserver;

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
		boolean hasName = false;
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
				hasName = true;
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

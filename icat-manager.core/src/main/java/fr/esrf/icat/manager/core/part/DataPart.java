package fr.esrf.icat.manager.core.part;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.EntityContentProvider;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;

public class DataPart {

	private final static Logger LOG = LoggerFactory.getLogger(DataPart.class);

	private static final String STRING_NONE = "None";
	private static final String READ_ERROR = "READ ERROR";
	private static final String EMPTY_STRING = "";
	private static final String STRING_ENTITIES = " Entities";

	private static final String NAME_FIELD = "name";
	private static final String ID_FIELD = "id";
	
	private TableViewer viewer;
	private EntityContentProvider provider;

	@PostConstruct
	public void postConstruct(final Composite parent, final MContribution contrib) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		ICATEntity entity = (ICATEntity) contrib.getObject();
		provider = new EntityContentProvider(entity);
		createColumns(parent, viewer);
		viewer.setContentProvider(provider);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		viewer.setInput(entity);
		table.pack();
	}

	private void createColumns(Composite parent, TableViewer tViewer) {
		final WrappedEntityBean data = provider.getExampleData();
		if(null == data) {
			return;
		}
		List<String> fields = new LinkedList<>();
		fields.addAll(data.getMutableFields());
		fields.addAll(data.getImmutableFields());
		
		for(final String field : fields) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(field);
			column.getColumn().setWidth(100);
		    column.getColumn().setResizable(true);
		    column.getColumn().setMoveable(true);

			column.setLabelProvider(new ColumnLabelProvider(){
				@Override
				public String getText(Object element) {
					try {
						final WrappedEntityBean wrapped = (WrappedEntityBean)element;
						Object object = wrapped.get(field);
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
			});
		}
		
	}
}

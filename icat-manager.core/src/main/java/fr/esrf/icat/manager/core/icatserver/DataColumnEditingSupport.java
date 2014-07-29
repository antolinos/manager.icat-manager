package fr.esrf.icat.manager.core.icatserver;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.cdatetime.CDateTimeCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;

public class DataColumnEditingSupport extends EditingSupport {

	private final static Logger LOG = LoggerFactory.getLogger(DataColumnEditingSupport.class);

	private TableViewer viewer;
	private String field;
	private Class<?> clazz;
	private CellEditor editor;
	private ICATServer server;

	public DataColumnEditingSupport(TableViewer viewer, String field, Class<?> clazz, ICATServer icatServer) {
		super(viewer);
		this.viewer = viewer;
		this.field = field;
		this.clazz = clazz;
		this.server = icatServer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if(null == element || !(element instanceof WrappedEntityBean)) {
			return null;
		}
		if(null == editor) {
			WrappedEntityBean w = (WrappedEntityBean)element;
			if(w.isEntity(field)) {
				final ComboBoxViewerCellEditor combo = new ComboBoxViewerCellEditor(viewer.getTable());
				final ICATEntity entity = new ICATEntity(server, w.getReturnType(field).getSimpleName());
				combo.setContentProvider(new EntityContentProvider(entity));
				combo.setLabelProvider(new EntityLabelProvider());
				combo.setInput(entity);
				this.editor = combo;
			} else if(clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
				this.editor = new CheckboxCellEditor(viewer.getTable());
			} else if(Calendar.class.isAssignableFrom(clazz)
					|| Date.class.isAssignableFrom(clazz) ||
					XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
				this.editor = new CDateTimeCellEditor(viewer.getTable(), CDT.SPINNER | CDT.TAB_FIELDS | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM);
			} else {
				this.editor = new TextCellEditor(viewer.getTable());
			}
		}
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		if(null == element || !(element instanceof WrappedEntityBean)) {
			return false;
		}
		return ((WrappedEntityBean)element).isMutable(field);
	}

	@Override
	protected Object getValue(Object element) {
		if(null == element || !(element instanceof WrappedEntityBean)) {
			return null;
		}
		final WrappedEntityBean w = (WrappedEntityBean)element;
		try {
			Object o = w.get(field);
			if(null == o && w.getReturnType(field).equals(String.class)) {
				return ICATEntity.EMPTY_STRING;
			}
			if(o instanceof Calendar) {
				return ((Calendar)o).getTime();
			}
			if(o instanceof XMLGregorianCalendar) {
				return ((XMLGregorianCalendar)o).toGregorianCalendar().getTime();
			}
			return o;
		} catch (Exception e) {
			LOG.error("Error getting value of " + field + " from " + element.toString(), e);
			return null;
		}
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(null == element || !(element instanceof WrappedEntityBean)) {
			return;
		}
		WrappedEntityBean w = (WrappedEntityBean)element;
		Object o = value;
		if(value instanceof Date) {
			final Class<?> returnType = w.getReturnType(field);
			if(Calendar.class.isAssignableFrom(returnType)) {
				Calendar c = GregorianCalendar.getInstance();
				c.setTime((Date)value);
				o = c;
			} else if(returnType.equals(XMLGregorianCalendar.class)) {
				GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
				c.setTime((Date)value);
				try {
					o = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				} catch (DatatypeConfigurationException e) {
					LOG.error("Unable to create XMLGregorianCalendar", e);
					return;
				}
			}
		}
		try {
			w.set(field, o);
			ICATDataService.getInstance().getClient(server).update(w);
			viewer.update(element, null);
		} catch (Exception e) {
			LOG.error("Error setting " + field + " at " + o + " for " + element, e);
		}
	}

}

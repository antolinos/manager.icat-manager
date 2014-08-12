package fr.esrf.icat.manager.core.icatserver;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.jface.cdatetime.CDateTimeCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.part.DataPart;

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
			} else if(Enum.class.isAssignableFrom(clazz)){
				final ComboBoxViewerCellEditor combo = new ComboBoxViewerCellEditor(viewer.getTable());
				combo.setContentProvider(new ArrayContentProvider());
				combo.setLabelProvider(new EntityLabelProvider());
				combo.setInput(clazz.getEnumConstants());
				this.editor = combo;				
			} else if(clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
				this.editor = new CheckboxCellEditor(viewer.getTable());
			} else if(Calendar.class.isAssignableFrom(clazz)
					|| Date.class.isAssignableFrom(clazz) ||
					XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
				// workaround for focus lost from https://www.eclipse.org/forums/index.php/m/1403724/?srch=CDateTimeCellEditor#msg_1403724
				// note that using CDT.SIMPLE leads to a StackOverflowException
				this.editor = new CDateTimeCellEditor(viewer.getTable(),CDT.SPINNER | CDT.TAB_FIELDS | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM) {
					@Override
					protected boolean dependsOnExternalFocusListener() {
						return false;
					}
					@Override
					protected Control createControl(Composite parent) {
						final CDateTime cdt = (CDateTime) super.createControl(parent);
						cdt.addFocusListener(new FocusAdapter() {
						public void focusLost(FocusEvent e) {
						if (!cdt.isOpen()) {
							fireApplyEditorValue();
							deactivate();
						}
					}
					});
					return cdt;
					}
				};
			} else {
				this.editor = new TextCellEditor(viewer.getTable());
				if(Number.class.isAssignableFrom(clazz)) {
					final Color original = this.editor.getControl().getForeground();
					this.editor.setValidator(new ICellEditorValidator() {
						@Override
						public String isValid(Object value) {
							try {
								clazz.getMethod("valueOf", new Class<?>[]{String.class}).invoke(null, value);
								editor.getControl().setForeground(original);
								return null;
							} catch (Exception e) {
								editor.getControl().setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
								return "Please enter a number";
							}
						}
					});
				}
			}
		}
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		if(viewer.getData(DataPart.IN_PLACE_EDITING_PROPERTY_KEY).equals(Boolean.FALSE)
				|| null == element
				|| !(element instanceof WrappedEntityBean)) {
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
			if(editor instanceof TextCellEditor) {
				if(null == o) {
					return ICATEntity.EMPTY_STRING;
				}
				return o.toString();
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
		} else if(Number.class.isAssignableFrom(clazz)) {
			try {
				o = clazz.getMethod("valueOf", new Class<?>[]{String.class}).invoke(null, value);
			} catch (Exception e) {
				LOG.error("Error setting " + field + " to " + value, e);
				o = null;
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

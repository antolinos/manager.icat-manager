package fr.esrf.icat.manager.core.part;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.EntityLabelProvider;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;

public class EntityEditDialog extends Dialog {

	private final static Logger LOG = LoggerFactory.getLogger(EntityEditDialog.class);
	private WrappedEntityBean entity;
	private SimpleICATClient client;
	
	public EntityEditDialog(final Shell parentShell, final WrappedEntityBean entity, final SimpleICATClient client) {
		super(parentShell);
		this.entity = entity;
		this.client = client;
	}

	public WrappedEntityBean getEntity() {
		return entity;
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
	    GridLayout layout = new GridLayout(2, false);
	    layout.marginRight = 5;
	    layout.marginLeft = 10;
	    container.setLayout(layout);
	    
	    for(final String field : entity.getMutableFields()) {
		    Label lblAuthn = new Label(container, SWT.NONE);
		    lblAuthn.setText(StringUtils.capitalize(field) + ":");
		    final Class<?> clazz = entity.getReturnType(field);
		    Object initialValue = null;
		    try {
				initialValue = entity.get(field);
			} catch (Exception e) {
				LOG.error("Error getting initial value for " + field);
			}

			if(entity.isEntity(field)) {
				final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
				combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				final WrappedEntityBean[] beans;
				Object initialEntityID = null;
				try {
					beans = client.search(StringUtils.capitalize(field)).toArray(new WrappedEntityBean[0]);
				} catch (ICATClientException e) {
					throw new IllegalStateException("Error creating dialog", e);
				}
				try {
					if(initialValue != null) {
						initialEntityID = ((WrappedEntityBean)initialValue).get(ICATEntity.ID_FIELD);
					}
				} catch(Exception e){
					LOG.error("Error getting " + ICATEntity.ID_FIELD, e);
				}
				String[] values = new String[beans.length];
				EntityLabelProvider lblprovider = new EntityLabelProvider();
				int selected = -1;
				for(int i = 0; i < beans.length; i++) {
					values[i] = lblprovider.getText(beans[i]);
					Object id = null;
					try {
						id = beans[i].get(ICATEntity.ID_FIELD);
					} catch (Exception e) {
						LOG.error("Error getting " + ICATEntity.ID_FIELD, e);
					}
					if(initialEntityID != null && id != null && id.equals(initialEntityID)) {
						selected = i;
					}
				}
				combo.setItems(values);
				if(selected >= 0) {
					combo.select(selected);
				}
				combo.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						Object value = beans[combo.getSelectionIndex()];
						try {
							entity.set(field, value);
						} catch (Exception e1) {
							LOG.error("Error setting " + field + " to " + value);
						}
					}
				});
			} else if(Enum.class.isAssignableFrom(clazz)){
				final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
				final Object[] c = clazz.getEnumConstants();
				String[] s = new String[c.length];
				int selected = -1;
				for(int i = 0; i < c.length; i++) {
					s[i] = c[i].toString();
					if(initialValue != null && c[i].equals(initialValue)) {
						selected = i;
					}
				}
				combo.setItems(s);
				if(selected >= 0) {
					combo.select(selected);
				}
				combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				combo.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						Object value = c[combo.getSelectionIndex()];
						try {
							entity.set(field, value);
						} catch (Exception e1) {
							LOG.error("Error setting " + field + " to " + value);
						}
					}
				});
				
			} else if(clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
				final Button btn = new  Button(container, SWT.CHECK);
				btn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
				if(null == initialValue) {
					try {
						entity.set(field, Boolean.FALSE);
					} catch (Exception e) {
						LOG.error("Error setting " + field + " to " + Boolean.FALSE);
					}
				} else {
					btn.setSelection((Boolean) initialValue);
				}
				btn.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						boolean value = btn.getSelection();
						try {
							entity.set(field, value);
						} catch (Exception e1) {
							LOG.error("Error setting " + field + " to " + value);
						}
					}
				});
			} else if(Calendar.class.isAssignableFrom(clazz)
					|| Date.class.isAssignableFrom(clazz) ||
					XMLGregorianCalendar.class.isAssignableFrom(clazz)) {
				
				final CDateTime cdt = new CDateTime(container,  CDT.BORDER | CDT.SPINNER | CDT.TAB_FIELDS | CDT.DATE_MEDIUM | CDT.TIME_MEDIUM);
				cdt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				if(null != initialValue) {
					Date initialDate = null;
					if(initialValue instanceof Calendar) {
						initialDate = ((Calendar)initialValue).getTime();
					} else if(initialValue instanceof XMLGregorianCalendar) {
						initialDate = ((XMLGregorianCalendar)initialValue).toGregorianCalendar().getTime();
					} else if(initialValue instanceof Date) {
						initialDate = (Date) initialValue;
					}
					cdt.setSelection(initialDate);
				}

				cdt.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						Date value = cdt.getSelection();
						Object o = value;
						if(Calendar.class.isAssignableFrom(clazz)) {
							Calendar c = GregorianCalendar.getInstance();
							c.setTime((Date)value);
							o = c;
						} else if(clazz.equals(XMLGregorianCalendar.class)) {
							GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
							c.setTime((Date)value);
							try {
								o = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
							} catch (DatatypeConfigurationException e1) {
								LOG.error("Unable to create XMLGregorianCalendar", e1);
								return;
							}
						}
						try {
							entity.set(field, o);
						} catch (Exception e1) {
							LOG.error("Error setting " + field + " to " + o);
						}
					}
				});
			} else {
				final Text text = new Text(container, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				if(null == initialValue) {
					try {
						entity.set(field, ICATEntity.EMPTY_STRING);
					} catch (Exception e) {
						LOG.error("Error setting " + field + " to EMPTY_STRING");
					}
				} else {
					text.setText((String) initialValue);
				}
				text.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent e) {
						String value = text.getText();
						if(null == value) {
							value =  ICATEntity.EMPTY_STRING;
						}
						try {
							entity.set(field, value);
						} catch (Exception e1) {
							LOG.error("Error setting " + field + " to " + value);
						}
					}
				});
			}
		    
	    }
		return container;
	}

}
package fr.esrf.icat.manager.core.part;

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


import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.sf.swtaddons.autocomplete.AutocompleteContentProposalProvider;
import net.sf.swtaddons.autocomplete.AutocompleteSelectorContentProposalProvider;
import net.sf.swtaddons.autocomplete.combo.AutocompleteCombo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.SimpleICATClient;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.EntityLabelProvider;
import fr.esrf.icat.manager.core.icatserver.EntityListProposalContentProvider;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;

public class EntityEditDialog extends Dialog {

	private final static Logger LOG = LoggerFactory.getLogger(EntityEditDialog.class);
	
	private final static char[] DEFAULT_ACTIVATION_CHARS = ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_12345679890" + SWT.BS + SWT.DEL).toCharArray();
	private final static KeyStroke DEFAULT_KEYSTROKE = KeyStroke.getInstance(SWT.CTRL, ' ');
	public final static Image WARNING_IMAGE;
	public final static Image ERROR_IMAGE;
	public final static Image MULTI_IMAGE;

	static {
	    Bundle bundle = FrameworkUtil.getBundle(EntityEditDialog.class);
	    URL url = FileLocator.find(bundle, new Path("icons/warning.gif"), null);
	    WARNING_IMAGE = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/fail.gif"), null);
	    ERROR_IMAGE = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/morph2.gif"), null);
	    MULTI_IMAGE = ImageDescriptor.createFromURL(url).createImage();
	}
	
	private List<WrappedEntityBean> entities;
	private final boolean isSingle;
	private SimpleICATClient client;
	private Map<String, Pair<Object[], Combo>> comboMapping;
	private Map<String, Object> fieldValues;
	
	public EntityEditDialog(final Shell parentShell, final List<WrappedEntityBean> beans, final SimpleICATClient client) {
		super(parentShell);
		this.entities = beans;
		this.client = client;
		this.isSingle = (beans.size() == 1);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		StringBuilder b = new StringBuilder();
		b.append("Editing ");
		if(isSingle) {
			WrappedEntityBean bean = entities.get(0);
			b.append(bean.getWrapped().getClass().getSimpleName());
			b.append(" ");
			b.append(new EntityLabelProvider().getText(bean));
		} else if (entities.size() > 0) {
			WrappedEntityBean bean = entities.get(0);
			b.append(entities.size());
			b.append(" ");
			b.append(bean.getWrapped().getClass().getSimpleName());
			b.append('s'); // plural
		}
		shell.setText(b.toString());
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
	    GridLayout layout = new GridLayout(3, false);
	    layout.marginRight = 5;
	    layout.marginLeft = 10;
	    container.setLayout(layout);
	    comboMapping = new HashMap<>();
	    fieldValues = new HashMap<>();
	    // we are sure we have at least one entity, use the 1st one for anything general (field types, etc.)
	    final WrappedEntityBean firstEntity = entities.get(0);
	    for(final String field : firstEntity.getMutableFields()) {
		    Label lblAuthn = new Label(container, SWT.NONE);
		    lblAuthn.setText(StringUtils.capitalize(field) + ":");
		    final Button checkEdit = new Button(container, SWT.CHECK);
		    checkEdit.setEnabled(false);
		    checkEdit.setVisible(false);
		    final Class<?> clazz = firstEntity.getReturnType(field);
		    Object initialValue = null;
		    boolean notSet = true;
		    for(WrappedEntityBean entity : entities) {
			    try {
					Object value = entity.get(field);
			    	if(notSet) {
			    		initialValue = value;
			    		notSet = false;
			    	} else if((null == value && null != initialValue) || (null != value && !value.equals(initialValue))) {
			    		initialValue = null;
			    		checkEdit.setImage(MULTI_IMAGE);
			    		checkEdit.setSelection(false);
			    		checkEdit.setEnabled(true);
			    		checkEdit.setVisible(true);
			    		break;
			    	}
				} catch (Exception e) {
					LOG.error("Error getting initial value for " + field, e);
				}
		    }
		    final boolean hasInitialValue = initialValue != null;
			if(firstEntity.isEntity(field)) {
				final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
				new Label(container, SWT.NONE); //empty left label
				final Label label = new Label(container, SWT.RIGHT);
				label.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
				label.setImage(WARNING_IMAGE);
				final Label warningLabel = new Label(container, SWT.LEFT);
				warningLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false));
				combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				final EntityListProposalContentProvider proposalProvider = new EntityListProposalContentProvider(
						client, firstEntity.getReturnType(field).getSimpleName(), initialValue,
						label, warningLabel, container);
				warningLabel.setText(proposalProvider.getCurrentFilter());
				final ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(
						combo, new ComboContentAdapter(), proposalProvider, DEFAULT_KEYSTROKE, DEFAULT_ACTIVATION_CHARS);
				contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
				contentProposalAdapter.setPropagateKeys(true);
				contentProposalAdapter.setAutoActivationDelay(1000);
				contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {
					@Override
					public void proposalPopupOpened(ContentProposalAdapter adapter) {
					}
					@Override
					public void proposalPopupClosed(ContentProposalAdapter adapter) {
						// when the proposal popup closes we set the content of the combo to the proposals
						final String[] currentItems = proposalProvider.getCurrentItems();
						if(currentItems != null && currentItems.length > 0) {
							combo.setItems(currentItems);
						}
						final String currentText = proposalProvider.getCurrentText();
						final int caretPosition = proposalProvider.getCaretPosition();
						combo.setText(currentText);
						combo.setSelection(new Point(caretPosition, caretPosition));
					}
				});
				combo.setItems(proposalProvider.getInitialItems());
				if(hasInitialValue) {
					combo.select(0);
				}
				comboMapping.put(field, new ImmutablePair<Object[], Combo>(new Object[]{proposalProvider}, combo));
				if(checkEdit.isEnabled()) {
					combo.setEnabled(false);
					checkEdit.addSelectionListener(new SelectionAdapter(){
						@Override
						public void widgetSelected(SelectionEvent e) {
							combo.setEnabled(checkEdit.getSelection());
						}
					});
				}
				
			} else if(Enum.class.isAssignableFrom(clazz)){
				final Combo combo = new Combo(container, SWT.DROP_DOWN | SWT.BORDER);
				final Object[] c = clazz.getEnumConstants();
				final String[] s = new String[c.length];
				int selected = -1;
				for(int i = 0; i < c.length; i++) {
					s[i] = c[i].toString();
					if(initialValue != null && c[i].equals(initialValue)) {
						selected = i;
					}
				}
				// replacement for AutocompleteComboSelector to avoid selecting the 1st value in the combo when
				// no proposal is accepted (or field is emptied)
				new AutocompleteCombo(combo){
					@Override
					protected AutocompleteContentProposalProvider getContentProposalProvider(String[] proposals) {
						return new AutocompleteSelectorContentProposalProvider(proposals, this.combo);
					}
					
				};
				combo.setItems(s);
				if(selected >= 0) {
					combo.select(selected);
				}
				combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				comboMapping.put(field, new ImmutablePair<Object[], Combo>(c, combo));
				if(checkEdit.isEnabled()) {
					combo.setEnabled(false);
					checkEdit.addSelectionListener(new SelectionAdapter(){
						@Override
						public void widgetSelected(SelectionEvent e) {
							combo.setEnabled(checkEdit.getSelection());
						}
					});
				}
				
			} else if(clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
				final Button btn = new  Button(container, SWT.CHECK);
				btn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
				if(null == initialValue) {
					if(isSingle) {
						try {
							firstEntity.set(field, Boolean.FALSE);
						} catch (Exception e) {
							LOG.error("Error setting " + field + " to " + Boolean.FALSE);
						}
					}
				} else {
					btn.setSelection((Boolean) initialValue);
				}
				btn.addSelectionListener(new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						boolean value = btn.getSelection();
						fieldValues.put(field, value);
					}
				});
				if (checkEdit.isEnabled()) {
					btn.setEnabled(false);
					checkEdit.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							final boolean selected = checkEdit.getSelection();
							btn.setEnabled(selected);
							if(selected) {
								fieldValues.put(field, btn.getSelection());
							} else {
								fieldValues.remove(field);
							}
						}
					});
				}
				
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
						fieldValues.put(field, makeCorrectDateValue(clazz, cdt.getSelection()));
					}
				});
				if (checkEdit.isEnabled()) {
					cdt.setEnabled(false);
					checkEdit.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							final boolean selected = checkEdit.getSelection();
							cdt.setEnabled(selected);
							if(selected) {
								fieldValues.put(field, makeCorrectDateValue(clazz, cdt.getSelection()));
							} else {
								fieldValues.remove(field);
							}
						}
					});
				}

				
			} else if(Number.class.isAssignableFrom(clazz)){
				final Text text = new Text(container, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				if(null != initialValue) {
					text.setText(initialValue.toString());
				}
				final Color original = text.getForeground();
				text.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent e) {
						text.setForeground(original);
						String value = text.getText();
						if(null == value) {
							value = ICATEntity.EMPTY_STRING;
						}
						final Object numVal = makeCorrectNumericValue(clazz, value);
						if(null == numVal) {
							text.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
						} else {
							fieldValues.put(field, numVal);
						}
					}
				});
				if (checkEdit.isEnabled()) {
					text.setEnabled(false);
					checkEdit.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							final boolean selected = checkEdit.getSelection();
							text.setEnabled(selected);
							if(selected) {
								fieldValues.put(field, makeCorrectNumericValue(clazz, text.getText()));
							} else {
								fieldValues.remove(field);
							}
						}
					});
				}
				
			} else { // Assumes String
				final Text text = new Text(container, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				if(null == initialValue) {
					if(isSingle) {
						try {
							firstEntity.set(field, ICATEntity.EMPTY_STRING);
						} catch (Exception e) {
							LOG.error("Error setting " + field + " to EMPTY_STRING");
						}
					}
				} else {
					text.setText(initialValue.toString());
				}
				text.addModifyListener(new ModifyListener(){
					@Override
					public void modifyText(ModifyEvent e) {
						String value = text.getText();
						if(null == value) {
							value = ICATEntity.EMPTY_STRING;
						}
						fieldValues.put(field, value);
					}
				});
				if (checkEdit.isEnabled()) {
					text.setEnabled(false);
					checkEdit.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							final boolean selected = checkEdit.getSelection();
							text.setEnabled(selected);
							if(selected) {
								fieldValues.put(field, text.getText());
							} else {
								fieldValues.remove(field);
							}
						}
					});
				}
			}		    
	    }
		return container;
	}

	@Override
	protected void cancelPressed() {
		comboMapping = null;
		fieldValues = null;
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		for(Entry<String, Pair<Object[], Combo>> entry : comboMapping.entrySet()) {
			final Pair<Object[], Combo> pair = entry.getValue();
			final Combo combo = pair.getRight();
			// added: skip value for disabled combos
			// needed to not apply changed values on multi value field
			// when disabled after change
			if(!combo.isEnabled()) {
				continue;
			}
			final String field = entry.getKey();
			final Object[] left = pair.getLeft();
			final Object[] objects = left[0] instanceof EntityListProposalContentProvider ?
					((EntityListProposalContentProvider)left[0]).getCurrentObjects(): left;
			final int index = combo.indexOf(combo.getText());
			Object value = null;
			if(index >= 0) {
				value = objects[index];
			}
			for(WrappedEntityBean entity : entities) {
				try {
					entity.set(field, value);
				} catch (Exception e) {
					LOG.error("Error setting " + field + " to " + value, e);
				}
			}
		}
		for(Entry<String, Object> entry : fieldValues.entrySet()) {
			for(WrappedEntityBean entity : entities) {
				try {
					entity.set(entry.getKey(), entry.getValue());
				} catch (Exception e) {
					LOG.error("Error setting " + entry.getKey() + " to " + entry.getValue(), e);
				}
			}
		}
		comboMapping = null;
		fieldValues = null;
		super.okPressed();
	}

	private Object makeCorrectDateValue(final Class<?> clazz, final Date date) {
		// null date do not change
		if(null == date) return null;
		// calendar
		if(Calendar.class.isAssignableFrom(clazz)) {
			Calendar c = GregorianCalendar.getInstance();
			c.setTime(date);
			return c;
		}
		// xml calendar
		if(clazz.equals(XMLGregorianCalendar.class)) {
			GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
			c.setTime(date);
			try {
				return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			} catch (DatatypeConfigurationException e) {
				LOG.error("Unable to create XMLGregorianCalendar", e);
				return null;
			}
		}
		// anything else we do not convert
		return date;
	}
	
	private Object makeCorrectNumericValue(final Class<?> clazz, final String value) {
		if(null == clazz || null == value) return null;
		try {
			return clazz.getMethod("valueOf", new Class<?>[]{String.class}).invoke(null, value);
		} catch (Exception e) {
			LOG.error("Error getting {} value from {}", clazz.getSimpleName(), value);
			return null;
		}
	}

}

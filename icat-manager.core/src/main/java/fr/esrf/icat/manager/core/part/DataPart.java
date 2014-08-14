package fr.esrf.icat.manager.core.part;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.icat.client.ICATClientException;
import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.DataColumnEditingSupport;
import fr.esrf.icat.manager.core.icatserver.DataColumnLabelProvider;
import fr.esrf.icat.manager.core.icatserver.EntityContentProvider;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;

@SuppressWarnings("restriction")
public class DataPart {

	public static final String ICAT_MANAGER_MAINSTACK = "icat-manager.core.partstack.mainstack";
	public static final String DATA_PART_ELEMENT_HEADER = "icat-manager.core.part.data";
	public static final String DATA_PART_DESCRIPTOR = "icat-manager.core.partdescriptor.datapart";
	public static final String IN_PLACE_EDITING_PROPERTY_KEY = "inPlaceEditing";
	
	private final static Logger LOG = LoggerFactory.getLogger(DataPart.class);

	private final static Image IMAGE_UP;
	private final static Image IMAGE_DOWN;
	private final static Image IMAGE_NEXT;
	private final static Image IMAGE_PREVIOUS;
	
	static {
	    Bundle bundle = FrameworkUtil.getBundle(DataPart.class);
	    URL url = FileLocator.find(bundle, new Path("icons/up.gif"), null);
	    IMAGE_UP = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/down.gif"), null);
	    IMAGE_DOWN = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/next.gif"), null);
	    IMAGE_NEXT = ImageDescriptor.createFromURL(url).createImage();
	    url = FileLocator.find(bundle, new Path("icons/prev.gif"), null);
	    IMAGE_PREVIOUS = ImageDescriptor.createFromURL(url).createImage();
	}
	
	private TableViewer viewer;
	private ICATEntity entity;
	private Label paginationLabel;
	private EntityContentProvider contentProvider;
	private Button previousBtn;
	private Button nextBtn;

	@PostConstruct
	public void postConstruct(final Composite parent, final EMenuService menuService, 
			final MContribution contrib, final ESelectionService selectionService) {
		
		final GridLayout pageLayout = new GridLayout();
		pageLayout.marginWidth = 0;
		pageLayout.marginHeight = 0;
		pageLayout.verticalSpacing = 0;
		parent.setLayout(pageLayout);
		
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
		final RowLayout barLayout = new RowLayout();
		barLayout.center = true;
		barLayout.pack = true;
		barLayout.spacing = 10;
		top.setLayout(barLayout);
		
		paginationLabel = new Label(top, SWT.NONE);

		previousBtn = new Button(top, SWT.PUSH);
		previousBtn.setText("Previous");
		previousBtn.setImage(IMAGE_PREVIOUS);
		
		nextBtn = new Button(top, SWT.PUSH);
		nextBtn.setText("Next");
		nextBtn.setImage(IMAGE_NEXT);
		
		new Label(top, SWT.NONE).setText("Page size:");
		final Combo pageSizeCombo = new Combo(top, SWT.DROP_DOWN);
		pageSizeCombo.setItems(new String[]{"50", "100", "150", "200"});
		pageSizeCombo.select(0);
		pageSizeCombo.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				e.doit = e.character == SWT.BS || e.character == SWT.DEL || e.text.matches("\\d+");
			}
		});
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		contentProvider = new EntityContentProvider();
		viewer.setContentProvider(contentProvider);
		entity = (ICATEntity) contrib.getObject();

		pageSizeCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				contentProvider.setPageSize(new Integer(pageSizeCombo.getText()));
				refresh();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				contentProvider.setPageSize(new Integer(pageSizeCombo.getText()));
				refresh();
			}
		});
		previousBtn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				contentProvider.previousPage();
				refresh();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		nextBtn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				contentProvider.nextPage();
				refresh();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		viewer.setData(IN_PLACE_EDITING_PROPERTY_KEY, Boolean.FALSE);
		createColumns(parent);
		final Table table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		viewer.setInput(entity);
		paginationLabel.setText(contentProvider.getPaginationLabelText());
		nextBtn.setEnabled(!contentProvider.isLastPage());
		previousBtn.setEnabled(!contentProvider.isFirstPage());
	    // make selection available
	    viewer.addSelectionChangedListener(new ISelectionChangedListener() {
	    	  @Override
	    	  public void selectionChanged(SelectionChangedEvent event) {
	    	    IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
	    	    selectionService.setSelection(selection.getFirstElement());
	    	  }
    	}); 
	    // context menu
	    menuService.registerContextMenu(viewer.getControl(), "icat-manager.core.popupmenu.entity");
		table.pack();
	}

	private void createColumns(final Composite parent) {
		WrappedEntityBean data = null;
		try {
			data = ICATDataService.getInstance().getClient(entity.getServer()).create(entity.getEntityName());
		} catch (ICATClientException e) {
			LOG.error("Error creating " + entity.getEntityName(), e);
			return;
		}
		List<String> fields = new LinkedList<>();
		fields.addAll(data.getMutableFields());
//		fields.addAll(data.getAssociationFields());
		fields.addAll(data.getImmutableFields());
		
		for(final String field : fields) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			final TableColumn currentColumn = column.getColumn();
			currentColumn.setText(field);
			currentColumn.setWidth(100);
			currentColumn.setResizable(true);
			currentColumn.setMoveable(true);
			if(field.equals(contentProvider.getSortingField())) {
				switch(contentProvider.getSortingOrder()) {
					case SWT.UP:   currentColumn.setImage(IMAGE_UP); break;
					case SWT.DOWN: currentColumn.setImage(IMAGE_DOWN); break;
					default: currentColumn.setImage(null); break;
				}
			} else {
				currentColumn.setImage(null); 
			}
		    
		    column.getColumn().addSelectionListener(new SelectionListener(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					final TableColumn col = (TableColumn) e.getSource();
					final String sortingField = col.getText();
					DataPart.LOG.debug("Selected column " + sortingField);
					contentProvider.toggleSortingField(field);
					for(TableColumn column : viewer.getTable().getColumns()) {
						if(column.getText().equals(sortingField)) {
							switch(contentProvider.getSortingOrder()) {
								case SWT.UP:   column.setImage(IMAGE_UP); break;
								case SWT.DOWN: column.setImage(IMAGE_DOWN); break;
								default: column.setImage(null); break;
							}
						} else {
							column.setImage(null);
						}
					}
					refresh();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
		    	
		    });

			column.setLabelProvider(new DataColumnLabelProvider(field));
			if(data.isMutable(field)) {
				column.setEditingSupport(new DataColumnEditingSupport(viewer, field, data.getReturnType(field), entity.getServer()));
			}
		}
	}
	
	public ICATEntity getEntity() {
		return entity;
	}

	public void refresh() {
		viewer.refresh();
		paginationLabel.setText(contentProvider.getPaginationLabelText());
		previousBtn.setEnabled(!contentProvider.isFirstPage());
		nextBtn.setEnabled(!contentProvider.isLastPage());
		// this has to be called to avoid the label not resizing properly
		paginationLabel.getParent().getParent().layout();
	}
	
	public void toggleInPLaceEditing() {
		final Object value = viewer.getData(IN_PLACE_EDITING_PROPERTY_KEY);
		if(Boolean.FALSE.equals(value)) {
			viewer.setData(IN_PLACE_EDITING_PROPERTY_KEY, Boolean.TRUE);
		} else {
			viewer.setData(IN_PLACE_EDITING_PROPERTY_KEY, Boolean.FALSE);
		}
	}
	
}

package fr.esrf.icat.manager.core.part;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import fr.esrf.icat.client.wrapper.WrappedEntityBean;
import fr.esrf.icat.manager.core.icatserver.DataColumnEditingSupport;
import fr.esrf.icat.manager.core.icatserver.DataColumnLabelProvider;
import fr.esrf.icat.manager.core.icatserver.EntityContentProvider;
import fr.esrf.icat.manager.core.icatserver.ICATEntity;

@SuppressWarnings("restriction")
public class DataPart {

	public static final String ICAT_MANAGER_MAINSTACK = "icat-manager.core.partstack.mainstack";
	public static final String DATA_PART_ELEMENT_HEADER = "icat-manager.core.part.data";
	public static final String DATA_PART_DESCRIPTOR = "icat-manager.core.partdescriptor.datapart";

	private TableViewer viewer;
	private EntityContentProvider provider;
	private ICATEntity entity;

	@PostConstruct
	public void postConstruct(final Composite parent, final EMenuService menuService, 
			final MContribution contrib, final ESelectionService selectionService) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		entity = (ICATEntity) contrib.getObject();
		provider = new EntityContentProvider(entity);
		createColumns(parent, viewer, entity);
		viewer.setContentProvider(provider);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true); 
		viewer.setInput(entity);
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

	private void createColumns(Composite parent, TableViewer tViewer, ICATEntity entity) {
		final WrappedEntityBean data = provider.getExampleData();
		if(null == data) {
			return;
		}
		List<String> fields = new LinkedList<>();
		fields.addAll(data.getMutableFields());
//		fields.addAll(data.getAssociationFields());
		fields.addAll(data.getImmutableFields());
		
		for(final String field : fields) {
			TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
			column.getColumn().setText(field);
			column.getColumn().setWidth(100);
		    column.getColumn().setResizable(true);
		    column.getColumn().setMoveable(true);

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
		provider.loadContent();
		viewer.refresh();
	}
	
}

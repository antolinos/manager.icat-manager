package fr.esrf.icat.manager.core.icatserver;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

import fr.esrf.icat.manager.core.ICATDataService;

public class IcatServerLabelProvider extends StyledCellLabelProvider {

	private ICATDataService service = ICATDataService.getInstance();
	
	
	@Override
	public void update(ViewerCell cell) {
	      Object element = cell.getElement();
	      StyledString text = new StyledString();
	      if(element instanceof ICATServer) {
		      ICATServer icatServer = (ICATServer)element;
		      text.append(icatServer.getServerURL());
		      text.append(" (" + icatServer.getVersion() + ") ", StyledString.QUALIFIER_STYLER);
	      } else if(element instanceof ICATEntity){
	    	  ICATEntity entity = (ICATEntity)element;
	    	  text.append(entity.getEntityName());
		      text.append(" (" + service.getEntityCount(entity) + ") ", StyledString.COUNTER_STYLER);
	      } else {
	    	  text.append(element.toString());
	      }
	      cell.setText(text.toString());
	      cell.setStyleRanges(text.getStyleRanges());
	      super.update(cell);
	}

}

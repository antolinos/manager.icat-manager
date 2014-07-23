package fr.esrf.icat.manager.core.icatserver;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;

public class IcatServerLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
	      Object element = cell.getElement();
	      StyledString text = new StyledString();
	      if(element instanceof ICATServer) {
		      ICATServer icatServer = (ICATServer)element;
		      text.append(icatServer.getServerURL());
		      text.append(" (" + icatServer.getVersion() + ") ", StyledString.QUALIFIER_STYLER);
	      } else {
	    	  text.append(element.toString());
		      text.append(" (" + "1" + ") ", StyledString.COUNTER_STYLER);
	      }
	      cell.setText(text.toString());
	      cell.setStyleRanges(text.getStyleRanges());
	      super.update(cell);
	}

}

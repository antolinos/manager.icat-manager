package fr.esrf.icat.manager.core.handlers;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import fr.esrf.icat.manager.core.ICATDataService;
import fr.esrf.icat.manager.core.icatserver.ICATServer;

public class NewServerHandler {

	  private static final String SLASH = "/";

	@Execute
	  public void execute(final Shell shell) {
		  InputDialog dlg = new InputDialog(shell, "New ICAT server", "Enter the URL of the new ICAT server", "",
				  new IInputValidator() {
			  		private final UrlValidator validator = new UrlValidator(new String[]{"http","https"});
					@Override
					public String isValid(String newText) {
						if(null == newText || newText.isEmpty()) {
							return "Please provide an URL";
						}
						if(!validator.isValid(newText)) {
							return "Invalid URL";
						}
						// Extra validation to avoid issue with URL relative path
						if(StringUtils.countMatches(newText, SLASH) > 2 && !newText.endsWith(SLASH)) {
							return "Please set a terminal '/' on the URL";
						}
						return null;
					}
				});
		  if(dlg.open() == Window.OK) {
			  ICATDataService.getInstance().addServer(new ICATServer(dlg.getValue()));
		  }
	  }

}

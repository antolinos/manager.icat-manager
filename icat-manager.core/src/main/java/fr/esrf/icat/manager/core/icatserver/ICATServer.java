package fr.esrf.icat.manager.core.icatserver;

import fr.esrf.icat.client.ICATClient;

public class ICATServer {

	private String serverURL;
	
	private String version;
	
	private boolean accessible;
	
	private ICATClient client;

	public ICATServer(String serverURL) {
		super();
		this.serverURL = serverURL;
		this.accessible = false;
		this.version = "4.3.1";
		
	}
		
	public String getServerURL() {
		return serverURL;
	}

	public String getVersion() {
		return version;
	}

}

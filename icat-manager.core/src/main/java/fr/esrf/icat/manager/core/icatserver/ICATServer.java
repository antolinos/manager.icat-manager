package fr.esrf.icat.manager.core.icatserver;

public class ICATServer {

	private String serverURL;
	
	private String version;
	
	private boolean connected;
	
	public ICATServer(String serverURL) {
		super();
		this.serverURL = serverURL;
		this.version = null;
		this.connected = false; 
	}
		
	public String getServerURL() {
		return serverURL;
	}

	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isConnected(){
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

}

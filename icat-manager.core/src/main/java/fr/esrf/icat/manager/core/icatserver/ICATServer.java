package fr.esrf.icat.manager.core.icatserver;

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


public class ICATServer {

	private String serverURL;
	
	private String version;
	
	private boolean connected;
	
	private String lastAuthnMethod;
	
	private String lastUserName; 
	
	private ICATServer.Status status;
	
	public static enum Status {
		UNKNOWN,
		CONNECTED,
		FAILED
	}
	
	public ICATServer(String serverURL) {
		super();
		this.serverURL = serverURL;
		this.version = null;
		this.connected = false; 
		this.status = Status.UNKNOWN;
		this.lastAuthnMethod = "";
		this.lastUserName = "";
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

	public ICATServer.Status getStatus() {
		return status;
	}

	public void setStatus(ICATServer.Status status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		return serverURL.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ICATServer)) return false;
		ICATServer other = (ICATServer) obj;
		if (serverURL == null) {
			return other.serverURL == null;
		}
		return serverURL.equals(other.serverURL);
	}

	public String getLastAuthnMethod() {
		return lastAuthnMethod;
	}

	public void setLastAuthnMethod(String lastAuthnMethod) {
		this.lastAuthnMethod = lastAuthnMethod;
	}

	public String getLastUserName() {
		return lastUserName;
	}

	public void setLastUserName(String lastUserName) {
		this.lastUserName = lastUserName;
	}

}

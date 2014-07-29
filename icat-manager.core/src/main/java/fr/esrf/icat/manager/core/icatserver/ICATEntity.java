package fr.esrf.icat.manager.core.icatserver;

public class ICATEntity {

	public static final String NAME_FIELD = "name";
	public static final String ID_FIELD = "id";

	public static final String EMPTY_STRING = "";

	private ICATServer server;
	private String entityName;
	
	public ICATEntity(ICATServer server, String entityName) {
		super();
		this.server = server;
		this.entityName = entityName;
	}

	public ICATServer getServer() {
		return server;
	}

	public String getEntityName() {
		return entityName;
	}

}

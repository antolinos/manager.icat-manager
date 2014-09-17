package fr.esrf.icat.manager.core.icatserver;

public class ICATEntity {

	public static final String NAME_FIELD = "name";
	public static final String FULLNAME_FIELD = "fullName";
	public static final String ID_FIELD = "id";

	public static final String ENTITY_CONTEXT_KEY = "entity";
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityName == null) ? 0 : entityName.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ICATEntity)) return false;
		ICATEntity other = (ICATEntity) obj;
		if (entityName == null) {
			if (other.entityName != null)
				return false;
		} else if (!entityName.equals(other.entityName))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		return true;
	}

}

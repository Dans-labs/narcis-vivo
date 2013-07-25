package nl.knaw.dans.labs.narcisvivo.data;

public class Person {
	private final String name;
	private final String source;
	private final String uri;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	/**
	 * @param firstName
	 * @param lastName
	 * @param source
	 * @param uri
	 */
	public Person(String firstName, String lastName, String source, String uri) {
		StringBuffer name = new StringBuffer();
		name.append(firstName);
		name.append(" ").append(lastName);
		name.append(" (").append(source).append(")");

		this.name = name.toString();
		this.source = source;
		this.uri = uri;

	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @return
	 */
	public String getUri() {
		return uri;
	}
}

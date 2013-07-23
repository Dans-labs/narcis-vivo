package nl.knaw.dans.labs.narcisvivo.data;

public class Person {
	private final String name;
	private final String source;
	private final String uri;

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

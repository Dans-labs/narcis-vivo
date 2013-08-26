package nl.knaw.dans.labs.narcisvivo.util;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class InterestsQuery implements Runnable {
	// The name of the source to be queried
	private final String source;

	// The person and interest URI
	private String personURI = null;
	private String interestURI = null;

	// The set of results
	private final Set<String> results = new HashSet<String>();

	/**
	 * @param source
	 */
	public InterestsQuery(String source) {
		this.source = source;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// We have to be searching for something
		if (personURI == null && interestURI == null)
			return;

		// TODO If we look for persons interest in something, check the index
		// before sending the query to ensure there are some results

		// Compose the query
		StringBuffer query = new StringBuffer("select distinct ?r where {");
		if (personURI != null) {
			// We look for the interests of someone
			query.append("<").append(personURI).append(">");
			if (source.equals("isidore"))
				query.append("<http://xmlns.com/foaf/0.1/topic_interest> ?r.");
			else
				query.append("<http://vivoweb.org/ontology/core#hasResearchArea> ?r.");
		} else {
			// We look for the persons interested in something
			if (source.equals("isidore"))
				query.append("?r <http://xmlns.com/foaf/0.1/topic_interest> ");
			else
				query.append("?r <http://vivoweb.org/ontology/core#hasResearchArea> ");
			query.append("<").append(interestURI).append("> .");
		}
		query.append("}");

		// Get the target end point
		String endPoint = Parameters.getEndPoint(source);

		// Parameters for paginated query
		boolean newData = true;
		int offset = 0;

		while (newData) {
			// Compose query page
			StringBuffer queryPage = new StringBuffer(query);
			queryPage.append(" OFFSET ").append(offset).append("LIMIT 1000");

			// Execute the query
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					endPoint, queryPage.toString());
			qexec.setTimeout(0);
			ResultSet resultSet = qexec.execSelect();
			newData = resultSet.hasNext();

			// Add the results to the global results
			while (resultSet.hasNext()) {
				QuerySolution result = resultSet.next();
				String resource = result.get("r").toString();
				results.add(resource);
			}

			// Switch to next page
			offset += 1000;
		}
	}

	/**
	 * @return
	 */
	public String getPersonURI() {
		return personURI;
	}

	/**
	 * @param personURI
	 */
	public void setPersonURI(String personURI) {
		this.personURI = personURI;
	}

	/**
	 * @return
	 */
	public String getInterestURI() {
		return interestURI;
	}

	/**
	 * @param interestURI
	 */
	public void setInterestURI(String interestURI) {
		this.interestURI = interestURI;
	}

	/**
	 * @return
	 */
	public Set<String> getResults() {
		return results;
	}
}

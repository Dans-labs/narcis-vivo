package nl.knaw.dans.labs.narcisvivo.data;

import java.util.HashSet;
import java.util.Set;

import nl.knaw.dans.labs.narcisvivo.util.Parameters;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class Interests {
	/**
	 * @param concept
	 * @return
	 */
	public static Set<String> getPersonsInterestedIn(String concept,
			boolean withSameAs) {
		// Prepare the output
		Set<String> output = new HashSet<String>();

		// Add the persons interested in that concept
		for (String source : Parameters.sources)
			getPersonsInterested(output, source, concept);

		// Do the same for sameAs concepts if requested
		if (withSameAs)
			for (String sameAs : ConceptMapping.getMatchingConcepts(concept))
				for (String source : Parameters.sources)
					getPersonsInterested(output, source, sameAs);

		return output;
	}

	/**
	 * 
	 */
	private static void getPersonsInterested(Set<String> output, String source,
			String concept) {
		// Set things according to the source
		String endPoint = Parameters.getEndPoint(source);
		String rq = "";
		if (source.equals("isidore"))
			rq = "select distinct ?p where {?p <http://xmlns.com/foaf/0.1/topic_interest> <CONCEPT>}";
		else
			rq = "select distinct ?p where {?p <http://vivoweb.org/ontology/core#hasResearchArea> <CONCEPT>}";
		rq = rq.replace("CONCEPT", concept);

		// Parameters for paginated query
		boolean newData = true;
		int offset = 0;

		while (newData) {
			// Compose query
			StringBuffer queryPage = new StringBuffer(rq);
			queryPage.append(" OFFSET ").append(offset).append("LIMIT 1000");

			// Execute the query
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					endPoint, queryPage.toString());
			qexec.setTimeout(0);
			ResultSet results = qexec.execSelect();
			newData = results.hasNext();
			while (results.hasNext()) {
				// Get the data
				QuerySolution result = results.next();
				String person = result.get("p").toString();

				// Store the entity in the data store
				output.add(person);
			}

			// Switch to next page
			offset += 1000;
		}
	}

	/**
	 * @param person
	 * @return
	 */
	public static Set<String> getInterestsOf(String person) {
		// Prepare the output
		Set<String> output = new HashSet<String>();

		// Get the person
		Person p = Persons.getPerson(person);
		String source = p.getSource();

		// Set things according to the source
		String endPoint = Parameters.getEndPoint(source);
		String rq = "";
		if (source.equals("isidore"))
			rq = "select distinct ?c where {<PERSON> <http://xmlns.com/foaf/0.1/topic_interest> ?c}";
		else
			rq = "select distinct ?c where {<PERSON> <http://vivoweb.org/ontology/core#hasResearchArea> ?c}";
		rq = rq.replace("PERSON", person);

		// Execute the query
		QueryExecution qexec = QueryExecutionFactory
				.sparqlService(endPoint, rq);
		qexec.setTimeout(0);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			// Get the data
			QuerySolution result = results.next();
			String concept = result.get("c").toString();

			// Store the entity in the data store
			output.add(concept);
		}

		return output;
	}
}

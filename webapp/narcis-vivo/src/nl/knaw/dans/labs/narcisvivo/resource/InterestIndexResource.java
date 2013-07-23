package nl.knaw.dans.labs.narcisvivo.resource;

import nl.knaw.dans.labs.narcisvivo.data.Interests;
import nl.knaw.dans.labs.narcisvivo.util.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class InterestIndexResource extends ServerResource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.ServerResource#get()
	 */
	@Get
	public Representation get() {
		JSONObject output = new JSONObject();

		// Get query parameters
		String source = getQuery().getFirstValue("source");
		String resetKey = getQuery().getFirstValue("reset");
		String resource = getQuery().getFirstValue("resource");

		// Are we scoped to a particular source ?
		if (source != null)
			source = source.toLowerCase();

		// Shall we reset the index?
		if (resetKey != null && resetKey.equals("true")) {
			if (source != null) {
				updateIndex(source);
			} else {
				for (String src : Parameters.sources)
					updateIndex(src);
			}
		}

		// Look for a specific resource ?
		if (resource != null) {
			try {
				output.put("results", new JSONArray());

				// Add all the persons interested
				for (String person: Interests.getPersonsInterestedIn(resource, true))
					output.append("results", person);
				
			} catch (JSONException e) {
			}
		}

		setStatus(Status.SUCCESS_OK);
		return new JsonRepresentation(output);
	}
	
	/**
	 * 
	 */
	public void updateIndex(String source) {
		// Clean up the previous entries
		Interests.clear(source);

		// Set things according to the source
		String endPoint = Parameters.getEndPoint(source);
		String rq = "";
		if (source.equals("isidore"))
			rq = "select distinct ?p ?r where {?p <http://xmlns.com/foaf/0.1/topic_interest> ?r}";
		else
			rq = "select distinct ?p ?r where {?p <http://vivoweb.org/ontology/core#hasResearchArea> ?r}";

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
				String interest = result.get("r").toString();

				// Store the entity in the data store
				Interests.add(source, interest, person);
			}

			// Switch to next page
			offset += 1000;
		}
	}

}

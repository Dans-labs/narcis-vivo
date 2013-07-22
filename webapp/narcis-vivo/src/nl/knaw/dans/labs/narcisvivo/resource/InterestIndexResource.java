package nl.knaw.dans.labs.narcisvivo.resource;

import nl.knaw.dans.labs.narcisvivo.data.ConceptMapping;
import nl.knaw.dans.labs.narcisvivo.util.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class InterestIndexResource extends ServerResource {
	// Entity type for the data store
	private final static String ENTITY = "Interest";

	// Keys for the data store
	private final static String SOURCE = "source";
	private final static String RESOURCE = "interest";
	private final static String PERSON = "person";

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

				// Add for this resource
				lookupResource(resource, output);
				
				// Add for all the sameAs resources
				for (String sameAs: ConceptMapping.getMatchingConcepts(resource))
					lookupResource(sameAs, output);
			} catch (JSONException e) {
			}
		}

		setStatus(Status.SUCCESS_OK);
		return new JsonRepresentation(output);
	}

	/**
	 * @param resource
	 * @param output
	 * @throws JSONException
	 */
	private void lookupResource(String resource, JSONObject output)
			throws JSONException {
		// Look up the entity in the index
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		query.setFilter(new FilterPredicate(RESOURCE,
				Query.FilterOperator.EQUAL, resource));

		// Prepare the reply
		for (Entity entity : store.prepare(query).asIterable())
			output.append("results", (String) entity.getProperty(PERSON));
	}

	/**
	 * 
	 */
	public void updateIndex(String source) {
		// Clean up the previous entries
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		Filter filter = new FilterPredicate(SOURCE, Query.FilterOperator.EQUAL,
				source);
		query.setFilter(filter);
		query.setKeysOnly();
		for (Entity entity : store.prepare(query).asIterable())
			store.delete(entity.getKey());

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
				Entity entity = new Entity(ENTITY);
				entity.setProperty(RESOURCE, interest);
				entity.setProperty(PERSON, person);
				store.put(entity);
			}

			// Switch to next page
			offset += 1000;
		}
	}

}

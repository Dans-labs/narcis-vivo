package nl.knaw.dans.labs.narcisvivo.resource;

import java.util.ArrayList;
import java.util.List;

import nl.knaw.dans.labs.narcisvivo.util.Parameters;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class PersonIndexResource extends ServerResource {
	// Entity type for the data store
	private final static String ENTITY = "Person";

	// Keys for the data store
	private final static String SOURCE = "source";
	private final static String FIRST = "firstName";
	private final static String LAST = "lastName";
	private final static String RESOURCE = "URI";

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
		String search = getQuery().getFirstValue("search");

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
		if (resource != null && search == null) 
			lookupResource(resource, output);		
		
		// Or search the index ?
		if (resource == null && search != null)
			searchResource(search, output);
		
		setStatus(Status.SUCCESS_OK);
		return new JsonRepresentation(output);
	}

	/**
	 * @param search
	 * @param output
	 */
	private void searchResource(String search, JSONObject output) {
		// Search for matching resources
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new FilterPredicate(FIRST, Query.FilterOperator.EQUAL,
				search));
		filters.add(new FilterPredicate(LAST, Query.FilterOperator.EQUAL,
				search));
		Filter filter = new CompositeFilter(Query.CompositeFilterOperator.OR,
				filters);
		query.setFilter(filter);
		PreparedQuery pq = store.prepare(query);
		List<Entity> results = pq.asList(FetchOptions.Builder.withLimit(5));

		// Format the results
		try {
			for (Entity entity : results) {
				JSONObject entry = new JSONObject();
				StringBuffer tmp = new StringBuffer();
				tmp.append((String) entity.getProperty(FIRST));
				tmp.append(" ").append((String) entity.getProperty(LAST));
				tmp.append(" (").append((String) entity.getProperty(SOURCE))
						.append(")");
				entry.put("name", tmp.toString());
				entry.put("resource", entity.getProperty(RESOURCE));
				output.append("results", entry);
			}
		} catch (JSONException e) {
		}
	}

	/**
	 * @param resource
	 * @param output
	 */
	private void lookupResource(String resource, JSONObject output) {
		// Look up the entity in the index
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		query.setFilter(new FilterPredicate(RESOURCE,
				Query.FilterOperator.EQUAL, resource));
		Entity entity = store.prepare(query).asSingleEntity();
		String source = (String) entity.getProperty(SOURCE);
		
		// Get the right SPARQL template and end point
		String endPoint = Parameters.getEndPoint(source);
		String rq = "select ?p ?o where { <RESOURCE> ?p ?o }";

		// Instantiate the template
		rq = rq.replace("RESOURCE", resource);

		// Send a SPARQL query to get all the data
		QueryExecution qexec = QueryExecutionFactory
				.sparqlService(endPoint, rq);
		qexec.setTimeout(0);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution s = results.next();
			try {
				JSONObject entry = new JSONObject();
				entry.put("p",s.getResource("p").toString());
				entry.put("o",s.get("o").toString());
				output.append("results", entry);
			} catch (JSONException e) {
			}
		}
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
		String rqName = Parameters.getQuery(source, "persons");
		String endPoint = Parameters.getEndPoint(source);
		Request req = new Request(Method.GET, rqName);
		String rq = getContext().getClientDispatcher().handle(req)
				.getEntityAsText();

		// Parameters for paginated query
		boolean newData = true;
		int offset = 0;

		while (newData) {
			// Compose query
			StringBuffer queryPage = new StringBuffer(rq);
			queryPage.append("OFFSET ").append(offset).append("LIMIT 1000");

			// Execute the query
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					endPoint, queryPage.toString());
			qexec.setTimeout(0);
			ResultSet results = qexec.execSelect();
			newData = results.hasNext();
			while (results.hasNext()) {
				// Get the data
				QuerySolution result = results.next();
				Resource resource = result.getResource("resource");
				String firstName = result.getLiteral("firstName")
						.getLexicalForm();
				String lastName = result.getLiteral("lastName")
						.getLexicalForm();

				// Store the entity in the data store
				Entity entity = new Entity(ENTITY);
				entity.setProperty(RESOURCE, resource.toString());
				entity.setProperty(FIRST, firstName);
				entity.setProperty(LAST, lastName);
				entity.setProperty(SOURCE, source);
				store.put(entity);
			}

			// Switch to next page
			offset += 1000;
		}
	}
}

package nl.knaw.dans.labs.narcisvivo.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.knaw.dans.labs.narcisvivo.data.Concepts;
import nl.knaw.dans.labs.narcisvivo.data.Interests;
import nl.knaw.dans.labs.narcisvivo.data.Person;
import nl.knaw.dans.labs.narcisvivo.data.Persons;
import nl.knaw.dans.labs.narcisvivo.util.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class PersonIndexResource extends ServerResource {

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
		String query = getQuery().getFirstValue("query");

		// Are we scoped to a particular source ?
		if (source != null)
			source = source.toLowerCase();

		// Shall we reset the index?
		if (resetKey != null && resetKey.equals("true")) {
			Queue queue = QueueFactory.getDefaultQueue();
			if (source != null) {
				// Make a task
				TaskOptions task = TaskOptions.Builder.withUrl("/api/person")
						.payload(source).method(TaskOptions.Method.POST);
				queue.add(task);
			} else {
				for (String src : Parameters.sources) {
					// Make a task
					TaskOptions task = TaskOptions.Builder
							.withUrl("/api/person").payload(src)
							.method(TaskOptions.Method.POST);
					queue.add(task);
				}
			}
		}

		// Look for a specific resource ?
		if (resource != null && query == null)
			lookupResource(resource, output);

		// Or search the index ?
		if (resource == null && query != null)
			queryResource(query, output);

		setStatus(Status.SUCCESS_OK);
		return new JsonRepresentation(output);
	}

	/**
	 * Search for a person based on his first or last name
	 * 
	 * @param search
	 *            the keyword for the search
	 * @param output
	 *            the object where to write a list of matching person instances
	 */
	private void queryResource(String query, JSONObject output) {
		// Query the index
		List<Person> persons = Persons.query(query);

		try {
			// Prepare the reply
			output.put("query", query);
			output.put("suggestions", new JSONArray());

			// Format the results
			for (Person person : persons) {
				JSONObject entry = new JSONObject();
				entry.put("value", person.getName());
				entry.put("data", person.getUri());
				output.append("suggestions", entry);
			}
		} catch (JSONException e) {
		}
	}

	/**
	 * @param resource
	 * @param output
	 */
	private void lookupResource(String resource, JSONObject output) {
		// Lookup for the person
		Person p = Persons.getPerson(resource);
		if (p == null)
			return;

		// Add the name and prepare the other fields
		try {
			output.put("name", p.getName());
			output.put("triples", new JSONArray());
			output.put("interests", new JSONArray());
			output.put("relations", new JSONArray());
		} catch (JSONException e) {
		}

		// Get the right SPARQL template and end point
		String endPoint = Parameters.getEndPoint(p.getSource());
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
				// Put the triples in the reply
				JSONObject entry = new JSONObject();
				entry.put("p", s.get("p").toString());
				entry.put("o", s.get("o").toString());
				output.append("triples", entry);
			} catch (JSONException e) {
			}
		}

		// Prepare a set of related researchers
		Set<JSONObject> relations = new HashSet<JSONObject>();

		// Iterate through the centre of interests of that person
		for (String interest : Interests.getInterestsOf(resource)) {
			// Add this to the output
			String label = Concepts.getLabel(interest);
			try {
				JSONObject pair = new JSONObject();
				pair.put("concept", interest);
				pair.put("label", label);
				output.append("interests", pair);
			} catch (JSONException e) {
			}

			// Add related researchers
			for (String person : Interests.getPersonsInterestedIn(interest,
					true)) {
				if (!person.equals(resource)) {
					Person relPers = Persons.getPerson(person);
					try {
						if (relPers != null) {
							JSONObject relation = new JSONObject();
							relation.put("person", person);
							relation.put("name", relPers.getName());
							relation.put("source", relPers.getSource());
							relations.add(relation);
						}
					} catch (JSONException e) {
					}
				}
			}
		}

		// Write down the set of researchers
		try {
			for (JSONObject relation : relations)
				output.append("relations", relation);
		} catch (JSONException e) {
		}
	}

	/**
	 * Post requests are used by the tasks to update the index
	 * 
	 * @param source
	 *            the name of the source to update
	 */
	@Post
	public void post(String source) {
		// Clean up the previous entries (no need, keys are unique)
		// Persons.clear(source);

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
				String resource = result.getResource("resource").toString();
				String firstName = result.getLiteral("firstName")
						.getLexicalForm();
				String lastName = result.getLiteral("lastName")
						.getLexicalForm();

				// Store the entity in the data store
				Persons.add(firstName, lastName, source, resource);
			}

			// Switch to next page
			offset += 1000;
		}
	}
}

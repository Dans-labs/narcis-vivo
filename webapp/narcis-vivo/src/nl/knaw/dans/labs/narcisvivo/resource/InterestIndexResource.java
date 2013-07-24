package nl.knaw.dans.labs.narcisvivo.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import nl.knaw.dans.labs.narcisvivo.data.Interests;
import nl.knaw.dans.labs.narcisvivo.util.Parameters;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
		String resource = getQuery().getFirstValue("resource");
		String resetKey = getQuery().getFirstValue("reset");

		// Are we scoped to a particular source ?
		if (source != null)
			source = source.toLowerCase();

		// Shall we reset the index?
		if (resetKey != null && resetKey.equals("true")) {
			Queue queue = QueueFactory.getDefaultQueue();
			if (source != null) {
				// Make a task
				TaskOptions task = TaskOptions.Builder.withUrl("/api/interest")
						.payload(source).method(TaskOptions.Method.POST);
				queue.add(task);
			} else {
				for (String src : Parameters.sources) {
					// Make a task
					TaskOptions task = TaskOptions.Builder
							.withUrl("/api/interest").payload(src)
							.method(TaskOptions.Method.POST);
					queue.add(task);
				}
			}
		}

		// Look for a specific resource ?
		if (resource != null) {
			try {
				output.put("results", new JSONArray());

				// Add all the persons interested
				for (String person : Interests.getPersonsInterestedIn(resource,
						true))
					output.append("results", person);

			} catch (JSONException e) {
			}
		}

		setStatus(Status.SUCCESS_OK);
		return new JsonRepresentation(output);
	}

	/**
	 * Post requests are used by the tasks to update the index
	 * 
	 * @param source
	 *            the name of the source to update
	 */
	@Post
	public void post(String source) {
		// Get the list of new interests for that source
		Map<String, Set<String>> newInterestsMap = getNewInterests(source);
		
		// Iterate over all the individuals
		for (Entry<String, Set<String>> entry : newInterestsMap.entrySet()) {
			String person = entry.getKey();
			Set<String> newInterests = entry.getValue();
			
			// Get current interests
			Set<String> currentInterests = Interests.getInterestsOf(person);
			
			// Delete everything he is not interested in anymore
			Set<String> delete = new HashSet<String>(currentInterests);
			delete.removeAll(newInterests);
			for (String interest: delete)
				Interests.delete(person, interest, source);
			
			// Add everything new
			Set<String> add = new HashSet<String>(newInterests);
			add.removeAll(currentInterests);
			for (String interest: add)
				Interests.add(person, interest, source);
		}
	}

	/**
	 * @return
	 */
	private Map<String, Set<String>> getNewInterests(String source) {
		Map<String, Set<String>> output = new HashMap<String, Set<String>>();

		// Set things according to the source
		String endPoint = Parameters.getEndPoint(source);
		String rq = "";
		if (source.equals("isidore"))
			rq = "select distinct ?p ?c where {?p <http://xmlns.com/foaf/0.1/topic_interest> ?c}";
		else
			rq = "select distinct ?p ?c where {?p <http://vivoweb.org/ontology/core#hasResearchArea> ?c}";

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
				String interest = result.get("c").toString();

				// Add the interest
				Set<String> interests = null;
				if (!output.containsKey(person)) {
					 interests = new HashSet<String>();
					 output.put(person, interests);
				} else {
					interests = output.get(person);
				}
				interests.add(interest);
			}

			// Switch to next page
			offset += 1000;
		}

		return output;

	}
}

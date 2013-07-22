package nl.knaw.dans.labs.narcisvivo.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.knaw.dans.labs.narcisvivo.data.ConceptMapping;
import nl.knaw.dans.labs.narcisvivo.util.LevenshteinDistance;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class ConceptLinkResource extends ServerResource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.ServerResource#get()
	 */
	@Get
	public Representation get() {
		JSONObject output = new JSONObject();

		// Shall we reset the mappings?
		String reset = getQuery().getFirstValue("reset");
		if (reset != null && reset.equals("true"))
			computeLinks();

		// Find the requested concept pair
		String resource = getQuery().getFirstValue("resource");
		if (resource != null) {
			// Prepare the reply
			try {
				List<String> concepts = ConceptMapping
						.getMatchingConcepts(resource);
				if (concepts.size() == 0) {
					output.put("results", new JSONArray());
				} else {
					for (String concept : concepts)
						output.append("results", concept);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		setStatus(Status.SUCCESS_OK);
		return new JsonRepresentation(output);
	}

	/**
	 * 
	 */
	public void computeLinks() {
		// Get the concepts from Isidore
		Request reqIsidore = new Request(Method.GET,
				"war:///WEB-INF/queries/isidore/concepts.rq");
		String queryIsidore = getContext().getClientDispatcher()
				.handle(reqIsidore).getEntityAsText();
		Map<Resource, Literal> conceptsIsidore = getConceptLabel(
				"http://www.rechercheisidore.fr/sparql/", queryIsidore);

		// Get the concepts from Narcis
		Request reqNarcis = new Request(Method.GET,
				"war:///WEB-INF/queries/narcis/concepts.rq");
		String queryNarcis = getContext().getClientDispatcher()
				.handle(reqNarcis).getEntityAsText();
		Map<Resource, Literal> conceptsNarcis = getConceptLabel(
				"http://lod.cedar-project.nl:8888/openrdf-sesame/repositories/common",
				queryNarcis);

		// Clean up the previous pairs
		ConceptMapping.clear();

		// Compare the concepts
		for (Entry<Resource, Literal> conceptIsidore : conceptsIsidore
				.entrySet()) {
			for (Entry<Resource, Literal> conceptNarcis : conceptsNarcis
					.entrySet()) {
				String lIsidore = conceptIsidore.getValue().getLexicalForm();
				String lNarcis = conceptNarcis.getValue().getLexicalForm();
				if (LevenshteinDistance.distance(lIsidore, lNarcis) < 2)
					ConceptMapping.add(conceptIsidore.getKey().toString(),
							conceptNarcis.getKey().toString());
			}
		}
	}

	/**
	 * Retrieve the list of concepts and matching labels from the end points
	 * 
	 * @param endPoint
	 *            the URI of the SPARQL end point
	 * @param query
	 *            the text of the query
	 * @return a Map<Resource, Literal> associating a label to every resource
	 */
	public Map<Resource, Literal> getConceptLabel(String endPoint, String query) {
		// For the output
		Map<Resource, Literal> output = new HashMap<Resource, Literal>();

		// Parameters for paginated query
		boolean newData = true;
		int offset = 0;

		while (newData) {
			// Compose query
			StringBuffer queryPage = new StringBuffer(query);
			queryPage.append("OFFSET ").append(offset).append("LIMIT 1000");

			// Execute the query
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					endPoint, queryPage.toString());
			qexec.setTimeout(0);
			ResultSet results = qexec.execSelect();
			newData = results.hasNext();
			while (results.hasNext()) {
				QuerySolution result = results.next();
				Resource concept = result.getResource("concept");
				Literal label = result.getLiteral("label");
				output.put(concept, label);
			}

			// Switch to next page
			offset += 1000;
		}

		return output;

	}
}

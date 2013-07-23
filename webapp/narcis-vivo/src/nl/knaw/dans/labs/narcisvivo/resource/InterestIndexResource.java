package nl.knaw.dans.labs.narcisvivo.resource;

import nl.knaw.dans.labs.narcisvivo.data.Interests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

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

		// Are we scoped to a particular source ?
		if (source != null)
			source = source.toLowerCase();

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
}

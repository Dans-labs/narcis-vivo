package nl.knaw.dans.labs.narcisvivo.data;

import java.util.HashSet;
import java.util.Set;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class Interests {
	// Entity type for the data store
	private final static String ENTITY = "Interest";

	// Keys for the data store
	private final static String SOURCE = "source";
	private final static String PERSON = "person";
	private final static String CONCEPT = "concept";

	/**
	 * @param concept
	 * @return
	 */
	public static Set<String> getPersonsInterestedIn(String concept,
			boolean withSameAs) {
		// Prepare the output
		Set<String> output = new HashSet<String>();

		// List of concepts to search for
		Set<String> concepts = new HashSet<String>();
		concepts.add(concept);
		if (withSameAs)
			for (String sameAs : ConceptMapping.getMatchingConcepts(concept))
				concepts.add(sameAs);

		// Prepare the query
		Query query = new Query(ENTITY);
		query.setFilter(new FilterPredicate(CONCEPT, Query.FilterOperator.IN,
				concepts));

		// Add the persons interested in any of the concepts
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		for (Entity entity : store.prepare(query).asIterable())
			output.add((String) entity.getProperty(CONCEPT));

		return output;
	}

	/**
	 * @param person
	 * @return
	 */
	public static Set<String> getInterestsOf(String person) {
		// Prepare the output
		Set<String> output = new HashSet<String>();

		// Prepare the query
		Query query = new Query(ENTITY);
		query.setFilter(new FilterPredicate(PERSON, Query.FilterOperator.EQUAL,
				person));

		// Execute
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		for (Entity entity : store.prepare(query).asIterable())
			output.add((String) entity.getProperty(CONCEPT));

		return output;
	}

	/**
	 * @param source
	 */
	public static void clear(String source) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		if (source != null) {
			Filter filter = new FilterPredicate(SOURCE,
					Query.FilterOperator.EQUAL, source);
			query.setFilter(filter);
		}
		query.setKeysOnly();
		for (Entity entity : store.prepare(query).asIterable())
			store.delete(entity.getKey());
	}

	/**
	 * @param person
	 * @param interest
	 * @param source
	 */
	public static void add(String person, String interest, String source) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(ENTITY);
		entity.setProperty(PERSON, person);
		entity.setProperty(CONCEPT, interest);
		entity.setProperty(SOURCE, source);
		store.put(entity);
	}
}

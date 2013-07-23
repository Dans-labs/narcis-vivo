package nl.knaw.dans.labs.narcisvivo.data;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;

public class Interests {
	// Entity type for the data store
	private final static String ENTITY = "Interest";

	// Keys for the data store
	private final static String SOURCE = "source";
	private final static String RESOURCE = "interest";
	private final static String PERSON = "person";

	/**
	 * @param concept
	 * @return
	 */
	public static Set<String> getPersonsInterestedIn(String concept,
			boolean withSameAs) {
		// Prepare the output
		Set<String> output = new HashSet<String>();

		// Add the persons interested in that concept
		getPersonsInterested(output, concept);

		// Do the same for sameAs concepts if requested
		if (withSameAs)
			for (String sameAs : ConceptMapping.getMatchingConcepts(concept))
				getPersonsInterested(output, sameAs);

		return output;
	}

	/**
	 * 
	 */
	private static void getPersonsInterested(Set<String> output, String concept) {
		// Prepare the query
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		query.setFilter(new FilterPredicate(RESOURCE,
				Query.FilterOperator.EQUAL, concept));

		// Add the results
		for (Entity entity : store.prepare(query).asIterable())
			output.add((String) entity.getProperty(PERSON));
	}

	/**
	 * 
	 */
	public static void clear(String scope) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = store.beginTransaction();
		Query query = new Query(ENTITY);
		Filter filter = new FilterPredicate(SOURCE, Query.FilterOperator.EQUAL,
				scope);
		query.setFilter(filter).setKeysOnly();
		for (Entity entity: store.prepare(query).asIterable())
			store.delete(entity.getKey());
		txn.commit();
	}

	/**
	 * @param scope
	 * @param interest
	 * @param person
	 */
	public static void add(String scope, String interest, String person) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(ENTITY);
		entity.setProperty(RESOURCE, interest);
		entity.setProperty(PERSON, person);
		entity.setProperty(SOURCE, scope);
		store.put(entity);
	}

	/**
	 * @param person
	 * @return
	 */
	public static Set<String> getInterestsOf(String person) {
		// Prepare the output
		Set<String> output = new HashSet<String>();

		// Prepare the query
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		query.setFilter(new FilterPredicate(PERSON, Query.FilterOperator.EQUAL,
				person));

		// Add the results
		for (Entity entity : store.prepare(query).asIterable())
			output.add((String) entity.getProperty(RESOURCE));

		return output;
	}
}

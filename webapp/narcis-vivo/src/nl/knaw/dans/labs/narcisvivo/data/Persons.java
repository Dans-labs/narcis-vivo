package nl.knaw.dans.labs.narcisvivo.data;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class Persons {
	// Entity type for the data store
	private final static String ENTITY = "Person";

	// Keys for the data store
	private final static String SOURCE = "source";
	private final static String FIRST = "firstName";
	private final static String LAST = "lastName";

	/**
	 * @param person
	 * @return
	 */
	public static Person getPerson(String person) {
		// Look up the entity in the index
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Key k = KeyFactory.createKey(ENTITY, person);

		// Return the entity
		try {
			Entity entity = store.get(k);
			return entityToPerson(entity);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	/**
	 * @param queryTerm
	 * @return
	 */
	public static List<Person> query(String queryTerm) {
		// Prepare output
		List<Person> output = new ArrayList<Person>();

		// Search for matching resources
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query rqStore = new Query(ENTITY);
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new FilterPredicate(FIRST, Query.FilterOperator.EQUAL,
				queryTerm));
		filters.add(new FilterPredicate(LAST, Query.FilterOperator.EQUAL,
				queryTerm));
		Filter filter = new CompositeFilter(Query.CompositeFilterOperator.OR,
				filters);
		rqStore.setFilter(filter);
		PreparedQuery pq = store.prepare(rqStore);

		// Add the results to the output
		for (Entity entity : pq.asIterable(FetchOptions.Builder.withLimit(5)))
			output.add(entityToPerson(entity));

		return output;
	}

	/**
	 * @param entity
	 * @return
	 */
	private static Person entityToPerson(Entity entity) {
		String firstName = (String) entity.getProperty(FIRST);
		String lastName = (String) entity.getProperty(LAST);
		String source = (String) entity.getProperty(SOURCE);
		String uri = entity.getKey().toString();
		return new Person(firstName, lastName, source, uri);
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
	 * @param firstName
	 * @param lastName
	 * @param source
	 * @param resource
	 */
	public static void add(String firstName, String lastName, String source,
			String resource) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(ENTITY, resource);
		entity.setProperty(FIRST, firstName);
		entity.setProperty(LAST, lastName);
		entity.setProperty(SOURCE, source);
		store.put(entity);
	}
}

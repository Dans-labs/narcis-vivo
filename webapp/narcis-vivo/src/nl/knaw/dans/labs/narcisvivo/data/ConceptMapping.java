package nl.knaw.dans.labs.narcisvivo.data;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class ConceptMapping {
	// Entity type for the data store
	private final static String ENTITY = "ConceptPair";

	// Keys for the data store
	private final static String ISIDORE = "Isidore";
	private final static String NARCIS = "Narcis";

	/**
	 * @param concept
	 * @return
	 */
	public static List<String> getMatchingConcepts(String concept) {
		// Prepare the output
		List<String> output = new ArrayList<String>();

		// Prepare the query
		Query query = new Query(ENTITY);
		List<Filter> filters = new ArrayList<Filter>();
		filters.add(new FilterPredicate(ISIDORE, Query.FilterOperator.EQUAL,
				concept));
		filters.add(new FilterPredicate(NARCIS, Query.FilterOperator.EQUAL,
				concept));
		Filter filter = new CompositeFilter(Query.CompositeFilterOperator.OR,
				filters);
		query.setFilter(filter);

		// Execute
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		for (Entity entity : store.prepare(query).asIterable()) {
			String isidore = (String) entity.getProperty(ISIDORE);
			String narcis = (String) entity.getProperty(NARCIS);
			output.add(narcis.equals(concept) ? isidore : narcis);
		}

		return output;
	}

	/**
	 * 
	 */
	public static void clear() {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		query.setKeysOnly();
		for (Entity entity : store.prepare(query).asIterable())
			store.delete(entity.getKey());
	}

	/**
	 * @param isidore
	 * @param narcis
	 */
	public static void add(String isidore, String narcis) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(ENTITY);
		entity.setProperty(ISIDORE, isidore);
		entity.setProperty(NARCIS,narcis);
		store.put(entity);		
	}
}

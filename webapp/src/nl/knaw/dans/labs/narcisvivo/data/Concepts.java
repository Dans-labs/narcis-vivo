package nl.knaw.dans.labs.narcisvivo.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class Concepts {
	// Entity type for the data store
	private final static String ENTITY = "Concept";

	// Keys for the data store
	private final static String SOURCE = "source";
	private final static String LABEL = "label";

	/**
	 * 
	 */
	public static void clear(String scope) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Query query = new Query(ENTITY);
		if (scope != null) {
			Filter filter = new FilterPredicate(SOURCE,
					Query.FilterOperator.EQUAL, scope);
			query.setFilter(filter);
		}
		query.setKeysOnly();
		for (Entity entity : store.prepare(query).asIterable())
			store.delete(entity.getKey());
	}

	/**
	 * @param scope
	 * @param interest
	 * @param person
	 */
	public static void add(String scope, String concept, String label) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		Entity entity = new Entity(ENTITY, concept);
		entity.setProperty(LABEL, label);
		entity.setProperty(SOURCE, scope);
		store.put(entity);
	}

	/**
	 * @param concept
	 * @return
	 */
	public static String getLabel(String concept) {
		DatastoreService store = DatastoreServiceFactory.getDatastoreService();
		
		Key k = KeyFactory.createKey(ENTITY, concept);
		try {
			Entity entity = store.get(k);
			return (String) entity.getProperty(LABEL);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}
}

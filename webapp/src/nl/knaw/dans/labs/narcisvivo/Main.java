package nl.knaw.dans.labs.narcisvivo;

import java.util.logging.Logger;

import javax.xml.datatype.DatatypeFactory;

import nl.knaw.dans.labs.narcisvivo.resource.ConceptLinkResource;
import nl.knaw.dans.labs.narcisvivo.resource.InterestIndexResource;
import nl.knaw.dans.labs.narcisvivo.resource.PersonIndexResource;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/**
 * @author Christophe Gueret <christophe.gueret@gmail.com>
 */
public class Main extends Application {
	// Logger
	protected static final Logger logger = Logger.getLogger(Main.class
			.getName());

	/**
	 * Creates a root Restlet that will receive all incoming calls and route
	 * them to the corresponding handlers
	 */
	@Override
	public Restlet createInboundRoot() {
		// Hack to get around a problem with Jena
		System.setProperty(DatatypeFactory.DATATYPEFACTORY_PROPERTY,
				"org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl");
		
		// Create the router
		Router router = new Router(getContext());

		// Handler for the links between concepts from Narcis and Isidore
		router.attach("/concept", ConceptLinkResource.class);

		// Handler for the person index
		router.attach("/person", PersonIndexResource.class);

		// Handler for the area of interest
		router.attach("/interest", InterestIndexResource.class);
		
		return router;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Component component = new Component();
		component.getClients().add(Protocol.WAR);
		component.getServers().add(Protocol.HTTP, 8080);
		component.getDefaultHost().attach(new Main());
		component.start();
	}
}

package nl.knaw.dans.labs.narcisvivo.util;

import java.util.Arrays;
import java.util.List;

public class Parameters {
	// List of data sources
	public final static List<String> sources = Arrays.asList("isidore",
			"eur", "kun", "ouh", "rug", "rul", "rum", "ruu", "tud", "tue",
			"tum", "uva", "uvt", "vua", "wur");

	/**
	 * @param source
	 * @return
	 */
	public static String getEndPoint(String source) {
		if (source.equals("isidore"))
			return "http://www.rechercheisidore.fr/sparql/";

		return "http://lod.cedar-project.nl:8888/openrdf-sesame/repositories/"
				+ source;
	}

	/**
	 * @param source
	 * @param queryName
	 * @return
	 */
	public static String getQuery(String source, String queryName) {
		StringBuffer tmp = new StringBuffer("war:///WEB-INF/queries/");
		tmp.append(source.equals("isidore") ? "isidore" : "narcis");
		tmp.append("/").append(queryName).append(".rq");
		return tmp.toString();
	}

}

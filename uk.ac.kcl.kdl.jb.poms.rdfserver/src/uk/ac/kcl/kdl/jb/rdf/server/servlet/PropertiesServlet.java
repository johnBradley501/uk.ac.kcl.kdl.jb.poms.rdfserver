package uk.ac.kcl.kdl.jb.rdf.server.servlet;

import static org.eclipse.rdf4j.query.QueryLanguage.SPARQL;

import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.workbench.base.TupleServlet;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;

public class PropertiesServlet extends TupleServlet {

	public PropertiesServlet() {
		super("properties.xsl", "propertye");
	}

	private static final String DISTINCT_TYPE = "SELECT DISTINCT ?pred WHERE { ?pred a rdf:Property} order by ?pred";

	@Override
	protected void service(TupleResultBuilder builder, RepositoryConnection con)
		throws Exception
	{
		TupleQuery query = con.prepareTupleQuery(SPARQL, DISTINCT_TYPE);
		TupleQueryResult result = query.evaluate();
		try {
			while (result.hasNext()) {
				builder.result(result.next().getValue("pred"));
			}
		}
		finally {
			result.close();
		}
	}


}

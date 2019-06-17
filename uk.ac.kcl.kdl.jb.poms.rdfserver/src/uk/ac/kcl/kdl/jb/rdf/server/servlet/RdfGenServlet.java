/*******************************************************************************
 * Copyright (c) 2017 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.kdl.jb.rdf.server.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.workbench.base.TupleServlet;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.kcl.kdl.jb.rdf.server.SharedRepositoryHandler;
/**
 * the servlet that generates a strictly RDF data response to an entity request. rdf4j's native code provides a response that is lightly
 * wrapped in HTML instead.  This class is invoked in uk.ac.kcl.kdl.jb.rdf.server.servlet.EntityServlet when a pure RDF data response is requested.
 * <p>Note the parallels with rdf4j's org.eclipse.rdf4j.workbench.commands.ExploreServlet class.
 * 
 * @author John Bradley
 */

// http://localhost:8080/uk.ac.kcl.kdl.jb.rdf.server/repositories/dprr/rdfgen?resource=%3Chttp%3A%2F%2Fromanrepublic.ac.uk%2Frdf%2Fentity%2FPerson%2F1%3E
public class RdfGenServlet extends TupleServlet {

	private final Logger logger = LoggerFactory.getLogger(RdfGenServlet.class);
	
	private final String TURTLE_MIMETYPE = RDFFormat.TURTLE.getDefaultMIMEType(); // "application/x-turtle";

	public RdfGenServlet() {
		super(null, "subject", "predicate", "object", "context");
	}

	@Override
	public void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
			throws Exception
	{
		RepositoryConnection con = repository.getConnection();
		con.setParserConfig(NON_VERIFYING_PARSER_CONFIG);
		try {
			this.service(req, resp, null, con);
		}
		finally {
			con.close();
		}
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp,
			final TupleResultBuilder builder, final RepositoryConnection con)
		throws BadRequestException, RDF4JException
	{
		final Value value = req.getValue("resource");
		logger.debug("resource = {}", value);

		String requestedMimeType = "";
		Optional<RDFFormat> formatHolder = Optional.empty();

		String mimeType = req.getParameter("format");
		if(mimeType != null){
			formatHolder = Rio.getWriterFormatForMIMEType(mimeType);
		} else {

			List<String> acceptList = EntityServlet.processAcceptHeader(req);
			Iterator<String> it = acceptList.iterator();
			while((!formatHolder.isPresent()) && it.hasNext()){
				requestedMimeType = it.next();
				formatHolder = Rio.getWriterFormatForMIMEType(requestedMimeType);
			}
		}
		RDFFormat format = null;
		if(formatHolder.isPresent())format = formatHolder.get();
		else {
			format = RDFFormat.TURTLE;
			requestedMimeType = TURTLE_MIMETYPE;
		}
		resp.setContentType(requestedMimeType);

		RDFWriter writer;
		try {
			writer = Rio.createWriter(format, resp.getOutputStream());
		} catch (UnsupportedRDFormatException e) {
			throw new RuntimeException("unrecognised RDF Format 'requestedMimeType'", e);
		} catch (IOException e) {
			throw new RuntimeException("IOException on HttpServletResponse output", e);
		}
		writer.startRDF();
		
//		writer.handleComment("This data is Copyright 2017 King's College London (All rights reserved),\n"+
//		"and is made available under the Creative Commons License 'Attribution-NonCommercial 4.0 International (CC BY-NC 4.0) '");
		writer.handleComment(SharedRepositoryHandler.getCopyrightText());
		
		if(format.supportsNamespaces()){
			RepositoryResult<Namespace> namespaces = con.getNamespaces();
			while(namespaces.hasNext()){
				Namespace name = namespaces.next();
				String uri = con.getNamespace(name.getPrefix());
				writer.handleNamespace(name.getPrefix(), uri);
			}
		}

		boolean resource = value instanceof Resource;
		int count = 0;
		if (resource) {
			count += export(con, writer, (Resource)value, null, null);
			logger.debug("After subject, total = {}", count);
		}
		if (value instanceof IRI) {
			count += export(con, writer, null, (IRI)value, null);
			logger.debug("After predicate, total = {}", count);
		}
		if (value != null) {
			count += export(con, writer, null, null, value);
			logger.debug("After object, total = {}", count);
		}
		if (resource) {
			count += export(con, writer, null, null, null, (Resource)value);
			logger.debug("After context, total = {}", count);
		}
		writer.endRDF();

	}
	int export(RepositoryConnection con, RDFWriter writer, 
			Resource subj, IRI pred, Value obj, Resource... context)
		throws RDF4JException, MalformedQueryException, QueryEvaluationException{
		RepositoryResult<Statement> result = con.getStatements(subj, pred, obj, true, context);
		int count = 0;
		try {
			while (result.hasNext()) {
				Statement statement = result.next();
				writer.handleStatement(statement);
				count++;
			}
		}
		finally {
			result.close();
		}
		return count;
	}

}

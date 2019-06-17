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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.UnsupportedQueryResultFormatException;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;
import org.eclipse.rdf4j.workbench.exceptions.MissingInitParameterException;
import org.eclipse.rdf4j.workbench.util.QueryFactory;

import uk.ac.kcl.kdl.jb.rdf.server.SharedRepositoryHandler;
/**
 * the servlet that provides direct SPARQL endpoint services at http://romanrepublic.ac.uk/rdf/endpoint.
 * It implements most of the query-oriented part of the SPARQL 1.1 spec at https://www.w3.org/TR/sparql11-protocol/.
 * 
 * @author John Bradley
 */

public class EndpointServlet implements Servlet {
	public static final String SERVER_PARAM = "default-server";
	public static final String REPOSITORY_PARAM = "default-repository";
	public static final String FORMAT_PARAM = "default-format";

	private ServletConfig config = null;
	private RepositoryManager manager = null;
	private Map<String, String> defaults = new HashMap<String, String>();

@Override
public void destroy() {
	if(manager != null)manager.shutDown();
}

@Override
public ServletConfig getServletConfig() {
	return config;
}

@Override
public String getServletInfo() {
	return getClass().getSimpleName();
}


private void doParam(String configName, String defaultName) throws ServletException{
	String parm = config.getInitParameter(configName);
	if (parm == null || parm.trim().isEmpty()) {
		throw new MissingInitParameterException(configName);
	}
	defaults.put(defaultName, parm);
}

@Override
public void init(ServletConfig config) throws ServletException {
	this.config = config;
	// final String param = config.getInitParameter(SERVER_PARAM);
	// if (param == null || param.trim().isEmpty()) {
	//	 throw new MissingInitParameterException(SERVER_PARAM);
	// }
	SharedRepositoryHandler.initInstance(config);
	//doParam(REPOSITORY_PARAM, "path");
	defaults.put("path", fixPath(SharedRepositoryHandler.getDefaultPath()));
	doParam(FORMAT_PARAM, "Accept");
	doParam(FORMAT_PARAM, "Content-Type");
	manager = SharedRepositoryHandler.getManager();
}

private String fixPath(String parm){
	parm = parm.trim();
	String rslt = parm;
	if(parm.charAt(0)=='/')rslt = parm.substring(1);
	return rslt;
}

@Override
public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
	HttpServletRequest hreq = (HttpServletRequest)req;
	HttpServletResponse hresp = (HttpServletResponse)resp;
	/*
	if(manager == null){
		try {
			manager = createRepositoryManager(getManagerURI(hreq));
		}
		catch (IOException e) {
			throw new ServletException("Failure creating Manager",e);
		}
		catch (RepositoryException e) {
			throw new ServletException("Failure creating Manager",e);
		} catch (URISyntaxException e1) {
			throw new ServletException("Failure creating Manager", e1);
		}
	}
	*/
	String queryStr = hreq.getParameter("query");
	
	String repPath = defaults.get("path");
	Repository rep = manager.getRepository(repPath);
	// from AbstractServlet
	if (rep instanceof HTTPRepository) {
		((HTTPRepository)rep).setPreferredRDFFormat(RDFFormat.BINARY);
	}
	RepositoryConnection con = rep.getConnection();

	Query query = null;
	try {
	   query = QueryFactory.prepareQuery(con, QueryLanguage.SPARQL, queryStr);
	} catch (RDF4JException e){
		throw new ServletException("Query could not be processed", e);
	}
	// from QueryEvaluator
	try {
	if(query instanceof GraphQuery){
		GraphQuery graphQuery = (GraphQuery)query;
		handleGraphQuery(hreq, hresp, graphQuery, con);
	} else if(query instanceof TupleQuery){
		TupleQuery tupleQuery = (TupleQuery)query;
		handleTupleQuery(hreq, hresp, tupleQuery, con);
	} else {
		throw new BadRequestException("Unsupported query type: " + query.getClass().getSimpleName());
	}
	} finally {
		con.close();
	}
}

private void handleGraphQuery(HttpServletRequest hreq, HttpServletResponse hresp, GraphQuery graphQuery, RepositoryConnection con) throws ServletException {
	Optional<RDFFormat> formatHolder = Optional.empty();
	String formatMime = hreq.getParameter("format");
	if(formatMime != null && formatMime.length() > 0)formatHolder = Rio.getWriterFormatForMIMEType(formatMime);
	if(!formatHolder.isPresent()){
		List<String> accepts = EntityServlet.processAcceptHeader(hreq);
		Iterator<String>it = accepts.iterator();
		while((!formatHolder.isPresent()) && it.hasNext()){
			String acceptStr = it.next();
			formatHolder = Rio.getWriterFormatForMIMEType(acceptStr);
		}
	}
	RDFFormat format = null;
	if(formatHolder.isPresent())format = formatHolder.get();
	else {
		format = RDFFormat.TURTLE;
	}
	hresp.setContentType(format.getDefaultMIMEType());

	RDFWriter writer;
	try {
		writer = Rio.createWriter(format, hresp.getOutputStream());
	} catch (UnsupportedRDFormatException e) {
		throw new ServletException("unrecognised RDF Format 'requestedMimeType'", e);
	} catch (IOException e) {
		throw new ServletException("IOException on HttpServletResponse output", e);
	}
	writer.startRDF();
//	writer.handleComment("This data is Copyright 2017 King's College London (All rights reserved),\n"+
//	"and is made available under the Creative Commons License 'Attribution-NonCommercial 4.0 International (CC BY-NC 4.0)'");
	writer.handleComment(SharedRepositoryHandler.getCopyrightText());

	if(format.supportsNamespaces()){
		RepositoryResult<Namespace> namespaces = con.getNamespaces();
		while(namespaces.hasNext()){
			Namespace name = namespaces.next();
			String uri = con.getNamespace(name.getPrefix());
			writer.handleNamespace(name.getPrefix(), uri);
		}
	}
	
	// from QueryEvaluator.evaluateGraphQuery
	List<Statement> statements = Iterations.asList(graphQuery.evaluate());
	for (Statement statement : statements) {
		writer.handleStatement(statement);
	}
	writer.endRDF();
}

private void handleTupleQuery(HttpServletRequest hreq, HttpServletResponse hresp, TupleQuery tupleQuery, RepositoryConnection con) throws ServletException {
	Optional<QueryResultFormat> formatHolder = Optional.empty();
	String formatMime = hreq.getParameter("format");
	if(formatMime != null && formatMime.length() > 0)formatHolder = QueryResultIO.getWriterFormatForMIMEType(formatMime);
	if(!formatHolder.isPresent()){
		List<String> accepts = EntityServlet.processAcceptHeader(hreq);
		Iterator<String>it = accepts.iterator();
		while((!formatHolder.isPresent()) && it.hasNext()){
			String acceptStr = it.next();
			formatHolder = QueryResultIO.getWriterFormatForMIMEType(acceptStr);
		}
	}
	QueryResultFormat format = null;
	if(formatHolder.isPresent())format = formatHolder.get();
	else {
		format = TupleQueryResultFormat.TSV;
	}
	hresp.setContentType(format.getDefaultMIMEType());
	// see http://docs.rdf4j.org/javadoc/2.1/org/eclipse/rdf4j/query/resultio/class-use/QueryResultFormat.html
	
	QueryResultWriter writer = null;
	try {
		 writer = QueryResultIO.createWriter(format, hresp.getOutputStream());
	} catch (UnsupportedQueryResultFormatException | IOException e) {
		throw new ServletException("A problem arose when the Tuplet writer was being set up", e);
	}
	
	RepositoryResult<Namespace> namespaces = con.getNamespaces();
	while(namespaces.hasNext()){
		Namespace name = namespaces.next();
		String uri = con.getNamespace(name.getPrefix());
		writer.handleNamespace(name.getPrefix(), uri);
	}
	
	TupleQueryResult result = null;
	try {
	   result = tupleQuery.evaluate();
	} catch (Exception e){
		String msg = e.getMessage();
		if((msg == null) || msg.length() == 0)msg = "Error in Query proessing";
		throw new ServletException(msg, e);
	}
	String[] names = result.getBindingNames().toArray(new String[0]);
	writer.startQueryResult(Arrays.asList(names));
	while(result.hasNext()){
		BindingSet bset = result.next();
		writer.handleSolution(bset);
	}
	writer.endQueryResult();
}
}

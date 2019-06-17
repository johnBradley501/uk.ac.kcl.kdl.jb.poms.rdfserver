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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
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

import org.apache.commons.fileupload.FileUploadException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.workbench.commands.ExploreServlet;
import org.eclipse.rdf4j.workbench.exceptions.MissingInitParameterException;
import org.eclipse.rdf4j.workbench.util.WorkbenchRequest;

import uk.ac.kcl.kdl.jb.rdf.server.SharedRepositoryHandler;

public class EntityServlet implements Servlet {
	/**
	 * generates the response to the delivery of an "entity" URI to this server.  The material to be returned
	 * are the set of triples in the RDF repository that have the invoked the entity URL as one of the three elements.
	 * If the user has requested raw RDF data as the response, then this servlet invokes its held instance of RdfGenServlet
	 * to generate the result.  If not, it invokes the rdf4j ExploreServlet which will, in turn, generate a lightweight
	 * HTML facade in front of the RDF data (as the workbench Explore servlet does).
	 * 
	 * @author John Bradley
	 */

	public static final String SERVER_PARAM = "default-server";
	public static final String REPOSITORY_PARAM = "default-repository";
	public static final String FORMAT_PARAM = "default-format";
	public static final String XSL_PATH = "transformations";

	private ServletConfig config = null;
	
	private ExploreServlet myExploreServlet = null;
	private RdfGenServlet myRdfGenServlet = null;
	
	//private String xslPath = null;
	
	private Map<String, String> defaults = new HashMap<String, String>();
	/* defaults from workbench
path	/dprr
Accept: application/rdf+xml
infer:	true
queryLn:SPARQL
limit:	100
command:/query
Content-Type:	application/rdf+xml
	 */
	
	public static List<String> processAcceptHeader(HttpServletRequest hreq){
		List<String> rslt = new ArrayList<String>();
		Enumeration<String> accepts = hreq.getHeaders("Accept");
		while(accepts.hasMoreElements()){
			String accept = accepts.nextElement();
			String[] items = accept.split(",\\s*");
			for(String item: items){
				String itm = item.trim();
				if(itm.length() > 0)rslt.add(itm);
			}
			
		}
		return rslt;
	}

	private RepositoryManager manager = null;
	
	@Override
	public void destroy() {
		manager.shutDown();
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public String getServletInfo() {
		// from AbstractServlet
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
		SharedRepositoryHandler.initInstance(config);

		defaults.put("path", fixPath(SharedRepositoryHandler.getDefaultPath()));
		doParam(FORMAT_PARAM, "Accept");
		doParam(FORMAT_PARAM, "Content-Type");
	}
	
	private String fixPath(String parm){
		parm = parm.trim();
		String rslt = parm;
		if(parm.charAt(0)=='/')rslt = parm.substring(1);
		return rslt;
	}

	private String getManagerURI(HttpServletRequest hreq) throws URISyntaxException{ // compare with WorkbenchGateway.getDefaultServer
		String serverPath = config.getInitParameter(SERVER_PARAM);
		URI thisURI = new URI(new String(hreq.getRequestURL())); // http://localhost:8080/uk.ac.kcl.kdl.jb.rdf.server/entity/Person/20
		URI serverURI = thisURI.resolve(serverPath);
		System.out.println(serverURI.toString());
		return serverURI.toString();
	}

	private String getRdfType(String parm){
		if(parm == null)return null;
		if("text/html".equals(parm))return null;
		if(parm.equals("rdf"))parm="text/turtle";
		String rslt = null; // RDFFormat.TURTLE.getDefaultMIMEType();
		Optional<RDFFormat> formatHolder = Rio.getWriterFormatForMIMEType(parm.trim());
		if(formatHolder.isPresent())rslt = formatHolder.get().getDefaultMIMEType();
		else {rslt = RDFFormat.TURTLE.getDefaultMIMEType();}
		return rslt;
	}
	
	private String makeResourceParameter(HttpServletRequest hreq){
		// e.g. <http://romanrepublic.ac.uk/rdf/entity/Person/1>

		String path = hreq.getRequestURI();
		String[] parts = path.split("/");
		StringBuffer buf = new StringBuffer();
		// buf.append("<http://romanrepublic.ac.uk/rdf/entity/");
		buf.append("<"+SharedRepositoryHandler.getUriPrefix());
		buf.append(parts[parts.length-2]+"/");
		buf.append(parts[parts.length-1]+">");
		return new String(buf);
	}


	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		HttpServletRequest hreq = (HttpServletRequest)req;
		HttpServletResponse hresp = (HttpServletResponse)resp;
		if(manager == null){
			try {
				//manager = createRepositoryManager(getManagerURI(hreq));
				manager = SharedRepositoryHandler.getManager();

				myExploreServlet = new ExploreServlet();
				myExploreServlet.setRepositoryManager(manager);
				myExploreServlet.init(config);
				myRdfGenServlet = new RdfGenServlet();
				myRdfGenServlet.setRepositoryManager(manager);
				myRdfGenServlet.init(config);
			}
			//catch (IOException e) {
			//	throw new ServletException("Failure creating Manager",e);
			//}
			catch (RepositoryException e) {
				throw new ServletException("Failure creating Manager",e);
				//} catch (URISyntaxException e1) {
				//	throw new ServletException("Failure creating Manager", e1);
			}
		}
		
		// URLDecoder decoder = new  URLDecoder();
		
		String rdfContentType = null;
		String formatMime = hreq.getParameter("format");
		if(formatMime != null && formatMime.length() > 0)rdfContentType = getRdfType(formatMime);
		if(rdfContentType == null){
			List<String> accepts = processAcceptHeader(hreq);
			Iterator<String>it = accepts.iterator();
			String acceptStr = "";
			while(rdfContentType == null && (!"text/html".equals(acceptStr)) && it.hasNext()){
				acceptStr = it.next();
				rdfContentType = getRdfType(acceptStr);
			}
		}
		
		Exception exc = null;
		WorkbenchRequest wreq = null;
		String repPath = defaults.get("path");
		Repository rep = manager.getRepository(repPath);
		// from AbstractServlet
		if (rep instanceof HTTPRepository) {
			((HTTPRepository)rep).setPreferredRDFFormat(RDFFormat.BINARY);
		}

		try {
			wreq = new WorkbenchRequest(rep, hreq, defaults);
		} catch (RepositoryConfigException e) {
			exc = e;
		} catch (RepositoryException e) {
			exc = e;
		} catch (FileUploadException e) {
			exc = e;
		}
		if(exc != null) throw new ServletException("failure creating WorkbenchRequest", exc);
		
		wreq.setParameter("resource",makeResourceParameter(hreq));
		if(rdfContentType != null)wreq.setParameter("format", rdfContentType);
		
		String xslPath = hreq.getContextPath()+config.getInitParameter(XSL_PATH);

		if(rdfContentType == null){
			try {
				wreq.setParameter("limit_explore", "0"); // default setting for limit is "all"
				myExploreServlet.setRepository(rep);
				myExploreServlet.service(wreq, hresp, xslPath);
			} catch (Exception e) {
				throw new ServletException("Exception raised in ExploreServlet.service", e);
			}
		} else {
			try {
				myRdfGenServlet.setRepository(rep);
				myRdfGenServlet.service(wreq, hresp, xslPath);
			} catch (Exception e) {
				throw new ServletException("Exception raised in RdfGenServlet.service", e);
			}
			
		}
	}

}

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

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.kcl.kdl.jb.rdf.server.SharedRepositoryHandler;

public class OntologyServlet implements Servlet {
	/**
	 * generates a response consisting of the contents of a file containing the ontology. The location of this
	 * file can be speicified in option ontologyPath in the WEB-INF/werver.config.  Why is it done through this servlet?
	 * See https://stackoverflow.com/questions/1121858/redirect-all-requests-for-a-subdirectory-in-tomcat-6-0
	 * 
	 * @author John Bradley
	 */

	private ServletConfig config = null;

	@Override
	public void destroy() {
		// nothing needed here
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public String getServletInfo() {
		return getClass().getSimpleName();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		this.config  = config;
	}

	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		HttpServletRequest hreq = (HttpServletRequest)req;
		HttpServletResponse hresp = (HttpServletResponse)resp;
		
		String url=SharedRepositoryHandler.getOntologyPrefix();
		ServletContext sc = config.getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher(url);
		hresp.setHeader("Content-Type", "text/turtle");
		rd.forward(hreq,hresp);
	}

}

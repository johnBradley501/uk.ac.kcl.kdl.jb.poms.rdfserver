/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.eclipse.rdf4j.workbench.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import uk.ac.kcl.kdl.jb.rdf.server.SharedRepositoryHandler;

public class BasicServletConfig implements ServletConfig {
	/*
	 * code added to reference SharedRepositoryHandler, which now holds two string values that in base rdf4j code comes
	 * from the Servlet's initParameters. See lines 46-54 in BasicServletConfig(String name, ServletConfig config)    JB DDH/KCL   June 2017
	 */

	private String name;

	private ServletContext context;

	private Hashtable<String, String> params;

	public BasicServletConfig(String name, ServletContext context) {
		this.name = name;
		this.context = context;
		params = new Hashtable<String, String>();
	}

	@SuppressWarnings("unchecked")
	public BasicServletConfig(String name, ServletConfig config) {
		this(name, config.getServletContext());
		Enumeration<String> e = config.getInitParameterNames();
		while (e.hasMoreElements()) {
			String param = e.nextElement();
			params.put(param, config.getInitParameter(param));
		}
		// the following lines, added to use SharedRepositoryHandler, are needed because default-server and default-path are provided as
		// Servlet initParameters in the distributed version of rdf4j, but come from SharedRepositoryHandler in the DPRR RDF server.   JB
		try {
			SharedRepositoryHandler.initInstance(config); // make sure that SharedRepositoryHandler is set up    JB
		} catch (ServletException e1) {
			throw new RuntimeException("Unexpected error initializing SharedRepositoryHandler", e1);
		}
		if(!params.containsKey("default-server"))params.put("default-server", SharedRepositoryHandler.getServerURL());  // added JB
		if(!params.containsKey("default-path"))params.put("default-path", SharedRepositoryHandler.getDefaultPath());  // added JB
	}

	public BasicServletConfig(String name, ServletConfig config, Map<String, String> params) {
		this(name, config);
		this.params.putAll(params);
	}

	public BasicServletConfig(String name, ServletContext context, Map<String, String> params) {
		this.name = name;
		this.context = context;
		this.params = new Hashtable<String, String>(params);
	}

	public String getServletName() {
		return name;
	}

	public ServletContext getServletContext() {
		return context;
	}

	public Enumeration<String> getInitParameterNames() {
		return params.keys();
	}

	public String getInitParameter(String name) {
		return params.get(name);
	}

}

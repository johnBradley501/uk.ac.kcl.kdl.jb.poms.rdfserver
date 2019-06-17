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

package uk.ac.kcl.kdl.jb.rdf.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
/**
 * provides a holder for a shared RepositoryManager. It gets its information about what data source to use from
 * the file /WEB-INF/server.config, and holds an instance of a org.eclipse.rdf4j.repository.manager.RepositoryManager
 * that any DPRR (or rdf4j) class can get at and use.
 * This class also provides access to the strings in server.config that define where the data collection is, and
 * which repository is to be used within it.
 *
 * This class is used both by classes written specifically for
 * the DPRR RDF Server, and by the (slightly modified) classes defined within rdf4j.  It is needed for two reasons:
 * <ul>
 * <li>In the original rdf4j there was only one rdf4j servlet "WorkbenchGateway" (with a associated family of classes) that 
 * connected to the RDF repository, so data about where the repository is could be defined in servlet init-param data for
 * that servlet. In the DPRR RDF Server there are two other servlets that also need access to the same data, and it made
 * better sense to move the data from there (since it was inaccessible to the two added DPRR servlets) and put it in a /WEB-INF file
 * "server.config".
 * <li>DPRR RDF data is stored in the file system directly, rather than made available through a separate rdf4j RDF server.
 * When served this way, it has proven necessary to share a single connection between all servlets that need it, rather than
 * having each one open up its own.
 * </ul>
 * 
 * @author John Bradley
 */

public class SharedRepositoryHandler {

	public static final String SERVER_PARAM = "server";
	private static final String DEFAULT_PATH = "path";
	private static final String DEFAULT_COPYRIGHT = "copyright";
	private static final String DEFAULT_URIPREFIX = "uriPrefix";
	private static final String ONTOLOGY_PATH = "ontologyPath";

	private static SharedRepositoryHandler instance = null;
	
	private RepositoryManager manager = null;
	private String thePath = null;
	private String serverURL = null;
	private String copyrightText = null;
	private String uriPrefix = null;
	private String ontologyPath = null;
	
	public static void initInstance(ServletConfig config) throws ServletException{
		if(instance != null)return;
		instance = new SharedRepositoryHandler(config.getServletContext());
	}
	
	public static void init(ServletContext context) throws ServletException{
		if(instance != null)return;
		instance = new SharedRepositoryHandler(context);
	}
	
	public static void shutdown(){
		if(instance == null)return;
		if(instance.manager != null){
			instance.manager.shutDown();
		}
		instance = null;
	}
	
	public static RepositoryManager getManager(){
		if(instance == null)return null;
		return instance.manager;
	}
	
	public static String getDefaultPath(){
		if(instance == null)return null;
		return instance.thePath;
	}
	
	public static String getServerURL(){
		if(instance == null)return null;
		return instance.serverURL;
	}
	
	public static String getUriPrefix(){
		if(instance == null)return null;
		return instance.uriPrefix;
	}
	
	public static String getOntologyPrefix(){
		if(instance == null)return null;
		return instance.ontologyPath;
	}
	
	
	public static String getCopyrightText(){
		if(instance == null)return null;
		return instance.copyrightText;
	}
	private SharedRepositoryHandler(ServletContext context) throws ServletException{
		// https://stackoverflow.com/questions/4340653/file-path-to-resource-in-our-war-web-inf-folder
		InputStream resourceContent = context.getResourceAsStream("/WEB-INF/server.config");
		Properties myProperties = new Properties();
		try {
			myProperties.load(resourceContent);
		} catch (IOException e) {
			throw new ServletException("Could not open server.config", e);
		}
		serverURL = myProperties.getProperty(SERVER_PARAM);
		if(serverURL == null)throw new ServletException("Could not find parameter "+SERVER_PARAM);
		try {
			createRepositoryManager(serverURL);
		} catch (RepositoryException | IOException e) {
			throw new ServletException("Could not setup repository", e);
		}
		thePath = myProperties.getProperty(DEFAULT_PATH);
		copyrightText = myProperties.getProperty(DEFAULT_COPYRIGHT);
		uriPrefix = myProperties.getProperty(DEFAULT_URIPREFIX);
		ontologyPath = myProperties.getProperty(ONTOLOGY_PATH);
	}
	

	private File asLocalFile(final URL rdf)	throws UnsupportedEncodingException{
		return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
	}


	// from WorkbenchServlet
	private void createRepositoryManager(final String param)
		throws IOException, RepositoryException
	{
		if (param.startsWith("file:")) {
			manager = new LocalRepositoryManager(asLocalFile(new URL(param)));
		}
		else {
			manager = new RemoteRepositoryManager(param);
		}
		manager.initialize();
	}

}

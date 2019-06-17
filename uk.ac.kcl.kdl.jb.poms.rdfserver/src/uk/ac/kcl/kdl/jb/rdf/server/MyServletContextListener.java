package uk.ac.kcl.kdl.jb.rdf.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

public class MyServletContextListener implements ServletContextListener {

	// see https://javabeat.net/servletcontextlistener-example/

	@Override
	public void contextInitialized(ServletContextEvent arg) {
		try {
			SharedRepositoryHandler.init(arg.getServletContext());
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg) {
		SharedRepositoryHandler.shutdown();
	}

}

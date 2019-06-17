/*******************************************************************************
 * Copyright (c) 2017 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This code borrows from rdf4j's InfoServlet and SummaryServlet code.
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.kdl.jb.rdf.server.servlet;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.workbench.base.TransformationServlet;
import org.eclipse.rdf4j.workbench.commands.SummaryServlet;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WelcomeServlet extends TransformationServlet {

	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private static String numbStatements = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(SummaryServlet.class);

	@Override
	public void service(final TupleResultBuilder builder, final String xslPath)
		throws RepositoryException, QueryResultHandlerException
	{
		// final TupleResultBuilder builder = getTupleResultBuilder(req, resp);
		builder.transform(xslPath, "welcome.xsl");
		builder.start("version", "jvm", "size");
		builder.link(Arrays.asList(INFO));
		final String version = this.appConfig.getVersion().toString();
		final String jvm = getJvmName();
		final RepositoryConnection con = repository.getConnection();
		if (numbStatements == null) try {
			try {
				List<Future<String>> futures = getRepositoryStatistics(con);
				numbStatements = getResult("repository size.", futures.get(0));
			}
			catch (InterruptedException e) {
				LOGGER.warn("Interrupted while requesting repository statistics.", e);
			}
		}
		finally {
			con.close();
		}
		builder.result(version, jvm, numbStatements);
		builder.end();
	}

	private String getJvmName() {
		final StringBuilder builder = new StringBuilder();
		builder.append(System.getProperty("java.vm.vendor")).append(" ");
		builder.append(System.getProperty("java.vm.name")).append(" (");
		builder.append(System.getProperty("java.version")).append(")");
		return builder.toString();
	}

	private String getResult(String itemRequested, Future<String> future) {
		String result = "Unexpected interruption while requesting " + itemRequested;
		try {
			if (future.isCancelled()) {
				result = "Timed out while requesting " + itemRequested;
			}
			else {
				try {
					result = future.get();
				}
				catch (ExecutionException e) {
					LOGGER.warn("Exception occured during async request.", e);
					result = "Exception occured while requesting " + itemRequested;
				}
			}
		}
		catch (InterruptedException e) {
			LOGGER.error("Unexpected exception", e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Future<String>> getRepositoryStatistics(final RepositoryConnection con)
		throws InterruptedException
	{
		List<Future<String>> futures;
		futures = executorService.invokeAll(Arrays.asList(new Callable<String>() {

			@Override
			public String call()
				throws RepositoryException
			{
				return Long.toString(con.size());
			}

		}, new Callable<String>() {

			@Override
			public String call()
				throws RepositoryException
			{
				return Integer.toString(Iterations.asList(con.getContextIDs()).size());
			}

		}), 8000, TimeUnit.MILLISECONDS);
		return futures;
	}

}

<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns="http://www.w3.org/1999/xhtml">

	<!-- Titles -->
	<xsl:variable name="workbench.title">PoMS RDF Repository</xsl:variable>
	<xsl:variable name="information.title">System Information</xsl:variable>
	<xsl:variable name="detail-information.title">
		Detail System Information
	</xsl:variable>
	<xsl:variable name="repository-list.title">
		List of Repositories
	</xsl:variable>
	<xsl:variable name="summary.title">Summary</xsl:variable>
	<xsl:variable name="explore.title">Explore</xsl:variable>
	<xsl:variable name="namespaces.title">Namespaces In PoMS RDF</xsl:variable>
	<xsl:variable name="contexts.title">Contexts In POMS RDF</xsl:variable>
	<xsl:variable name="types.title">Types In PoMS RDF</xsl:variable>
	<xsl:variable name="classes.title">Classes In PoMS RDF</xsl:variable>
	<xsl:variable name="properties.title">Properties In PoMS RDF</xsl:variable>
	<xsl:variable name="query.title">Query PoMS RDF</xsl:variable>
	<xsl:variable name="saved-queries.title">Saved Queries</xsl:variable>
	<xsl:variable name="query-result.title">Query Result</xsl:variable>
	<xsl:variable name="export.title">Export Repository</xsl:variable>
	<xsl:variable name="update.title">Execute SPARQL Update on Repository</xsl:variable>
	<xsl:variable name="add.title">Add RDF</xsl:variable>
	<xsl:variable name="remove.title">Remove Statements</xsl:variable>
	<xsl:variable name="clear.title">Clear Repository</xsl:variable>
	<xsl:variable name="change-server.title">Connect to RDF4J Server</xsl:variable>
	<xsl:variable name="selections.title">Current Selections</xsl:variable>
	<xsl:variable name="repository-create.title">New Repository</xsl:variable>
	<xsl:variable name="repository-delete.title">
		Delete Repository
	</xsl:variable>
	<xsl:variable name="repository-location.title">
		Repository Location
	</xsl:variable>
	<xsl:variable name="repository-size.title">Repository Size</xsl:variable>
	<xsl:variable name="bad-request.title">Bad Request</xsl:variable>
	<xsl:variable name="super-classes.title">Super Classes</xsl:variable>
	<xsl:variable name="sub-classes.title">Sub Classes</xsl:variable>
	<xsl:variable name="individulas.title">Individuals</xsl:variable>
	<xsl:variable name="property-domain.title">Domain</xsl:variable>
	<xsl:variable name="property-range.title">Range</xsl:variable>
	<xsl:variable name="super-properties.title">Super Properties</xsl:variable>
	<xsl:variable name="sub-properties.title">Sub Properties</xsl:variable>

	<!-- Labels -->
	<xsl:variable name="workbench.label">PoMS RDF Repository</xsl:variable>
	<xsl:variable name="repository-list.label">Repositories</xsl:variable>
	<xsl:variable name="system.label">System</xsl:variable>
	<xsl:variable name="information.label">Information</xsl:variable>
	<xsl:variable name="detail-information.label">
		Detail Information
	</xsl:variable>
	<xsl:variable name="summary.label">Summary</xsl:variable>
	<xsl:variable name="explore.label">Explore</xsl:variable>
	<xsl:variable name="namespaces.label">Namespaces</xsl:variable>
	<xsl:variable name="contexts.label">Contexts</xsl:variable>
	<xsl:variable name="types.label">Types</xsl:variable>
	<xsl:variable name="classes.label">Classes</xsl:variable>
	<xsl:variable name="properties.label">Properties</xsl:variable>
	<xsl:variable name="query.label">Query</xsl:variable>
	<xsl:variable name="rule-query.label">Rule Query</xsl:variable>
	<xsl:variable name="matcher-query.label">Matcher Query (Optional)</xsl:variable>
	<xsl:variable name="saved-queries.label">Saved Queries</xsl:variable>
	<xsl:variable name="export.label">Export</xsl:variable>
	<xsl:variable name="modify.label">Modify</xsl:variable>
	<xsl:variable name="update.label">Update</xsl:variable>
	<xsl:variable name="sparqlupdate.label">SPARQL Update</xsl:variable>
	<xsl:variable name="add.label">Add</xsl:variable>
	<xsl:variable name="remove.label">Remove</xsl:variable>
	<xsl:variable name="delete.label">Delete</xsl:variable>
	<xsl:variable name="create.label">Create</xsl:variable>
	<xsl:variable name="next.label">Next</xsl:variable>
	<xsl:variable name="previous.label">Previous</xsl:variable>
	<xsl:variable name="cancel.label">Cancel</xsl:variable>
	<xsl:variable name="show.label">Show</xsl:variable>
	<xsl:variable name="clear.label">Clear</xsl:variable>
	<xsl:variable name="execute.label">Execute</xsl:variable>
	<xsl:variable name="change-server.label">RDF4J Server URL</xsl:variable>
	<xsl:variable name="change.label">Change</xsl:variable>
	<xsl:variable name="server.label">RDF4J Server</xsl:variable>
	<xsl:variable name="server-user.label">User (optional)</xsl:variable>
	<xsl:variable name="server-password.label">Password (optional)</xsl:variable>
	<xsl:variable name="repository.label">Repository</xsl:variable>
	<xsl:variable name="download.label">Download</xsl:variable>
	<xsl:variable name="download-format.label">Download format</xsl:variable>
	<xsl:variable name="change-server.desc">for example: http://localhost:8080/rdf4j-server</xsl:variable>
	<xsl:variable name="repository-create.label">New repository</xsl:variable>
	<xsl:variable name="repository-delete.label">
		Delete repository
	</xsl:variable>
	<xsl:variable name="show-datatypes.label">Show data types &amp; language tags</xsl:variable>

	<!-- General labels -->
	<xsl:variable name="copyright.label">
							Data © People of Medieval Scotland, 2019 (All rights reserved), 
							RDF Engine © 2019 Eclipse RDF4J Contributors
	</xsl:variable>
	<xsl:variable name="true.label">Yes</xsl:variable>
	<xsl:variable name="false.label">No</xsl:variable>
	<xsl:variable name="none.label">None</xsl:variable>
	<xsl:variable name="all.label">All</xsl:variable>
	<xsl:variable name="readable.label">Readable</xsl:variable>
	<xsl:variable name="writeable.label">Writeable</xsl:variable>

	<!-- Fields -->
	<xsl:variable name="base-uri.label">Base URI</xsl:variable>
	<xsl:variable name="clear-warning.desc">
		WARNING: Clearing the repository will remove all statements.
		This operation cannot be undone.
	</xsl:variable>
	<xsl:variable name="remove-warning.desc">
		WARNING: Specifying only a context will remove all statements belonging to that context.
		This operation cannot be undone.
	</xsl:variable>
	<xsl:variable name="SYSTEM-warning.desc">
		WARNING: Modifying the SYSTEM repository directly is not
		advised.
	</xsl:variable>
	<xsl:variable name="clear-context.label">Clear Context(s)</xsl:variable>
	<xsl:variable name="context.label">Context</xsl:variable>
	<xsl:variable name="data-format.label">Data format</xsl:variable>
	<xsl:variable name="include-inferred.label">
		Include inferred statements
	</xsl:variable>
	<xsl:variable name="save-private.label">Save privately (do not share)</xsl:variable>
	<xsl:variable name="save.label">Save query</xsl:variable>
	<xsl:variable name="object.label">Object</xsl:variable>
	<xsl:variable name="predicate.label">Predicate</xsl:variable>
	<xsl:variable name="query-options.label">Action Options</xsl:variable>
	<xsl:variable name="query-actions.label">Actions</xsl:variable>
	<xsl:variable name="query-language.label">Query Language</xsl:variable>
	<xsl:variable name="query-string.label">Query</xsl:variable>
	<xsl:variable name="update-string.label">Update</xsl:variable>
	<xsl:variable name="subject.label">Subject</xsl:variable>
	<xsl:variable name="upload-file.desc">
		Select the file containing the RDF data you wish to upload
	</xsl:variable>
	<xsl:variable name="upload-file.label">RDF Data File</xsl:variable>
	<xsl:variable name="upload-text.desc">
		Enter the RDF data you wish to upload
	</xsl:variable>
	<xsl:variable name="upload-text.label">RDF Content</xsl:variable>
	<xsl:variable name="upload-url.desc">
		Location of the RDF data you wish to upload
	</xsl:variable>
	<xsl:variable name="upload-url.label">RDF Data URL</xsl:variable>
	<xsl:variable name="value-encoding.desc">
		Please specify subject, predicate, object and/or context of the
		statements that should be removed. Empty fields match with any
		subject, predicate, object or context. URIs, bNodes and literals should
		be entered using the N-Triples encoding. Example values in
		N-Triples encoding are:
	</xsl:variable>
	<xsl:variable name="result-limit.label">Results per page</xsl:variable>
	<xsl:variable name="result-offset.label">Results offset</xsl:variable>
	<xsl:variable name="limit10.label">10</xsl:variable>
	<xsl:variable name="limit50.label">50</xsl:variable>
	<xsl:variable name="limit100.label">100</xsl:variable>
	<xsl:variable name="limit200.label">200</xsl:variable>
	<xsl:variable name="result-limited.desc">
		The results shown may be truncated.
	</xsl:variable>
	<xsl:variable name="prefix.label">Prefix</xsl:variable>
	<xsl:variable name="namespace.label">Namespace</xsl:variable>
	<xsl:variable name="repository-type.label">Type</xsl:variable>
	<xsl:variable name="repository-id.label">ID</xsl:variable>
	<xsl:variable name="repository-title.label">Title</xsl:variable>
	<xsl:variable name="repository-persist.label">Persist</xsl:variable>
	<xsl:variable name="repository-sync-delay.label">Sync delay</xsl:variable>
	<xsl:variable name="repository-indexes.label">Triple indexes</xsl:variable>
	<xsl:variable name="repository-evaluation-mode.label">Evaluation mode</xsl:variable>
	<xsl:variable name="remote-repository-server.label">
		RDF4J Server locations
	</xsl:variable>
	<xsl:variable name="remote-repository-id.label">
		Remote repository ID
	</xsl:variable>
	<xsl:variable name="sparql-repository-query-endpoint.label">SPARQL query endpoint URL</xsl:variable>
	<xsl:variable name="sparql-repository-update-endpoint.label">SPARQL update endpoint URL (optional)</xsl:variable>
	<xsl:variable name="federation-members.label">Federation members</xsl:variable>
	<xsl:variable name="distinct.label">Distinct</xsl:variable>
	<xsl:variable name="read-only.label">Read-only</xsl:variable>
	<xsl:variable name="federation-type.label">Member type</xsl:variable>
	<xsl:variable name="jdbc-driver.label">JDBC Driver</xsl:variable>
	<xsl:variable name="jdbc-host.label">Host</xsl:variable>
	<xsl:variable name="jdbc-port.label">Port</xsl:variable>
	<xsl:variable name="jdbc-database.label">Database</xsl:variable>
	<xsl:variable name="jdbc-properties.label">
		Connection properties
	</xsl:variable>
	<xsl:variable name="jdbc-user.label">User Name</xsl:variable>
	<xsl:variable name="jdbc-password.label">Password</xsl:variable>
	<xsl:variable name="max-triple-tables.label">
		Maximum number of triple tables
	</xsl:variable>
	<xsl:variable name="resource.label">Resource</xsl:variable>


	<!-- Information Fields -->
	<xsl:variable name="application-information.title">
		Application Information
	</xsl:variable>
	<xsl:variable name="application-name.label">Application Name</xsl:variable>
	<xsl:variable name="application-version.label">Version</xsl:variable>
	<xsl:variable name="data-directory.label">Data Directory</xsl:variable>
	<xsl:variable name="java-runtime.label">Java Runtime</xsl:variable>
	<xsl:variable name="maximum-memory.label">Maximum</xsl:variable>
	<xsl:variable name="memory.title">Memory</xsl:variable>
	<xsl:variable name="memory-used.label">Used</xsl:variable>
	<xsl:variable name="operating-system.label">Operating System</xsl:variable>
	<xsl:variable name="process-user.label">Process User</xsl:variable>
	<xsl:variable name="runtime-information.title">
		Runtime Information
	</xsl:variable>
	<xsl:variable name="system-properties.title">
		System and Environment Properties
	</xsl:variable>
	<xsl:variable name="repository-location.label">Location</xsl:variable>
	<xsl:variable name="repository-size.label">Number of Statements</xsl:variable>
	<xsl:variable name="repository-contexts-size.label">Number of Labeled Contexts</xsl:variable>
	<xsl:variable name="number-of-namespaces.label">Namespaces</xsl:variable>
	<xsl:variable name="number-of-contexts.label">Contexts</xsl:variable>

</xsl:stylesheet>

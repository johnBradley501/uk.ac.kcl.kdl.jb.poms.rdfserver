<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$export.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template match="sparql:sparql">
			<p>Requesting an export of this server's data from this page has been disabled due to problems with very slow performance.
			You can fetch a zip file containing files holding the RDF statements served by this server from KDL's CKAN site
			<a href="https://data.kdl.kcl.ac.uk/dataset/people-of-medieval-scotland-project-1093-1371">here</a>.</p>
		<form action="export">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select">
								<xsl:with-param name="onchange">this.form.submit();</xsl:with-param>
								<xsl:with-param name="limit_id">limit_explore</xsl:with-param>
							</xsl:call-template>
						</td>
						<td id="result-limited">
							<xsl:if
								test="$info//sparql:binding[@name='default-limit']/sparql:literal = count(//sparql:result)">
								<xsl:value-of
									select="$result-limited.desc" />
							</xsl:if>
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<table class="data">
			<xsl:apply-templates select="*" />
		</table>
        <script src="../../scripts/paging.js" type="text/javascript">  </script>
        <script src="../../scripts/export.js" type="text/javascript">  </script>
	</xsl:template>

</xsl:stylesheet>

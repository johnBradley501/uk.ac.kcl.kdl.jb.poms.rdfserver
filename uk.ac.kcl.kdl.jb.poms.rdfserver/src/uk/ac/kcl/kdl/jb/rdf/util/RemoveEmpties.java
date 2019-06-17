package uk.ac.kcl.kdl.jb.rdf.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.impl.BackgroundGraphResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

// draws on examples in http://docs.rdf4j.org/programming/#_parsing_and_writing_rdf_with_rio
/*
 * This program removes what are considered "empty" statements from a dump of PoMS data from d2rq's rdf-dump program.
 * "empty" statements are those with object elements which are either:
 *    * empty strings
 *    * boolean values which are "false" (this may be a controversial decision...
 *    * URIs which begin with "file:". At present only :hasLinkToPoNE produces these
 *   
 * The name of the input file is provided as the first parameter, and the generated, filtered, output as the 2nd.
 * 
 * .. John Bradley June 2019
 */

public class RemoveEmpties {
	private static int countIn = 0, countOut = 0;

	public static void main(String[] args) {
		String inputFileName = args[0];
		String outputFileName = args[1];
		File inFile = new File(inputFileName);
		InputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			inputStream = new FileInputStream(inFile);
			outputStream = new FileOutputStream(outputFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		GraphQueryResult res = QueryResults.parseGraphBackground(inputStream, "file://phoney.file", RDFFormat.TURTLE);
		BackgroundGraphResult bgr = (BackgroundGraphResult)res;
		RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, outputStream);
		bgr.startRDF();
		writer.startRDF();
		Map<String,String> ns = res.getNamespaces();
		Iterator<String> it = ns.keySet().iterator();
		while(it.hasNext()){
			String name = it.next();
			String prefix = ns.get(name);
			writer.handleNamespace(name, prefix);
		}
		
		while(res.hasNext()){
			Statement st = res.next();
			countIn++;
			handleStatement(st, writer);
		}
		
		try {
			inputStream.close();
			writer.endRDF();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Finished: Number of statements in: "+countIn+", out: "+countOut);
	}

	private static void handleStatement(Statement st, RDFWriter writer) {
		Value object = st.getObject();
		if(object instanceof SimpleLiteral){
			SimpleLiteral litobj = (SimpleLiteral)object;
			if(litobj.getDatatype().getLocalName().equals(XMLSchema.STRING.getLocalName())){
				String val = litobj.stringValue();
				if(val.length() != 0)writeStatement(st, writer);
			} else if(litobj.getDatatype().getLocalName().equals(XMLSchema.BOOLEAN.getLocalName())){
				Boolean val = litobj.booleanValue();
				if (val)writeStatement(st, writer);
			} else {
				writeStatement(st, writer);
			}
		} else if(object instanceof IRI){
			String uri = ((IRI)object).stringValue();
			if (!uri.startsWith("file:"))writeStatement(st, writer);
		} else {
			writeStatement(st, writer);
		}
	}

	private static void writeStatement(Statement st, RDFWriter writer) {
		writer.handleStatement(st);
		countOut++;
	}

}

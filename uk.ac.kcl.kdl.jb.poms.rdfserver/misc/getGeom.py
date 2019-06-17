# this Python script reads the field "geom" in PoMS's MySQL database table pomsacc_place
# and generates a Turtle RDF file that creates a sequence of triples that represent this
# data as RDF -- making use of the opengis specifaction that is, in turn, supported by
# rdf4j.

# This script connects to the MySQL database, and so must invoke and then use the connector
# you have in your Python installation.  I happen to have the, now rather old, MySQLdb module
# for this purpose and use it here.  If you have the newer version, you will probably have to
# make the, hopefully relatively minor, changes to the script to make it work.

# You'll probably need to modify the values of the variables at the front of the script to
# specify how the connection to the MySQL database is to be made, and to indicate where the
# file that this script creates is to be stored.

#   -- John Bradley April 2019

import MySQLdb

DBmachine = "xxxxxx"
DBaccount = "xxxxxx"
DBpassword = "xxxxxx"
DBdatabase = "xxxxxx"

dirName = "/xxxxxx/d2rq-0.8.1/"
outfile_name = dirName+"poms_geom.ttl"


db = MySQLdb.connect(DBmachine, DBaccount, DBpassword, DBdatabase)
c = db.cursor()
c.execute("select id, ST_AsText(geom) from pomsapp_place where geom is not null")

outfile = open(outfile_name, "w")
outfile.write('@prefix vocab: <http://www.poms.ac.uk/rdf/ontology#> .\n')
outfile.write('@prefix geo: <http://www.opengis.net/ont/geosparql#> .\n')

for row in c:
    id = row[0]
    txt = row[1]
    outfile.write("<http://www.poms.ac.uk/rdf/entity/Place/"+str(id)+"> vocab:hasGeoData "+'"'+txt+'"^^geo:wktLiteral .\n')

outfile.close()
db.close()
    

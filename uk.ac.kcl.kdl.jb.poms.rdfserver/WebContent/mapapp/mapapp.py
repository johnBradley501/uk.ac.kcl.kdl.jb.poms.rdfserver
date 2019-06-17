# This Python script demonstrates how to use PoMS RDF server to fetch data tailored to a particular query
# and transform this data into an HTML display page. The page created takes data from the query and plots
# it onto an interactive geographic map of Scotland created by the leaflet software: https://leafletjs.com/
#
#                ... John Bradley DDH/KDL    June 2019

import urllib2
import json
import re
import io

path = "/research/PoMS-LOD/map/"
fileName = "map.html"
urlBase = "http://www.poms.ac.uk/rdf/endpoint"
uriBase = "http://www.poms.ac.uk/rdf/entity/"

#
# this query finds places associated with properties involved in charters where
# the grantor and beneficiary are both women.
#
query = u"""
PREFIX vocab: <http://www.poms.ac.uk/rdf/ontology#>
select ?grantor ?grantorURI ?factoid ?beneficiary ?beneURI ?possName ?label ?lat ?long
where {
 ?grantorURI a vocab:HistoricalFemale;
   vocab:hasID ?id;
   vocab:hasPersonDisplayName ?grantor.
 ?areference vocab:referencesPerson ?grantorURI;
    vocab:hasRole <http://www.poms.ac.uk/rdf/entity/Role/5>;
    vocab:hasFactoid ?factoid.
  ?factoid a vocab:TransactionFactoid.
  ?breference vocab:hasFactoid ?factoid;
    vocab:hasRole <http://www.poms.ac.uk/rdf/entity/Role/7>;
    vocab:referencesPerson ?beneURI.
  ?beneURI a vocab:HistoricalFemale;
     vocab:hasPersonDisplayName ?beneficiary.
  MINUS{?breference vocab:referencesPerson <http://www.poms.ac.uk/rdf/entity/Person/710>}
  ?possref a vocab:PossessionReference;
      vocab:hasFactoid ?factoid;
      vocab:referencesPossession ?possession.
  ?possession a vocab:PossessionLand;
      vocab:hasName ?possName;
      vocab:hasPlace ?possPlace.
  ?possPlace a vocab:Place;
      vocab:hasName ?label;
      vocab:hasGeographicLatitude ?lat;
      vocab:hasGeographicLongitude ?long.
}
order by ?grantor
"""

def getData():
    #
    # this query finds places associated with properties involved in charters where
    # the grantor and beneficiary are both women.
    #
    url = urlBase+"?query="+urllib2.quote(query.strip())
    req = urllib2.Request(url)
    req.add_header('Accept', "application/json")
    conn = urllib2.urlopen(req)
    rslt = json.loads(conn.read())
    conn.close()
    return rslt

def genBoringBit1(out):
    prefix = u"""<html>
<head>
<title>Map test</title>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.5.1/dist/leaflet.css"
   integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ=="
   crossorigin=""/>
<script src="https://unpkg.com/leaflet@1.5.1/dist/leaflet.js"
   integrity="sha512-GffPMF3RvMeYyc1LWMHtK8EbPv0iNZ8/oTtHPx9/cc2ILxQ+u905qIwdpULaqDkyBKgOaB57QTMg7ztg8Jm2Og=="
   crossorigin=""></script>
</head>
<body>
<h4>A map with plotted PoMS data</h4>
<hr>
<div id="mapid" style="height: 700px;"></div>
<script>
   var mymap = L.map('mapid').setView([56.860916, -4.251433], 7);

	L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw', {
		maxZoom: 18,
		attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, ' +
			'<a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
			'Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
		id: 'mapbox.satellite'
	}).addTo(mymap);

"""
    out.write(prefix)

def genBoringBit2(out):
    q1 = re.sub(r"<", r"&lt;",query)
    q1 = re.sub(r">", r"&gt;",q1)
    postfix = u"""
</script>
<h4>The Query</h4>
<pre>"""+q1+u"""
</pre>
</body>
</html>
    """
    out.write(postfix)

def processData(data):
    rslt = []
    lookup = {}
    vars = data["head"]["vars"]
    bindings = data["results"]["bindings"]
    for binding in bindings:
        iname = binding["label"]["value"]
        lat = binding["lat"]["value"]
        long = binding["long"]["value"]
        key = iname+lat+long
        if key in lookup:
            instance = lookup[key]
        else:
           instance = [iname, lat, long]
           lookup[key] = instance
           rslt.append(instance)
        pdata = []
        instance.append(pdata)
        for item in vars:
            name = item
            if name not in ["label","lat","long"]:
                val = binding[name]["value"]
                if binding[name]["type"] == "uri":
                    val = '<a href="'+val+'">'+val+'</a>'
                pdata.append([name, val])
    return rslt

def genInterestingBit(out, leafdata):
    for leafitem in leafdata:
        iname = leafitem[0]
        lat = leafitem[1]
        long = leafitem[2]
        rslt = u"    var marker = L.marker(["+lat+", "+long+"]).addTo(mymap);\n"
        popup = u"<table width=600>"
        sep = ""
        for pcont in leafitem[3:]:
            popup = popup+sep
            sep = u'<tr><td colspan=2><hr></td></tr>'
            for pitem in pcont:
               name = pitem[0]
               val = pitem[1]
               popup = popup+u"<tr><th>"+name+u"</th><td>"+val+u"</td></tr>"
        popup = popup+u"</table>"
        popup = re.sub(r'"',r'\\"',popup)
        popup = re.sub(r"\n",r"\\n", popup)
        rslt = rslt+u'    marker.bindPopup("'+popup+u'",{ maxWidth : 600});\n'
        rslt = rslt+u'    var tooltip = L.tooltip({permanent: true}).setContent("'+iname+u'");\n'
        rslt = rslt+u'    marker.bindTooltip(tooltip).openTooltip().closePopup();'
        out.write(rslt)

out = io.open(path+fileName,"w", encoding='utf-8')
genBoringBit1(out);
data = getData()
leafdata = processData(data)
genInterestingBit(out, leafdata);
genBoringBit2(out);
out.close()
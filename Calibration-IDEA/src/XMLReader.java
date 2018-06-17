/*********************************************************************************************
 *
 *
 * 'Reader.java', in plugin 'msi.gama.headless', is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 *
 *
 **********************************************************************************************/


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XMLReader {

	public String fileName;
	public InputStream myStream;

	// Map of Name of the variable and for each step the associated value
	HashMap<String,HashMap<Integer,String>> results;
	
	public void dispose() {
		this.fileName = null;
		try {
			this.myStream.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		myStream = null;
	}
	
	public HashMap<String,HashMap<Integer,String>> getMap() {
		return results;
	}

	public XMLReader(final String file) throws FileNotFoundException {
		fileName = file;
		myStream = new FileInputStream(new File(file));
		results = new HashMap<>();
	}

	public XMLReader(final InputStream inp) {
		myStream = inp;
	}

	private void readResults(final Document dom) {
		final NodeList ee = dom.getChildNodes();

		for (int i = 0; i < ee.getLength(); i++) {

			final List<Node> nl = findElementByNameWithoutCase(ee.item(i), "Simulation");
			if (nl != null && nl.size() > 0) {
				for (int j = 0; j < nl.size(); j++) {

					// get the employee element
					final Node el = nl.get(j);
					// add it to list
					this.readSteps(el);
				}
			}

		}
	}

	public void parseXmlFile() {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document dom = db.parse(myStream);
			this.readResults(dom);
		} catch (final ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (final SAXException se) {
			se.printStackTrace();
		} catch (final IOException ioe) {
			ioe.printStackTrace();
		}
	}	
	
	private String getAttributeWithoutCase(final Node e, final String flag) {
		final NamedNodeMap mp = e.getAttributes();
		final String lflag = flag.toLowerCase();
		for (int i = 0; i < mp.getLength(); i++) {
			final Node nd = mp.item(i);
			if (nd.getNodeName().toLowerCase().equals(lflag)) { return nd.getTextContent(); }
		}
		return null;
	}
	
	private List<Node> findElementByNameWithoutCase(final Node e, final String name) {
		final String lname = name.toLowerCase();
		final ArrayList<Node> res = new ArrayList<Node>();
		if (e.getNodeName().toLowerCase().equals(lname)) {
			res.add(e);
			return res;
		}
		final NodeList nl = e.getChildNodes();
		// System.out.println("get child "+ nl.getLength()+" "+name+ " "+e.getNodeName());
		for (int i = 0; i < nl.getLength(); i++) {
			final Node ee = nl.item(i);
			res.addAll(findElementByNameWithoutCase(ee, name));

		}
		return res;
	}	

	private void readVariable(String id, Node var) {
		final String name = getAttributeWithoutCase(var, "name");
		final String value = var.getTextContent();
		
		if(! results.containsKey(name)) {
			HashMap<Integer,String> values = new HashMap<>();
			results.put(name, values);
		}
		
		results.get(name).put(Integer.parseInt(id), value);
	}

	private void readSteps(final Node docEle) {
		final List<Node> nl = findElementByNameWithoutCase(docEle, "Step");
		if (nl != null && nl.size() > 0) {
			for (int i = 0; i < nl.size(); i++) {

				// get the Step element
				final Node el = nl.get(i);

				// get the Employee object
				// final Parameter e = this.readParameter(el);
				final String id = getAttributeWithoutCase(el, "id");

				List<Node> nlVariables = findElementByNameWithoutCase(el, "Variable");
				
				if (nlVariables != null && nlVariables.size() > 0) {

					for (Node var : nlVariables) {
						this.readVariable(id,var);
					}
				}
			}
		}
	}
	
	public String getFinalValueOf(String varName) {
		if(!results.containsKey(varName)) {
			return "";
		}
		
		HashMap<Integer,String> varResults = results.get(varName);
		Integer maxIndex = varResults.keySet().stream().max(Comparator.comparing(Integer::valueOf)).get();
		return varResults.get(maxIndex);
	}
}

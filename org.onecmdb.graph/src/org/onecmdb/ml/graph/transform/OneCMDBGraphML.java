package org.onecmdb.ml.graph.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.omg.IOP.ComponentIdHelper;
import org.onecmdb.ml.graph.model.CIEdge;
import org.onecmdb.ml.graph.model.CINode;


public class OneCMDBGraphML {
	protected ArrayList<CIEdge> edges = new ArrayList<CIEdge>();
	protected ArrayList<CINode> nodes = new ArrayList<CINode>();
	protected HashMap<String, CINode> nodeMap = new HashMap<String, CINode>();
	protected CINode undefinedNode = new CINode("undefined", "undefined");
	protected boolean graphParsed;
	//protected IOneCMDBWebService service;
	//protected String token;
	
	/*
	<?xml version="1.0" encoding="UTF-8"?>
	<!--  An excerpt of an egocentric social network  -->
	<graphml xmlns="http://graphml.graphdrawing.org/xmlns">
	<graph edgedefault="undirected">
	 
	<!-- data schema -->
	<key id="name" for="node" attr.name="name" attr.type="string"/>
	<key id="gender" for="node" attr.name="gender" attr.type="string"/>
	  
	<!-- nodes -->  
	<node id="1">
	 <data key="name">Jeff</data>
	 <data key="gender">M</data>
	 </node>
	<node id="2">
	...
	<!-- edges -->
	<edge source="1" target="2"></edge>
	<edge source="1" target="3"></edge>
	<edge source="1" target="4"></edge>
	<edge source="1" target="5"></edge>
	*/
	public void printSchema(PrintWriter pw) {
		 pw.println("<key id=\"id\" for=\"node\" attr.name=\"id\" attr.type=\"string\"/>");
		  pw.println("<key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>");
		  pw.println("<key id=\"image\" for=\"node\" attr.name=\"image\" attr.type=\"string\"/>");
		  pw.println("<key id=\"name\" for=\"node\" attr.name=\"name\" attr.type=\"string\"/>");
		  pw.println("<key id=\"distance\" for=\"node\" attr.name=\"distance\" attr.type=\"integer\"/>");
		  pw.println("<key id=\"aggregate\" for=\"node\" attr.name=\"aggregate\" attr.type=\"string\"/>");
		  pw.println("<key id=\"alias\" for=\"node\" attr.name=\"alias\" attr.type=\"string\"/>");
		  pw.println("<key id=\"checked\" for=\"node\" attr.name=\"checked\" attr.type=\"boolean\"/>");
		  pw.println("<key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\"/>");
		 
		  pw.println("<key id=\"label\" for=\"edge\" attr.name=\"label\" attr.type=\"string\"/>");
		  pw.println("<key id=\"springLength\" for=\"edge\" attr.name=\"springLength\" attr.type=\"float\"/>");
		  pw.println("<key id=\"springCoefficient\" for=\"edge\" attr.name=\"springCoefficient\" attr.type=\"float\"/>");
		  pw.println("<key id=\"edgeType\" for=\"edge\" attr.name=\"edgeType\" attr.type=\"string\"/>");
		  pw.println("<key id=\"type\" for=\"edge\" attr.name=\"type\" attr.type=\"string\"/>");
		  pw.println("<key id=\"derived\" for=\"edge\" attr.name=\"derived\" attr.type=\"string\"/>");
	}
	
	public void toGraphML(PrintWriter out, final String sort) {
		System.out.println("Start Writing....");
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">");
		out.println("<graph edgedefault=\"directed\">");
		printSchema(out);
		
		List<CINode> nodes = getNodes();
		Collections.sort(nodes, new Comparator<CINode>() {

			public int compare(CINode arg0, CINode arg1) {
				if (arg1.getId().equals(sort)) {
					return(1);
				}
				return(0);
			}
			
		});
		for (CINode node : nodes) {
			node.toGraphML(out);
		}
		for (CIEdge edge : getEdges()) {
			edge.toGraphML(out);
		}
		
		
		
		out.println("</graph>");
		out.println("</graphml>");
		out.flush();
		System.out.println("End writing....");
	}
	
	public InputStream getInputStream(final String sort) throws IOException {
		
		PipedOutputStream pipeOut = new PipedOutputStream();
		PipedInputStream in = new PipedInputStream(pipeOut);
	    OutputStreamWriter osw = new OutputStreamWriter(pipeOut, "UTF8");
	    
		final PrintWriter out = new PrintWriter(osw);
		new Thread(new Runnable() {
			public void run() {
				toGraphML(out, sort);
				out.close();
			}
		}).start();
		
		return(in);
	}


	public List<CIEdge> getEdges() {
		return(this.edges);	}


	public List<CINode> getNodes() {
		return(this.nodes);
	}

	
	public CIEdge addEdge(CINode source, CINode target) {
		CIEdge edge = new CIEdge(source, target);
		this.edges.add(edge);
		return(edge);
	}
	
	public CINode addNode(CINode addNode) {
		CINode node = nodeMap.get(addNode.getId());
		if (node != null) {
			return(node);
		}
		this.nodeMap.put(addNode.getId(), addNode);
		this.nodes.add(addNode);
		return(addNode);
	}
}

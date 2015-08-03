/*
 * Lokomo OneCMDB - An Open Source Software for Configuration
 * Management of Datacenter Resources
 *
 * Copyright (C) 2006 Lokomo Systems AB
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 * 
 * Lokomo Systems AB can be contacted via e-mail: info@lokomo.com or via
 * paper mail: Lokomo Systems AB, Svärdvägen 27, SE-182 33
 * Danderyd, Sweden.
 *
 */
package org.onecmdb.ml.graph.main.graphquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.DocumentException;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.bean.ValueBean;
import org.onecmdb.core.utils.graph.query.GraphQuery;
import org.onecmdb.core.utils.graph.result.Graph;
import org.onecmdb.core.utils.graph.result.Template;
import org.onecmdb.ml.graph.main.CEvents;
import org.onecmdb.ml.graph.main.Event;
import org.onecmdb.ml.graph.main.EventDispatcher;
import org.onecmdb.ml.graph.main.OneCMDBConnection;
import org.onecmdb.ml.graph.utils.XML2GraphQuery;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;

public class GraphViewControl {
	
	prefuse.data.Graph g = new prefuse.data.Graph(true);

	private Graph result;

	private HashMap<String, Node> queryGraphNodeMap = new HashMap<String, Node>();
	
	
	
	public GraphViewControl() {
		// Add Model
		g.addColumn("alias", String.class);
		g.addColumn("type", String.class);
		g.addColumn("mark", Boolean.class);
		g.addColumn("name", String.class);
		
		// On Edge
		g.addColumn("springCoefficient", float.class);
		g.addColumn("springLength", float.class);
		
		// On Node
		g.addColumn("massValue", float.class);
		g.addColumn("image", String.class);
		
	}

	public prefuse.data.Graph getGraph() {
		return(g);
	}
	
	
	
	
	
	
	public void reloadGraph(String xml) throws DocumentException {
		
		XML2GraphQuery xml2Graph = new XML2GraphQuery();
		GraphQuery query = xml2Graph.parse(xml);
		
		result = OneCMDBConnection.instance().getCmdbService().queryGraph(OneCMDBConnection.instance().getToken(), query);
		
		EventDispatcher.fireEvent(this, new Event(CEvents.STATUS_MSG, "Load result:" + result.toString()));
		
		result.buildMap();
		
		updateGraph();
	}
	
	public Graph getResult() {
		return(this.result);
	}
	
	public void updateGraph() {
		HashMap<String, CiBean> nodeBeans = new HashMap<String, CiBean>();
		HashMap<String, CiBean> edgeBeans = new HashMap<String, CiBean>();
			
		for (Template t : result.getNodes()) {
			for (CiBean bean : t.getOffsprings()) {
				nodeBeans.put(bean.getAlias(), bean);
			}
		}
		for (Template t : result.getEdges()) {
			for (CiBean bean : t.getOffsprings()) {
				edgeBeans.put(bean.getAlias(), bean);
			}
		}
		
		// Syncronize the graph from concurrrent modifications.
		synchronized(g) {
		
			HashMap<String, Node> nodeMap = new HashMap<String, Node>();
			// handle update of nodes.
			List<Node> removeNode = new ArrayList<Node>();
			
			Iterator nIter = g.getNodes().tuples();
			while (nIter.hasNext()) {
				Object o = nIter.next();
				if (o instanceof Tuple) {
					Tuple t = (Tuple) o;
				
					if (t.canGetString("alias")) {
						String alias = t.getString("alias");
						if (result.findOffspringAlias(alias) == null) {
							System.out.println("Remove Node " + alias);
							Node n = g.getNode(t.getRow());
							System.out.println("\t" + n);
							removeNode.add(n);
						} else {
							nodeMap.put(alias, g.getNode(t.getRow()));
							if (nodeBeans.containsKey(alias)) {
								nodeBeans.remove(alias);
							}

						}
					}
				}
			}
			
			List<Edge> removeEdge = new ArrayList<Edge>();
			Iterator eIter = g.getEdges().tuples();
			while (eIter.hasNext()) {
				Object o = eIter.next();
				if (o instanceof Tuple) {
					Tuple t = (Tuple)o;
					if (t.canGetString("alias")) {
						String alias = t.getString("alias");
						if (result.findEdgeBean(alias) == null) {
							System.out.println("Remove Edge " + alias);
							Edge e = g.getEdge(t.getRow());
							System.out.println("\t" + e);
							removeEdge.add(e);
						}
						if (edgeBeans.containsKey(alias)) {
							edgeBeans.remove(alias);
						}
					}
				}
			}
			
			
			// Start update the graph
			// remove first.
			for (Edge e : removeEdge) {
				g.removeEdge(e);
			}
			for (Node n : removeNode) {
				g.removeNode(n);
			}
			
			
			for (CiBean bean : nodeBeans.values()) {
				Node n = g.addNode();
				n.set("alias", bean.getAlias());
				n.set("type", bean.getDerivedFrom());
				String displayName = bean.getDisplayName();
				if (displayName.length() > 20) {
					displayName = displayName.substring(0, 20) + "...";
				} else if (displayName.length() == 0) {
					displayName = "[" + bean.getAlias() + "]";
				}
				n.set("name", displayName);
				n.set("image", "http://localhost:8080/icons/generate?iconid=" + getValue(bean, "icon"));
				nodeMap.put(bean.getAlias(), n);
			}
			
			for (CiBean bean : edgeBeans.values()) {
				String sourceAlias = getSource(bean);
				String targetAlias = getTarget(bean);
				Node source = nodeMap.get(sourceAlias);
				Node target = nodeMap.get(targetAlias);
				if (source == null) {
					continue;
				}
				if (target == null) {
					continue;
				}
				Edge e = g.addEdge(source, target);
				e.set("alias", bean.getAlias());
				e.set("type", bean.getDerivedFrom());
				e.set("name", bean.getDerivedFrom());
			}
		} // End of synchronized graph modification.	
	}

	private String getValue(CiBean bean, String alias) {
		ValueBean vBean = bean.fetchAttributeValueBean(alias, 0);
		if (vBean == null) {
			return(null);
		}
		if (vBean.hasEmptyValue()) {
			return(null);
		}
		return(vBean.getValue());
	}

	private String getSource(CiBean bean) {
		return(getValue(bean, "source"));
	}
	
	private String getTarget(CiBean bean) {
		return(getValue(bean, "target"));
	}
	
}

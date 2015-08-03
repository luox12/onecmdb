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
package org.onecmdb.ml.graph.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.onecmdb.core.internal.storage.hibernate.PageInfo;
import org.onecmdb.core.utils.bean.AttributeBean;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.bean.ValueBean;
import org.onecmdb.core.utils.graph.query.GraphQuery;
import org.onecmdb.core.utils.graph.query.constraint.ItemConstraint;
import org.onecmdb.core.utils.graph.query.constraint.ItemOrGroupConstraint;
import org.onecmdb.core.utils.graph.query.constraint.ItemSecurityConstraint;
import org.onecmdb.core.utils.graph.query.selector.ItemOffspringSelector;
import org.onecmdb.core.utils.graph.query.selector.ItemRelationSelector;
import org.onecmdb.core.utils.graph.query.selector.ItemSelector;
import org.onecmdb.core.utils.graph.result.Graph;
import org.onecmdb.core.utils.graph.result.Template;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;



public class GraphControl {
	GraphQuery q = new GraphQuery();
	
	prefuse.data.Graph g = new prefuse.data.Graph(true);

	private Graph result;

	private boolean excludeRelation = true;

	private prefuse.data.Graph queryGraph = new prefuse.data.Graph(true);

	private HashMap<String, Node> queryGraphNodeMap = new HashMap<String, Node>();

	private Integer maxSize = 500;

	private String group;

	
	
	
	public GraphControl() {
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
		
		
		// Add Model
		queryGraph.addColumn("alias", String.class);
		queryGraph.addColumn("type", String.class);
		queryGraph.addColumn("mark", Boolean.class);
		queryGraph.addColumn("name", String.class);
		queryGraph.addColumn("id", String.class);
		queryGraph.addColumn("image", String.class);
		
		
	}

	public prefuse.data.Graph getGraph() {
		return(g);
	}
	
	public ItemSelector addItem(String id, String alias, boolean primary) {
		ItemSelector selector = q.findSelector(id);
		if (selector == null) {
			selector = new ItemOffspringSelector(id, alias);
			selector.setPrimary(primary);
			q.addSelector(selector);
			
			selector.setPageInfo(new PageInfo(0, maxSize ));
			
			// Update query Graph.
			CiBean template = OneCMDBConnection.instance().getBeanFromAlias(alias);
			Node n = queryGraph.addNode();
			n.setString("id",id);
			n.setString("name", alias);
			n.setString("alias", alias);
			if (template != null) {
				n.set("image", "http://localhost:8080/icons/generate?iconid=" + getValue(template, "icon"));
			}
			queryGraphNodeMap.put(id, n);
		}
		
		
		return(selector);
	}
	
	private ItemConstraint getSecurityConstraint() {
		if (group == null || group.length() == 0) {
			return(null);
		}
		String groups[] = group.split("\\|");
		if (groups.length == 1) {
			ItemSecurityConstraint security = new ItemSecurityConstraint();
			security.setGroupName(this.group);
			return(security);
		}
		ItemOrGroupConstraint or = new ItemOrGroupConstraint();

		for (int i = 0; i < groups.length; i++) {
			ItemSecurityConstraint security = new ItemSecurityConstraint();
			security.setGroupName(groups[i]);
			or.add(security);
		}
		return(or);
	}

	public void setSecurityGroup(String alias) {
		this.group = alias;
	}
	
	public boolean removeItem(String id) {
		ItemSelector selector = q.findSelector(id);
		if (selector == null) {
			return(false);
		}
		
		// Update Graph.
		if (selector instanceof ItemRelationSelector) {
			
			Iterator iter = queryGraph.edges();
			while (iter.hasNext()) {
				Edge e = (Edge)iter.next();
				if (e.getString("id").equals(id)) {
					queryGraph.removeEdge(e);
				}
			}
		} else {
			Iterator iter = queryGraph.nodes();
			while (iter.hasNext()) {
				Node n = (Node)iter.next();
				if (n.getString("id").equals(id)) {
					queryGraph.removeNode(n);
				}
			}
			queryGraphNodeMap.remove(id);
		}
		// Remove Selector, will also remove reference selectors.
		return(q.removeSelector(selector));
	}
	
	
	public ItemRelationSelector addRelation(String sourceId, String refType, String targetId, String primary) {
		String relId = sourceId + "->" + refType + "->" + targetId;
		String sId = sourceId;
		String tId = targetId;
		
		ItemSelector source = q.findSelector(sId);
		if (source == null) {
			source = addItem(sId, sourceId, sourceId.equals(primary));
		}
		ItemSelector target = q.findSelector(tId);
		if (target == null) {
			target = addItem(tId, targetId, targetId.equals(primary));
		}
		
		
		ItemSelector relation = q.findSelector(relId);
		if (relation == null) {
			relation = new ItemRelationSelector(relId, refType, target.getId(), source.getId());
			q.addSelector(relation);
			if (excludeRelation) {
				((ItemRelationSelector)relation).setMandatory(false);
			}
			// Add relation...
			Node sNode = queryGraphNodeMap.get(sourceId);
			Node tNode = queryGraphNodeMap.get(targetId);
			if (sNode != null && tNode != null) {
				Edge e = queryGraph.addEdge(sNode, tNode);
				e.setString("alias", refType);
				e.setString("id", relId);
			}
		}
		return((ItemRelationSelector)relation);
	}
	
	public boolean removeRelation(String sourceId, String refType, String targetId, String clicked) {
		String relId = sourceId + "->" + refType + "->" + targetId;
		String sId = sourceId;
		String tId = targetId;
	
		removeItem(relId);
		removeItem(clicked);
		//removeItem(tId);
		
		return(true);
	}	
	
	
	public void update() {
		// Update security constraint.
		ItemConstraint security = getSecurityConstraint();
		List<ItemOffspringSelector> sels = q.getItemOffspringSelector();
		if (sels != null) {
			for (ItemOffspringSelector sel : sels) {
				if (sel.isPrimary()) {
					sel.applyConstraint(security);
				}
			}
		}
		

		
		result = OneCMDBConnection.instance().getCmdbService().queryGraph(OneCMDBConnection.instance().getToken(), q);
		System.out.println(result.toString());
		
		result.buildMap();
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

	public void setExclude(boolean selected) {
		this.excludeRelation = selected;
		
	}
	
	public List<CiBean> graphBeanTemplate() {
		List<CiBean> beans = new ArrayList<CiBean>();
		if (true) {
			return(beans);
		}
		CiBean graph = new CiBean();
		graph.setAlias("GraphQuery");
		AttributeBean selA = new AttributeBean("Selector(s)", "selector", "ItemSelector", "ComposedOf", true);
		selA.setMaxOccurs("unbound");
		selA.setMinOccurs("0");
		graph.addAttribute(selA);
		beans.add(graph);

		/*
		CiBean itemConstraint = new CiBean();
		itemConstraint.setAlias("ItemSelector");
		itemConstraint.addAttribute(new AttributeBean("template", "xs:string", null, false));
		
		beans.add(itemSelector);
		*/
		
		
		CiBean itemSelector = new CiBean();
		itemSelector.setAlias("ItemSelector");
		itemSelector.addAttribute(new AttributeBean("template", "xs:string", null, false));
		
		beans.add(itemSelector);
		
		CiBean itemOffspringSelector = new CiBean();
		itemOffspringSelector.setDerivedFrom("ItemSelector");
		itemOffspringSelector.setAlias("ItemOffspringSelector");
		itemOffspringSelector.addAttribute(new AttributeBean("Template", "template", "xs:string", null, false));
		beans.add(itemOffspringSelector);
		
		CiBean itemRelationSelector = new CiBean();
		itemRelationSelector.setAlias("ItemRelationSelector");
		itemRelationSelector.setDerivedFrom("ItemSelector");
		itemRelationSelector.addAttribute(new AttributeBean("Template", "template", "xs:string", null, false));
		itemRelationSelector.addAttribute(new AttributeBean("Source", "source", "ItemSelector", "PointsTo", true));
		itemRelationSelector.addAttribute(new AttributeBean("Target", "target", "ItemSelector", "PointsTo", true));
		beans.add(itemRelationSelector);
		
		return(beans);
	}
	
	public List<CiBean> graphToBean() {
		List<CiBean> beans = new ArrayList<CiBean>();
		CiBean graph = new CiBean();
		graph.setAlias("GraphQuery-1");
		graph.setDerivedFrom("GraphQuery");
		graph.setDisplayName(graph.getAlias());
		
		beans.add(graph);
		
		for (ItemSelector sel : q.fetchSelectors()) {
			CiBean selBean = itemSelectorToBean(sel);
			if (selBean != null) {
				beans.add(selBean);
				graph.addAttributeValue(new ValueBean("selector", selBean.getAlias(), true));
			}
		}
		return(beans);
	}
	
	private CiBean itemSelectorToBean(ItemSelector sel) {
		
		if (sel instanceof ItemOffspringSelector) {
			CiBean offspringSel = new CiBean();
			offspringSel.setAlias("ItemSelector-" + sel.getId());
			offspringSel.setDisplayName(sel.getId());
			offspringSel.setDerivedFrom("ItemOffspringSelector");
			offspringSel.addAttributeValue(new ValueBean("template", sel.getTemplateAlias(), false));
			return(offspringSel);
		}
		
		if (sel instanceof ItemRelationSelector) {
			ItemRelationSelector rel = (ItemRelationSelector)sel;
			CiBean offspringSel = new CiBean();
			offspringSel.setAlias("ItemSelector-" + sel.getId());
			offspringSel.setDisplayName(sel.getId());
			offspringSel.setDerivedFrom("ItemRelationSelector");
			offspringSel.addAttributeValue(new ValueBean("template", sel.getTemplateAlias(), false));
			offspringSel.addAttributeValue(new ValueBean("source", "ItemSelector-" + rel.getSource(), true));
			offspringSel.addAttributeValue(new ValueBean("target", "ItemSelector-" + rel.getTarget(), true));
			return(offspringSel);
		}
		return(null);
		
	}

	/**
	 * Return the graph of the query
	 * 
	 * @return
	 */
	public prefuse.data.Graph getQueryGraph() {
		return(this.queryGraph);
	}
}

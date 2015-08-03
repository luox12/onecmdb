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

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.onecmdb.core.internal.storage.hibernate.PageInfo;
import org.onecmdb.core.utils.bean.AttributeBean;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.bean.ValueBean;
import org.onecmdb.core.utils.graph.query.GraphQuery;
import org.onecmdb.core.utils.graph.query.selector.ItemOffspringSelector;
import org.onecmdb.core.utils.graph.result.Graph;
import org.onecmdb.core.utils.graph.result.Template;
import org.onecmdb.ml.graph.applet.AppletLogger;
import org.onecmdb.ml.graph.model.CIEdge;
import org.onecmdb.ml.graph.model.CINode;
import org.onecmdb.ml.graph.transform.OneCMDBGraphML;

import prefuse.data.io.GraphMLReader;
import prefuse.util.ui.JPrefuseTable;

public class TemplateModel {
	//private static TemplateModel templateModel;
	prefuse.data.Graph templateGraph;
	prefuse.data.Graph referenceGraphAll;
	prefuse.data.Graph referenceGraph;
	
	private HashMap<String, CiBean> beanMap = new HashMap<String, CiBean>();
	private String root;
	private Integer totalInstanceCount = 0;
	private int totalTemplateCount = 0;
	private long loadTemplateTime;
	private long loadedInstanceCountTime;
	private static HashMap<String, TemplateModel> models = new HashMap<String, TemplateModel>();
	
	public TemplateModel(String root) {
		this.root = root;
	}
	
	public static void reset() {
		for (TemplateModel tModel : models.values()) {
			tModel.update();
		}
		//models.clear();
	}
	
	public static TemplateModel get(String root) {
		TemplateModel templateModel = models.get(root);
		if (templateModel == null) {
			templateModel = new TemplateModel(root);
			templateModel.update();
			models.put(root, templateModel);
		}
		return(templateModel);
	}
	
	public prefuse.data.Graph getTemplateGraph() {
		return(templateGraph);
	}
	
	public prefuse.data.Graph getReferenceGraph() {
		return(referenceGraph);
	}
	
	public prefuse.data.Graph getReferenceGraphAll() {
		return(referenceGraphAll);
	}
	

	public CiBean getBean(String alias) {
		return(beanMap.get(alias));
	}
	
	public Map<String, CiBean> getBeanMap() {
		return(beanMap);
	}
	
	private void update() {
		ItemOffspringSelector ci = new ItemOffspringSelector("ci", root);
		ci.setMatchTemplate(true);
		ci.setPrimary(true);
		
		
		GraphQuery q = new GraphQuery();
		q.addSelector(ci);
		
		AppletLogger.showMessage("Query CMDB...");
		Graph result = null;
		long start = System.currentTimeMillis();
		result = OneCMDBConnection.instance().getCmdbService().queryGraph(OneCMDBConnection.instance().getToken(), q);
		long stop = System.currentTimeMillis();
		
		System.out.println(result.toString());
		
		result.buildMap();
		
		totalTemplateCount = result.fetchAllNodeOffsprings().size() + 1;
		loadTemplateTime = stop-start;
		// Query for number of instances...
		GraphQuery instanceQuery = new GraphQuery();
		ItemOffspringSelector offsprings = new ItemOffspringSelector("offsprings", root);
		offsprings.setMatchTemplate(false);
		offsprings.setPrimary(true);
		offsprings.setPageInfo(new PageInfo(0,1));
		instanceQuery.addSelector(offsprings);
		
		start = System.currentTimeMillis();
		Graph instanceResult = OneCMDBConnection.instance().getCmdbService().queryGraph(OneCMDBConnection.instance().getToken(), instanceQuery);
		stop = System.currentTimeMillis();
		loadedInstanceCountTime = stop-start;
		Template t = instanceResult.fetchNode(offsprings.getId());	
		totalInstanceCount = t.getTotalCount();
		
		AppletLogger.showMessage("Received " + result.fetchAllNodeOffsprings().size() + " CI:s");
		
		Template c = result.fetchNode(ci.getId());
		
		templateGraph = new prefuse.data.Graph();
		referenceGraphAll = new prefuse.data.Graph();
		referenceGraph = new prefuse.data.Graph();
				
		
		OneCMDBGraphML mlTemplate = new OneCMDBGraphML();
		OneCMDBGraphML mlReference = new OneCMDBGraphML();
		OneCMDBGraphML mlReferenceAll = new OneCMDBGraphML();
		
		if (c.getOffsprings() == null || c.getOffsprings().size() == 0) {
			
			CINode notFound = new CINode(root, "Empty Model for [" + root + "]");
			mlTemplate.addNode(notFound);
			mlReference.addNode(notFound);
			mlReferenceAll.addNode(notFound);
		} else {
			for (CiBean ciBean : c.getOffsprings()) {
				CINode parent = new CINode(ciBean.getDerivedFrom(), ciBean.getDerivedFrom());
				CINode child = new CINode(ciBean.getAlias(), "http://localhost:8080/" + "icons/generate?iconid=" + getIcon(ciBean), ciBean.getAlias());

				child = mlTemplate.addNode(child);
				parent = mlTemplate.addNode(parent);
				CIEdge inheritance = mlTemplate.addEdge(parent, child);

				beanMap.put(ciBean.getAlias(), ciBean);
			}

			for (CiBean ciBean : c.getOffsprings()) {

				// References...
				CINode child = new CINode(ciBean.getAlias(), "http://localhost:8080/" + "icons/generate?iconid=" + getIcon(ciBean), ciBean.getAlias());

				for (AttributeBean aBean : ciBean.getAttributes()) {
					/*
				if (aBean.isDerived()) {
					continue;
				}
					 */
					if (aBean.isComplexType()) {
						String type = aBean.getType();
						// Fetch all offsprings of type...
						connectAllOffsprings(c, mlReferenceAll, type, aBean.getRefType(), aBean.isDerived(), child, true);
					}
				}
			}

			for (CiBean ciBean : c.getOffsprings()) {

				// References...
				CINode child = new CINode(ciBean.getAlias(), "http://localhost:8080/" + "icons/generate?iconid=" + getIcon(ciBean), ciBean.getAlias());

				for (AttributeBean aBean : ciBean.getAttributes()) {

					if (aBean.isDerived()) {
						continue;
					}

					if (aBean.isComplexType()) {
						String type = aBean.getType();
						// Fetch all offsprings of type...
						connectAllOffsprings(c, mlReference, type, aBean.getRefType(), aBean.isDerived(), child, false);
					}
				}
			}
		}

		try {
			mlTemplate.toGraphML(new PrintWriter(System.out), root);
			templateGraph = (prefuse.data.Graph)new GraphMLReader().readGraph(mlTemplate.getInputStream(root));
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		/*
		try {
			referenceGraphAll = (prefuse.data.Graph)new GraphMLReader().readGraph(mlReferenceAll.getInputStream(root));
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		try {
			referenceGraph = (prefuse.data.Graph)new GraphMLReader().readGraph(mlReference.getInputStream(root));
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		*/
		//JPrefuseTable.showTableWindow(templateGraph.getNodeTable());
		//JPrefuseTable.showTableWindow(referenceGraph.getNodeTable());
					
	}
	
	private void connectAllOffsprings(Template t, OneCMDBGraphML mlReference, String type,
			String refType, boolean derived, CINode child, boolean all) {
		
		mlReference.addNode(child);
		
		CINode to = new CINode(type, type);
		to = mlReference.addNode(to);
		
		CIEdge relation = mlReference.addEdge(child, to);
		
		relation.setProperty("type", refType);
		relation.setProperty("derived", ""  + derived);
		
		// Find all offspprings of type.
		if (all) {
			for (CiBean b : t.getOffsprings()) {
				if (b.getDerivedFrom().equals(type)) {
					connectAllOffsprings(t, mlReference, b.getAlias(), refType, true, child, all);
				}
			}
		}
		
		
	}

	private String getIcon(CiBean ciBean) {
		ValueBean icon = ciBean.fetchAttributeValueBean("icon", 0);
		if (icon == null) {
			return("ci");
		}
		if (icon.hasEmptyValue()) {
			return("ci");
		}
		return(icon.getValue());
	}
	
	public String getTemplateInfoAsHTML() {
		StringBuffer b = new StringBuffer();
		b.append("<html>");
		b.append("OneCMDB contains " + totalTemplateCount + " templates<br>");
		b.append("OneCMDB contaons " + totalInstanceCount + " instances<br>");
		b.append("<hr size=\"1\">");
		b.append("Loaded templates in " + loadTemplateTime + "ms<br>");
		b.append("Query instance count in " + loadedInstanceCountTime + "ms");
		b.append("</html>");
		return(b.toString());
	}

}

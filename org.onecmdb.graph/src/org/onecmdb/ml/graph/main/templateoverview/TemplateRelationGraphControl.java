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
package org.onecmdb.ml.graph.main.templateoverview;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.onecmdb.core.utils.bean.AttributeBean;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.bean.ValueBean;
import org.onecmdb.core.utils.graph.result.Graph;
import org.onecmdb.ml.graph.applet.AppletLogger;
import org.onecmdb.ml.graph.main.OneCMDBConnection;
import org.onecmdb.ml.graph.main.TemplateModel;

import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.data.Tuple;

public class TemplateRelationGraphControl {
	
	prefuse.data.Graph g = new prefuse.data.Graph(true);

	private Graph result;

	private HashMap<String, Node> nodeMap = new HashMap<String, Node>();

	private TemplateModel tModel;
		
	public TemplateRelationGraphControl(TemplateModel tModel) {
		// Add Model
		g.addColumn("alias", String.class);
		g.addColumn("type", String.class);
		g.addColumn("mark", Boolean.class);
		g.addColumn("name", String.class);
		g.addColumn("checked", boolean.class);
		
		// On Edge
		g.addColumn("springCoefficient", float.class);
		g.addColumn("springLength", float.class);
		
		// On Node
		g.addColumn("massValue", float.class);
		g.addColumn("image", String.class);
		
		this.tModel = tModel;
		
	}

	public prefuse.data.Graph getGraph() {
		return(g);
	}
	
	public Node reloadGraph(String alias) {
		Map<String, CiBean> map = tModel.getBeanMap();
		CiBean root = map.get(alias);
		if (root == null) {
			AppletLogger.showMessage("Template '" + alias + "' is not found!");
			throw new IllegalArgumentException("Template '" + alias + "' is not found!");
		}
		//g.removeAllSets();
		
		// Clear graph
		// Start by edges
		
		Iterator eIter = g.getEdges().tuples();
		while (eIter.hasNext()) {
			Object o = eIter.next();
			if (o instanceof Tuple) {
				Tuple t = (Tuple) o;
				Edge e = g.getEdge(t.getRow());
				g.removeEdge(e);
			}
		}
		
		Iterator nIter = g.getNodes().tuples();
		while (nIter.hasNext()) {
			Object o = nIter.next();
			if (o instanceof Tuple) {
				Tuple t = (Tuple) o;
				Node n = g.getNode(t.getRow());
				g.removeNode(n);
			}
		}
		
		nodeMap.clear();
		
		// Always add ourselft.
		Node rootNode = getNode(root);
		
		// Get outbound nodes.
		for (CiBean bean : map.values()) {
			for (AttributeBean aBean : bean.getAttributes()) {
				if (!aBean.isComplexType()) {
					continue;
				}
				if (aBean.isDerived() && !bean.getAlias().equals(alias)) {
					continue;
				}
				String type = aBean.getType();
				CiBean targetBean = map.get(type);
				if (targetBean == null) {
					continue;
				}
				
				Node target = getNode(targetBean);
				Node source = getNode(bean);
				Edge e = g.addEdge(source, target);
				e.set("type", aBean.getRefType());
				
				if (isDervedFrom(targetBean, root)) {
					Edge e1 = g.addEdge(source, rootNode);
					e1.set("type", aBean.getRefType());
				}
			
				System.out.println(bean.getAlias() + "->[" + aBean.getRefType() + "]->" + type);
			}
		}
		return(rootNode);
	}
	
	private boolean isDervedFrom(CiBean parent, CiBean child) {
		if (parent == null) {
			return(false);
		}
		if (child == null) {
			return(false);
		}
		if (child.getDerivedFrom() == null) {
			return(false);
		}
		if (child.getDerivedFrom().equals(parent.getAlias())) {
			return(true);
		}
		Map<String, CiBean> map = tModel.getBeanMap();
		return(isDervedFrom(parent, map.get(child.getDerivedFrom())));
	}

	protected Node getNode(CiBean bean) {
		Node n = nodeMap.get(bean.getAlias());
		if (n != null) {
			return(n);
		}
		n = g.addNode();
		n.set("alias", bean.getAlias());
		n.set("type", bean.getDerivedFrom());
		String displayName = bean.getDisplayName();
		if (displayName.length() > 20) {
			displayName = displayName.substring(0, 20) + "...";
		} else if (displayName.length() == 0) {
			displayName = "[" + bean.getAlias() + "]";
		}
		n.set("name", displayName);
		String icon = "";
		if (getValue(bean, "icon") != null) {
			icon = "&icon=" + getValue(bean, "icon");
		}
		n.set("image", OneCMDBConnection.instance().getIconURL() + "?type=" + bean.getAlias() + icon);


		nodeMap.put(bean.getAlias(), n);
		
		return(n);
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

	public Node findNode(String alias) {
		Node n = nodeMap.get(alias);
		return(n);
	}
}

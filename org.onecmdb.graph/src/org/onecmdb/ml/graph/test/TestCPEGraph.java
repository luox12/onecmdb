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
package org.onecmdb.ml.graph.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.onecmdb.core.tests.AbstractOneCmdbTestCase;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.bean.ValueBean;
import org.onecmdb.core.utils.graph.query.GraphQuery;
import org.onecmdb.core.utils.graph.query.constraint.ItemSecurityConstraint;
import org.onecmdb.core.utils.graph.query.constraint.RelationConstraint;
import org.onecmdb.core.utils.graph.query.selector.ItemOffspringSelector;
import org.onecmdb.core.utils.graph.query.selector.ItemRelationSelector;
import org.onecmdb.core.utils.graph.result.Graph;
import org.onecmdb.core.utils.graph.result.Template;
import org.onecmdb.core.utils.wsdl.IOneCMDBWebService;
import org.onecmdb.core.utils.wsdl.OneCMDBWebServiceImpl;
import org.onecmdb.ml.graph.model.CIEdge;
import org.onecmdb.ml.graph.model.CINode;
import org.onecmdb.ml.graph.transform.OneCMDBGraphML;
import org.onecmdb.ml.graph.view.GraphView;

import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Table;
import prefuse.data.io.GraphMLReader;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.SearchTupleSet;
import prefuse.util.FontLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;

public class TestCPEGraph extends AbstractOneCmdbTestCase {
	private IOneCMDBWebService cmdbService;
	private String token;

	public void setURL(String url) {
		Service serviceModel = new ObjectServiceFactory().create(IOneCMDBWebService.class);
		try {
			cmdbService = (IOneCMDBWebService) new XFireProxyFactory().create(serviceModel, url);
		} catch (MalformedURLException e) {				
			e.printStackTrace();
			fail("Can't connect to remote WebService ");
		}
	}
	public void login(String user, String pwd) throws Exception {
		setToken(cmdbService.auth(user, pwd));
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	@Override
	protected String getDatasourceResource() {
		return("org/onecmdb/core/tests/graph/mysql-update-cygate-datasource.xml");
		//return(OneCMDBTestConfig.MYSQL_UPDATE_DATASOURCE);
		//return(OneCMDBTestConfig.SQLSERVER2005_CREATE_DROP_DATASOURCE);
		//return(OneCMDBTestConfig.HSQL_SERVER_CREATE_DROP_DATASOURCE);
	}

	public void setUp() {
		if (true) {
			Service serviceModel = new ObjectServiceFactory().create(IOneCMDBWebService.class);
			try {
				String remoteURL = "http://localhost:8080/webservice/onecmdb";
				cmdbService = (IOneCMDBWebService) new XFireProxyFactory().create(serviceModel, remoteURL);
			} catch (MalformedURLException e) {				
				e.printStackTrace();
				fail("Can't connect to remote WebService ");
			}
		} else {
			super.setUp();

			OneCMDBWebServiceImpl impl = new OneCMDBWebServiceImpl();
			impl.setOneCmdb(getCmdbContext());
			cmdbService = impl;
		}
		try {
			//token = cmdbService.auth("niklas", "G8sigåt1");
			//token = cmdbService.auth("support", "1Qaz1qaz");
			token = cmdbService.auth("admin", "123");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private OneCMDBGraphML getDirectConnections() {
		ItemOffspringSelector brukare = new ItemOffspringSelector("brukare", "Brukare");
		ItemOffspringSelector access = new ItemOffspringSelector("access", "WANAccess");
			
		ItemOffspringSelector connection = new ItemOffspringSelector("direkt", "DirektNetworkConnection");
		ItemOffspringSelector ifs = new ItemOffspringSelector("ifs", "NetworkIF");
		ItemOffspringSelector nodes = new ItemOffspringSelector("nodes", "NetworkNode");
		
		ItemRelationSelector c2i = new ItemRelationSelector("c2i", "ConnectedTo", ifs.getId(), connection.getId());
		ItemRelationSelector i2n = new ItemRelationSelector("i2n", "BelongsTo", nodes.getId(), ifs.getId());
		ItemRelationSelector a2c = new ItemRelationSelector("a2c", "BelongsTo", connection.getId(), access.getId());
		ItemRelationSelector a2b = new ItemRelationSelector("a2b", "BelongsTo", brukare.getId(), access.getId());
			
		nodes.setPrimary(true);
		/*
		ifs.setExcludedInResultSet(true);
		connection.setExcludedInResultSet(true);
		c2i.setExcludedInResultSet(true);
		i2n.setExcludedInResultSet(true);
		*/
		
		GraphQuery q = new GraphQuery();
		q.addSelector(connection);
		q.addSelector(ifs);
		q.addSelector(nodes);
		q.addSelector(brukare);
		q.addSelector(access);
		
		
		q.addSelector(c2i);
		q.addSelector(i2n);
		q.addSelector(a2c);
		q.addSelector(a2b);
			
		Graph result = cmdbService.queryGraph(token, q);
	System.out.println(result.toString());
		
		result.buildMap();
		
		Template n = result.fetchNode("nodes");
		
		OneCMDBGraphML ml = new OneCMDBGraphML();
		
		CINode p = new CINode("p", "images/pe-node.jpg", "P-NOD");
		ml.addNode(p);
		for (CiBean pe : n.getOffsprings()) {
			ValueBean v = pe.fetchAttributeValueBean("sysName", 0);
			String name = (v == null ? "Unknown" : v.getValue());
			CINode peNode = new CINode(pe.getAlias(), "images/pe-node.jpg", name);
			ml.addNode(peNode);
			CIEdge edge = ml.addEdge(peNode, p);
			edge.setProperty("springLength", "80");
			edge.setProperty("springCoefficient", "0.9E-5f");
			
			
			Template peIfs = result.fetchReference(pe, RelationConstraint.TARGET, "i2n");
			if (peIfs != null) {
				for (CiBean peIfBean : peIfs.getOffsprings()) {
					CINode ifNode = getNode(peIfBean, "images/pe-node.jpg", null, "IF");
					ml.addNode(ifNode);
					CIEdge pE = ml.addEdge(ifNode, peNode);
					pE.setProperty("springLength", "1");
					pE.setProperty("springCoefficient", "1E-5f");
					
					// Fetch brukare for this interface
					//  Brukare <-- WANAcccess --> Connection --> IFS
					Template brukarList = getBrukareForIF(peIfBean, result);
					for (CiBean bBean : brukarList.getOffsprings()) {
						CINode bNode = getNode(bBean, "images/pe-node.jpg", "name", "B");
						ml.addNode(bNode);
						CIEdge bE = ml.addEdge(bNode, ifNode);
						bE.setProperty("springCoefficient", "1E-5f");
						bE.setProperty("springLength", "80");
					}
				}
			}
		}
		return(ml);
	}
	
	private OneCMDBGraphML getInDirectConnections() {
		ItemOffspringSelector brukare = new ItemOffspringSelector("brukare", "Brukare");
		ItemOffspringSelector access = new ItemOffspringSelector("access", "WANAccess");
			
		ItemOffspringSelector connection = new ItemOffspringSelector("indirekt", "InDirektNetworkConnection");
		ItemOffspringSelector ifs = new ItemOffspringSelector("ifs", "NetworkIF");
		ItemOffspringSelector nodes = new ItemOffspringSelector("nodes", "NetworkNode");
		
		ItemRelationSelector c2i = new ItemRelationSelector("c2i", "ConnectedTo", ifs.getId(), connection.getId());
		ItemRelationSelector i2n = new ItemRelationSelector("i2n", "BelongsTo", nodes.getId(), ifs.getId());
		ItemRelationSelector a2c = new ItemRelationSelector("a2c", "BelongsTo", connection.getId(), access.getId());
		ItemRelationSelector a2b = new ItemRelationSelector("a2b", "BelongsTo", brukare.getId(), access.getId());
			
		connection.setPrimary(true);
		/*
		ifs.setExcludedInResultSet(true);
		connection.setExcludedInResultSet(true);
		c2i.setExcludedInResultSet(true);
		i2n.setExcludedInResultSet(true);
		*/
		
		GraphQuery q = new GraphQuery();
		q.addSelector(connection);
		q.addSelector(ifs);
		q.addSelector(nodes);
		q.addSelector(brukare);
		q.addSelector(access);
		
		
		q.addSelector(c2i);
		q.addSelector(i2n);
		q.addSelector(a2c);
		q.addSelector(a2b);
			
		Graph result = cmdbService.queryGraph(token, q);
		System.out.println(result.toString());
		
		result.buildMap();
		
		Template c = result.fetchNode(connection.getId());
		
		OneCMDBGraphML ml = new OneCMDBGraphML();
		
		CINode p = new CINode("p", "images/pe-node.jpg", "P-NOD");
		ml.addNode(p);
		
		for (CiBean con : c.getOffsprings()) {
			ValueBean lanIF = con.fetchAttributeValueBean("lanIF", 0);
			//ValueBean mgmtIF = con.fetchAttributeValueBean("mgmtIF", 0);
			ValueBean linkIF = con.fetchAttributeValueBean("linkIF", 0);
			ValueBean coreIF = con.fetchAttributeValueBean("coreNodeIF", 0);
				
			if (isNullValue(lanIF) ||
				//isNullValue(mgmtIF) ||
				isNullValue(linkIF) ||
				isNullValue(coreIF)) {
				continue;
			}
			
			CiBean lanIFBean = result.findOffspringAlias(lanIF.getValue());
			//CiBean mgmtIFBean = result.findOffspringAlias(mgmtIF.getValue());
			CiBean linkIFBean = result.findOffspringAlias(linkIF.getValue());
			CiBean coreIFBean = result.findOffspringAlias(coreIF.getValue());
				
			// Find CPE Node.
			ValueBean cpeValue = lanIFBean.fetchAttributeValueBean("system", 0);
			if (isNullValue(cpeValue)) {
				continue;
			}
			
			CiBean cpeBean = result.findOffspringAlias(cpeValue.getValue());
			
			// Find PE node.
			ValueBean peValue = coreIFBean.fetchAttributeValueBean("system", 0);
			if (isNullValue(peValue)) {
				continue;
			}
			
			CiBean peBean = result.findOffspringAlias(peValue.getValue());
			
			CINode peNode = getNode(peBean, null, "sysName", "PE_NODE_MISSING");
			CINode cpeNode = getNode(cpeBean, null, "sysName", "CPE_NODE_MISSING");
			CINode linkNode = getNode(linkIFBean, null, null, "LINK_MISSING");
			CINode coreIFNode = getNode(coreIFBean, null, null, "PE_LINK_IF");
			//CINode mgmtNode = getNode(linkIFBean, null, "");
			CINode lanNode = getNode(lanIFBean, null, null, "LAN_IF_MISSING");
			
			ml.addNode(peNode);
			ml.addNode(cpeNode);
			
			ml.addNode(linkNode);
			ml.addNode(lanNode);
			ml.addNode(coreIFNode);
			
			// Relation. 
			// PE->P
			CIEdge pe2p = ml.addEdge(peNode, p);
			pe2p.setProperty("springCoefficient", "1E-5f");
			pe2p.setProperty("springLength", "80");
			
			// LINKIF -> CPE
			
			CIEdge link2cpe = ml.addEdge(cpeNode, linkNode);
			link2cpe.setProperty("springCoefficient", "1E-4f");
			link2cpe.setProperty("springLength", "1");
			
			// LANIF -> CPE
			CIEdge lan2cpe = ml.addEdge(lanNode, cpeNode);
			lan2cpe.setProperty("springCoefficient", "1E-4f");
			lan2cpe.setProperty("springLength", "1");
			
			// COREIF -> PE
			CIEdge core2pe = ml.addEdge(coreIFNode, peNode);
			core2pe.setProperty("springCoefficient", "1E-4f");
			core2pe.setProperty("springLength", "1");
			
			// LINKIF->COREIF
			CIEdge link2coreIF = ml.addEdge(linkNode, coreIFNode);
			core2pe.setProperty("springCoefficient", "1E-5f");
			core2pe.setProperty("springLength", "80");
			
			
			// Brukare --> LANIF
			Template brukarList = getBrukareForIF(lanIFBean, result);
			for (CiBean bBean : brukarList.getOffsprings()) {
				CINode bNode = getNode(bBean, "images/pe-node.jpg", "name", "BRUKARE_MISSING");
				ml.addNode(bNode);
				CIEdge bE = ml.addEdge(bNode, lanNode);
				bE.setProperty("springCoefficient", "1E-5f");
				bE.setProperty("springLength", "80");
			}
		}
		return(ml);
	}

	private OneCMDBGraphML getInDirectNoIFConnections(boolean valid) {
		ItemOffspringSelector brukare = new ItemOffspringSelector("brukare", "Brukare");
		ItemOffspringSelector access = new ItemOffspringSelector("access", "WANAccess");
			
		ItemOffspringSelector connection = new ItemOffspringSelector("indirekt", "InDirektNetworkConnection");
		ItemOffspringSelector ifs = new ItemOffspringSelector("ifs", "NetworkIF");
		ItemOffspringSelector nodes = new ItemOffspringSelector("nodes", "NetworkNode");
		
		ItemRelationSelector c2i = new ItemRelationSelector("c2i", "ConnectedTo", ifs.getId(), connection.getId());
		ItemRelationSelector i2n = new ItemRelationSelector("i2n", "BelongsTo", nodes.getId(), ifs.getId());
		ItemRelationSelector a2c = new ItemRelationSelector("a2c", "BelongsTo", connection.getId(), access.getId());
		ItemRelationSelector a2b = new ItemRelationSelector("a2b", "BelongsTo", brukare.getId(), access.getId());
			
		connection.setPrimary(true);
		//connection.addExcludeRelation("c2i");
		//connection.addExcludeRelation("a2c");
		
		/*
		ifs.setExcludedInResultSet(true);
		connection.setExcludedInResultSet(true);
		c2i.setExcludedInResultSet(true);
		i2n.setExcludedInResultSet(true);
		*/
		
		ItemSecurityConstraint security = new ItemSecurityConstraint();
		security.setGroupName("SLLNetSecurityGroup");
		
		connection.applyConstraint(security);
		ifs.applyConstraint(security);
		nodes.applyConstraint(security);
		brukare.applyConstraint(security);
		access.applyConstraint(security);
			
		
		GraphQuery q = new GraphQuery();
		q.addSelector(connection);
		q.addSelector(ifs);
		q.addSelector(nodes);
		q.addSelector(brukare);
		q.addSelector(access);
		
		q.addSelector(c2i);
		
		q.addSelector(i2n);
		q.addSelector(a2c);
		q.addSelector(a2b);
			
		Graph result = cmdbService.queryGraph(token, q);
		System.out.println(result.toString());
		
		result.buildMap();
		
		Template c = result.fetchNode(connection.getId());
		
		OneCMDBGraphML ml = new OneCMDBGraphML();
		
		CINode p = new CINode("p", "images/pe-node.jpg", "P-NOD");
		ml.addNode(p);
		
		for (CiBean con : c.getOffsprings()) {
			ValueBean lanIF = con.fetchAttributeValueBean("lanIF", 0);
			//ValueBean mgmtIF = con.fetchAttributeValueBean("mgmtIF", 0);
			ValueBean linkIF = con.fetchAttributeValueBean("linkIF", 0);
			ValueBean coreIF = con.fetchAttributeValueBean("nodeIF", 0);
				
			CiBean lanIFBean = null;
			CiBean linkIFBean = null; 
			CiBean coreIFBean = null;
			
			if (lanIF != null) {
				lanIFBean = result.findOffspringAlias(lanIF.getValue());
			}
			//CiBean mgmtIFBean = result.findOffspringAlias(mgmtIF.getValue());
			if (linkIF != null) {
				linkIFBean = result.findOffspringAlias(linkIF.getValue());
			}
			if (coreIF != null) {
				coreIFBean = result.findOffspringAlias(coreIF.getValue());
			}
				
			// Find CPE Node.
			CiBean cpeBean = null;
			if (lanIFBean != null) {
				ValueBean cpeValue = lanIFBean.fetchAttributeValueBean("system", 0);
			
				 cpeBean = result.findOffspringAlias(cpeValue.getValue());
			}
			CiBean peBean = null;
			if (coreIFBean != null) {
				// Find PE node.
				ValueBean peValue = coreIFBean.fetchAttributeValueBean("system", 0);
				peBean = result.findOffspringAlias(peValue.getValue());
			}
			
			CINode peNode = getNode(peBean, null, "sysName", "NO_PE_NODE");
			CINode cpeNode = getNode(cpeBean, null, "sysName", "NO_CPE_NODE");
			//CINode linkNode = getNode(linkIFBean, null, null);
			//CINode coreIFNode = getNode(coreIFBean, null, null);
			//CINode mgmtNode = getNode(linkIFBean, null, "");
			//CINode lanNode = getNode(lanIFBean, null, null, "NO_LAN_NODE");
			
			// Brukare --> CPE
			Template brukarList = getBrukareForIF(lanIFBean, result);
			
			// Check if valid....
			boolean validModel = false;
			if (cpeBean != null &&
				peBean != null &&
				lanIFBean != null &&
				brukarList.getOffsprings().size() > 0) {
				validModel = true;
			}
		
			if (valid != validModel) {
				continue;
			}
			
			ml.addNode(peNode);
			ml.addNode(cpeNode);
			
			/*
			ml.addNode(linkNode);
			ml.addNode(lanNode);
			ml.addNode(coreIFNode);
			*/
			
			// Relation. 
			// PE->P
			CIEdge pe2p = ml.addEdge(peNode, p);
			pe2p.setProperty("springCoefficient", "1E-6f");
			pe2p.setProperty("springLength", "90");
			
			// CPE -> PE
			CIEdge cpe2pe = ml.addEdge(cpeNode, peNode);
			cpe2pe.setProperty("springCoefficient", "1E-5f");
			cpe2pe.setProperty("springLength", "50");
			
			
			
			if (brukarList.getOffsprings().size() == 0) {
				CINode bNode = getNode(null, "images/pe-node.jpg", "name", "NO_BRUKARE" + con.getAlias());
				ml.addNode(bNode);
				CIEdge bE = ml.addEdge(bNode, cpeNode);
				bE.setProperty("springCoefficient", "1E-5f");
				bE.setProperty("springLength", "80");
			} else {
				for (CiBean bBean : brukarList.getOffsprings()) {
					CINode bNode = getNode(bBean, "images/pe-node.jpg", "name", "NO_BRUKARE");
					ml.addNode(bNode);
					CIEdge bE = ml.addEdge(bNode, cpeNode);
					bE.setProperty("springCoefficient", "1E-5f");
					bE.setProperty("springLength", "80");
				}
			}
		}
		return(ml);
	}

	
	private boolean isNullValue(ValueBean v) {
		if (v == null) {
			return(true);
		}
		if (v.getValue() == null) {
			return(true);
		}
		return(false);
	}
	
	
	public JPanel getPanel() {
		//OneCMDBGraphML ml = getDirectConnections();
		OneCMDBGraphML ml = getInDirectNoIFConnections(true);
			
		ml.toGraphML(new PrintWriter(System.out), "p");
		prefuse.data.Graph mlGraph = null;
    	try {
            mlGraph = (prefuse.data.Graph)new GraphMLReader().readGraph(ml.getInputStream("p"));
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(1);
        }
		JFrame frame = new JFrame();
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		final String label = "name";
		final GraphView gView = new GraphView(mlGraph, label);
	     final JFastLabel title = new JFastLabel("                 ");
	        title.setPreferredSize(new Dimension(350, 20));
	        title.setVerticalAlignment(SwingConstants.BOTTOM);
	        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
	        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
	        
	        gView.getVisualization().getDisplay(0).addControlListener(new ControlAdapter() {
	            public void itemEntered(VisualItem item, MouseEvent e) {
	                if ( item.canGetString(label) )
	                    title.setText(item.getString(label));
	            }
	            public void itemExited(VisualItem item, MouseEvent e) {
	                title.setText(null);
	            }
	        });
	 
		
		
		center.add(gView, BorderLayout.CENTER);
		
		final JCheckBox mag = new JCheckBox("ZoomMag");
		mag.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean enabled = mag.getModel().isSelected();
				System.out.println("Mag Clicked! " + enabled);
				gView.magnify(enabled);
				
			}
			
		});
	
		final JCheckBox fisheye = new JCheckBox("FishEye");
		fisheye.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean enabled = fisheye.getModel().isSelected();
				System.out.println("Mag Clicked! " + enabled);
				gView.fisheye(enabled);
				
			}
			
		});
		
		  // create a search panel for the tree map
        SearchQueryBinding sq = new SearchQueryBinding(
             (Table)gView.getVisualization().getGroup("graph.nodes"), label,
             (SearchTupleSet)gView.getVisualization().getGroup(Visualization.SEARCH_ITEMS));
        JSearchPanel search = sq.createSearchPanel();
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5,5,4,0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
		
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalGlue());
        box.add(search);
        box.add(Box.createHorizontalStrut(3));
        
		JPanel panel = new JPanel();
		panel.add(mag);
		panel.add(fisheye);
		panel.add(box);
		center.add(panel, BorderLayout.SOUTH);

		return(center);
	}
	
	public void testDirektConnection() {
		
		//OneCMDBGraphML ml = getDirectConnections();
		OneCMDBGraphML ml = getInDirectNoIFConnections(true);
			
		ml.toGraphML(new PrintWriter(System.out), "p");
		prefuse.data.Graph mlGraph = null;
    	try {
            mlGraph = (prefuse.data.Graph)new GraphMLReader().readGraph(ml.getInputStream("p"));
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(1);
        }
		JFrame frame = new JFrame();
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		final String label = "name";
		final GraphView gView = new GraphView(mlGraph, label);
	     final JFastLabel title = new JFastLabel("                 ");
	        title.setPreferredSize(new Dimension(350, 20));
	        title.setVerticalAlignment(SwingConstants.BOTTOM);
	        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
	        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
	        
	        gView.getVisualization().getDisplay(0).addControlListener(new ControlAdapter() {
	            public void itemEntered(VisualItem item, MouseEvent e) {
	                if ( item.canGetString(label) )
	                    title.setText(item.getString(label));
	            }
	            public void itemExited(VisualItem item, MouseEvent e) {
	                title.setText(null);
	            }
	        });
	 
		
		
		center.add(gView, BorderLayout.CENTER);
		
		final JCheckBox mag = new JCheckBox("ZoomMag");
		mag.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean enabled = mag.getModel().isSelected();
				System.out.println("Mag Clicked! " + enabled);
				gView.magnify(enabled);
				
			}
			
		});
	
		final JCheckBox fisheye = new JCheckBox("FishEye");
		fisheye.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean enabled = fisheye.getModel().isSelected();
				System.out.println("Mag Clicked! " + enabled);
				gView.fisheye(enabled);
				
			}
			
		});
		
		  // create a search panel for the tree map
        SearchQueryBinding sq = new SearchQueryBinding(
             (Table)gView.getVisualization().getGroup("graph.nodes"), label,
             (SearchTupleSet)gView.getVisualization().getGroup(Visualization.SEARCH_ITEMS));
        JSearchPanel search = sq.createSearchPanel();
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5,5,4,0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));
		
        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalGlue());
        box.add(search);
        box.add(Box.createHorizontalStrut(3));
        
		JPanel panel = new JPanel();
		panel.add(mag);
		panel.add(fisheye);
		panel.add(box);
		center.add(panel, BorderLayout.SOUTH);
	
		frame.getContentPane().add(center);
		frame.setSize(900, 700);
		frame.setVisible(true);
		synchronized(this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private CINode getNode(CiBean bean, String image, String alias, String defaultNode) {
		if (bean == null) {
			CINode node = new CINode(defaultNode, image, defaultNode);
			return(node);
		}
		
		
		String value = "";
		if (alias != null) {
			ValueBean v = bean.fetchAttributeValueBean(alias, 0);
			value = (v == null ? "Unknown" : v.getValue());
			value = value.replace('&', 'o');
		}
		
		CINode node = new CINode(bean.getAlias(), image, value);
		return(node);
	}
	
	private Template getBrukareForIF(CiBean ifBean, Graph result) {
		//  Brukare <-- WANAcccess --> Connection --> IFS --> Node
		Template t = new Template();
		Template c = result.fetchReference(ifBean, RelationConstraint.TARGET, "c2i");
		for (CiBean cBean : c.getOffsprings()) {
			Template a = result.fetchReference(cBean, RelationConstraint.TARGET, "a2c");
			for (CiBean aBean : a.getOffsprings()) {
				Template b = result.fetchReference(aBean, RelationConstraint.SOURCE, "a2b");
				for (CiBean bBean : b.getOffsprings()) {
					t.addOffspring(bBean);
				}
			}
		}
		return(t);
	}
	
	private Template fetchRelation(Graph result, CiBean start, String startId, String endId) {
		//result.get
		
		return(null);
	}

		

}

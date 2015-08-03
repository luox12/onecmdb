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
import java.util.Iterator;

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
import org.onecmdb.core.utils.bean.AttributeBean;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.bean.ValueBean;
import org.onecmdb.core.utils.graph.query.GraphQuery;
import org.onecmdb.core.utils.graph.query.selector.ItemOffspringSelector;
import org.onecmdb.core.utils.graph.query.selector.ItemRelationSelector;
import org.onecmdb.core.utils.graph.result.Graph;
import org.onecmdb.core.utils.graph.result.Template;
import org.onecmdb.core.utils.wsdl.IOneCMDBWebService;
import org.onecmdb.core.utils.wsdl.OneCMDBWebServiceImpl;
import org.onecmdb.ml.graph.expression.EdgeTypeGroupPredicate;
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
import prefuse.util.PrefuseLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.visual.VisualItem;

public class TestTemplateGraph extends AbstractOneCmdbTestCase {
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
			if (false) {
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
		
		private OneCMDBGraphML getTemplateGraph() {
			
			ItemOffspringSelector ci = new ItemOffspringSelector("ci", "Ci");
			ci.setMatchTemplate(true);
			ci.setPrimary(true);
			
			
			GraphQuery q = new GraphQuery();
			q.addSelector(ci);
			
			Graph result = cmdbService.queryGraph(token, q);
			System.out.println(result.toString());
			
			result.buildMap();
			
			Template c = result.fetchNode(ci.getId());
			
			OneCMDBGraphML ml = new OneCMDBGraphML();
			
			for (CiBean ciBean : c.getOffsprings()) {
				CINode parent = new CINode(ciBean.getDerivedFrom(), ciBean.getDerivedFrom());
				CINode child = new CINode(ciBean.getAlias(), ciBean.getAlias());
				child = ml.addNode(child);
				parent = ml.addNode(parent);
				
				
				// Inheritance Relation. 
				
				CIEdge inheritance = ml.addEdge(parent, child);
				inheritance.setProperty("springCoefficient", "1E-5f");
				inheritance.setProperty("springLength", "70");
				inheritance.setProperty("edgeType", "Inheritance");
				
				
				// References...
				
				for (AttributeBean aBean : ciBean.getAttributes()) {
					/*
					if (aBean.isDerived()) {
						continue;
					}
					*/
					if (aBean.isComplexType()) {
						String type = aBean.getType();
						CINode to = new CINode(type, type);
						to = ml.addNode(to);

						CIEdge relation = ml.addEdge(child, to);
						relation.setProperty("springCoefficient", "1E-6f");
						relation.setProperty("springLength", "100");
						relation.setProperty("edgeType", "Relation");		
					}
				}
				
			}
			return(ml);

		}
		
		public void testTemplateGraph() {

			//OneCMDBGraphML ml = getDirectConnections();
			OneCMDBGraphML ml = getTemplateGraph();

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
			
			final JCheckBox inheritance = new JCheckBox("Inheritance");
			inheritance.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					boolean enabled = inheritance.getModel().isSelected();
					Iterator iter = gView.getVisualization().items(new EdgeTypeGroupPredicate("graph.edges", "Inheritance"));
					while(iter.hasNext()) {
						VisualItem item = (VisualItem) iter.next();
						 PrefuseLib.updateVisible(item, enabled);
					}
					System.out.println("Inheriance Clicked! " + enabled);
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
			panel.add(inheritance);
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

}

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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.dom4j.DocumentException;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.ml.graph.main.CEvents;
import org.onecmdb.ml.graph.main.Event;
import org.onecmdb.ml.graph.main.EventDispatcher;
import org.onecmdb.ml.graph.main.IEventListener;
import org.onecmdb.ml.graph.main.OneCMDBConnection;
import org.onecmdb.ml.graph.main.model.CIAttributeModel;
import org.onecmdb.ml.graph.main.model.CIModel;
import org.onecmdb.ml.graph.main.view.GraphView;
import org.onecmdb.ml.graph.utils.StringConvert;
import org.onecmdb.swing.treetable.JTreeTable;

import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;

public class MainGraphQueryView extends JPanel implements IEventListener {
	
	private CIAttributeModel propertyControl;
	private JTreeTable pTree;
	private GraphViewControl ctrl;
	private GraphView gView;
	private String root;
	private String graphDef;
	private JTextArea errorArea = new JTextArea();
	
	public MainGraphQueryView(String contentRoot, String graphDef) {
		setRoot(contentRoot);
		setGraphDef(graphDef);
		initUI();
		EventDispatcher.addEventListener(this);
    }
	
	public void setGraphDef(String graphDef) {
		this.graphDef = graphDef;
	}

	protected void initUI() {
		setSize(500, 600);
		propertyControl = new CIAttributeModel();
		propertyControl.setAdvanced(false);
		
		pTree = new JTreeTable(propertyControl);
		pTree.setDefaultEditor(CiBean.class, propertyControl.getTableCellEditor());
		
		ctrl = new GraphViewControl();
		gView = new GraphView(ctrl.getGraph(), "name", null);
		
		gView.getVisualization().getGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(
	            new TupleSetListener() {
	                public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
	                	if (add.length == 1) {
	                		// Fire selection change...
	                		Node n = (Node)add[0];
	                		EventDispatcher.fireEvent(this, new Event(CEvents.ITEM_SELECTED, n.getString("alias")));
	                	}
	                }
	            }
	        );
		
		try { 
		
		JTabbedPane tp = new JTabbedPane();
		tp.add(new JScrollPane(pTree), "Attribute(s)");
		tp.add(new GraphQueryInputPanel(), "Query");
		tp.add(new JScrollPane(errorArea), "Status");
		final JSplitPane centerSplit = new JSplitPane();
		centerSplit.setTopComponent(gView);
		centerSplit.setBottomComponent(tp);
		centerSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		centerSplit.setOneTouchExpandable(true);
		setLayout(new BorderLayout());
		add(getToolBar(), BorderLayout.NORTH);
		add(centerSplit, BorderLayout.CENTER);
		
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Dimension newDimension = e.getComponent().getSize();
				System.out.println("New Dimension:" + newDimension);
				centerSplit.setDividerLocation(0.7D);
			}
		});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private JToolBar getToolBar() throws MalformedURLException, DocumentException {
		JToolBar bar = new JToolBar();
		
		bar.add(new JLabel("Select Graph"));
		
		final XMLComboBoxModel model = getGraphComboModel();
		final JComboBox box = new JComboBox(model);
		box.setMaximumSize(box.getPreferredSize());
		bar.add(box);
		bar.addSeparator();
		JButton reload = new JButton("Load");
		reload.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int index = box.getSelectedIndex();
				final String path = model.getXML(index, "path");
				EventDispatcher.fireEvent(this, new Event(CEvents.STATUS_MSG, "Load Graph Path: " + path));
				SwingUtilities.invokeLater(new Runnable() {

					public void run() {
						try {
							loadGraph(path);
						} catch (Exception e) {
							// TODO: Show error somewhere.
						}
					}

					
				});
			}
			
		});
		bar.add(reload);
		
	    final JLabel label = new JLabel("5");
		
		final JSlider slider = new JSlider(0, 30, 5);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gView.setDistance(slider.getValue());
				label.setText("" + slider.getValue());
			}
		});
		slider.setPreferredSize(new Dimension(150,30));
		slider.setMaximumSize(new Dimension(150,30));
		slider.setEnabled(false);
		label.setPreferredSize(new Dimension(48, 30));
		
		JPanel component = new JPanel();
		component.setLayout(new BoxLayout(component, BoxLayout.X_AXIS));
	    component.add(label);
	    component.add(slider);
		final JCheckBox enableDistance = new JCheckBox("Enable Distance");
        enableDistance.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				gView.setEnableDistanceFilter(enableDistance.isSelected());
				slider.setEnabled(enableDistance.isSelected());
			}
        });
		
        bar.add(enableDistance);
        bar.add(component);
		return(bar);
		
	}
	public void setRoot(String root) {
		this.root = root;
	}
	private String getRoot() {
		return(this.root);
		//return("file:///d:/appl/cygatecmdb-1.4.1/cygate/export/");
	}
	private XMLComboBoxModel getGraphComboModel() throws MalformedURLException, DocumentException {
		URL url = new URL(getRoot() + "/" + this.graphDef);
		return(new XMLComboBoxModel(url, "graph", "name"));
	}
	
	
	
	
	public static void main(String argv[]) {
		JFrame frame = new JFrame();
		frame.getContentPane().add(new MainGraphQueryView("http://localhost:8888/org.onecmdb.ui.gwt.framework.CMDBApplication/onecmdb/content/", "ss"));
		frame.setSize(500, 600);
		frame.setVisible(true);
	}

	private void loadGraph(String path) throws Exception {
		URL url = new URL(getRoot() + path);
		String query = parseISToString(url.openStream());
		EventDispatcher.fireEvent(this, new Event(CEvents.REDRAW_GRAPH, query));
			
		
	}
	public String parseISToString(java.io.InputStream is){
		LineNumberReader lin = new LineNumberReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		try{
			String line = null;
			while((line=lin.readLine()) != null){
				sb.append(line+"\n");
			}
		}catch(Exception ex){
			ex.getMessage();
		}finally{
			try{
				is.close();
			}catch(Exception ex){}
		}
		return sb.toString();
	}
	
	public void onEvent(Event e) {
		switch(e.getType()) {
			case(CEvents.ITEM_SELECTED):
				String alias = (String) e.getData();
				CiBean bean = OneCMDBConnection.instance().getBeanFromAlias(alias);
				if (bean != null) {
					CiBean template = bean;
					if (!bean.isTemplate()) {
						template = OneCMDBConnection.instance().getBeanFromAlias(bean.getDerivedFrom());
					}
					propertyControl.setRoot(new CIModel(template, bean));
					TableModel tModel = pTree.getModel();
					if (tModel instanceof AbstractTableModel) {
						((AbstractTableModel)tModel).fireTableDataChanged();
					}
				}
				break;
			case(CEvents.REDRAW_GRAPH):
				try {
					EventDispatcher.fireEvent(this, new Event(CEvents.STATUS_MSG, "Load Graph"));
					
					ctrl.reloadGraph((String)e.getData());
				    gView.redrawAndZoomToFit();    
				} catch (DocumentException e1) {
					e1.printStackTrace();
					EventDispatcher.fireEvent(this, new Event(CEvents.STATUS_MSG, "ERROR: Load Graph" + e.toString()));
						
				}
				break;
			case CEvents.STATUS_MSG:
				errorArea.append(e.getData().toString() +"\n");
				break;
				
			
		}
		
	}
	
	
}

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
package org.onecmdb.ml.graph.main.transform;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.onecmdb.utils.wsdl.OneCMDBQuery2XML;

public class MainQueryTransform extends JPanel {
	
	
	private File currentQuery;
	private File currentXSLT;
	
	
	public MainQueryTransform() {
		initUI();
	}
	
	protected void initUI() {
		setLayout(new BorderLayout());
		final JTextArea query = new JTextArea();
		final JTextArea queryResult = new JTextArea();
		final JTextArea transform = new JTextArea();
		final JTextArea transformResult = new JTextArea();
		
		JTabbedPane inputTab = new JTabbedPane();
	
		inputTab.addTab("Query", new JScrollPane(query));
		inputTab.addTab("Transform", new JScrollPane(transform));
		
		JTabbedPane resultTab = new JTabbedPane();
		resultTab.addTab("Result Query", new JScrollPane(queryResult));
		resultTab.addTab("Result Transform", new JScrollPane(transformResult));
		
		
		JSplitPane split = new JSplitPane();
		split.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		split.setTopComponent(inputTab);
		split.setBottomComponent(resultTab);
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenuItem openQuery = new JMenuItem("Open Query");
		openQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				File file = openFile();
				if (file == null) {
					return;
				}
				currentQuery = file;
			    String text = loadFile(file);
			    query.setText(text);
			}
		});
		JMenuItem openTransform = new JMenuItem("Open XSLT");
		openTransform.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				File file = openFile();
				if (file == null) {
					return;
				}
				currentXSLT = file;
				String text = loadFile(file);
				transform.setText(text);
			}
		});
		
		JMenuItem saveQuery = new JMenuItem("Save Query");
		saveQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				storeFile(currentQuery, query.getText());
			}
			
		});
		JMenuItem saveTransform = new JMenuItem("Save XSLT");
		saveTransform.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				storeFile(currentXSLT, transform.getText());
			}
			
		});

		file.add(openQuery);
		file.add(openTransform);
		file.add(saveQuery);
		file.add(saveTransform);
		
		bar.add(file);
		JButton doQuery = new JButton("Query");
		doQuery.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doQuery(query.getText(), queryResult);
			}
			
		});
		bar.add(doQuery);
		JButton doTransform = new JButton("Transform");
		doTransform.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				doTransform(query.getText(), transform.getText(), queryResult, transformResult);
			}
			
		});
		
		bar.add(doTransform);
		
		add(bar, BorderLayout.NORTH);
		add(split, BorderLayout.CENTER);
	}
	
	private File lastPath = null;
	
	protected File openFile() {
		JFileChooser fileChooser = new JFileChooser();
		if (lastPath != null) {
			fileChooser.setCurrentDirectory(lastPath);
		}
		int result = fileChooser.showOpenDialog(MainQueryTransform.this);
	     if (result != JFileChooser.APPROVE_OPTION) {
	    	 return(null);
	     }
	     String file = fileChooser.getSelectedFile().getName();
	     String path = fileChooser.getCurrentDirectory().toString();
	     lastPath = new File(path);
	     File f = new File(path, file);
	     return(f);
	}
	
	protected File selectSaveFile(File currentFile) {
		JFileChooser fileChooser = new JFileChooser();
		if (currentFile != null) {
			//fileChooser.setCurrentDirectory(dir);
			fileChooser.setSelectedFile(currentFile);
		} else if (lastPath != null) {
			fileChooser.setCurrentDirectory(lastPath);
		}
		
		int result = fileChooser.showSaveDialog(MainQueryTransform.this);
	     if (result != JFileChooser.APPROVE_OPTION) {
	    	 return(null);
	     }
	     String file = fileChooser.getSelectedFile().getName();
	     String path = fileChooser.getCurrentDirectory().toString();
	     lastPath = new File(path);
	     File f = new File(path, file);
	     return(f);
	}
	

	protected void doTransform(final String query, final String xslt,
			JTextArea queryResult, final JTextArea transformResult) {
		new ProgressTask(new Runnable() {

			public void run() {

				OneCMDBQuery2XML xml = getOneCMDBQueryCmd();
				StringReader queryReader = new StringReader(query);
				StringReader xsltReader = new StringReader(xslt);
				StringWriter stringWr = new StringWriter();
				PrintWriter writer = new PrintWriter(stringWr);
				try {
					xml.process(queryReader, xsltReader, writer);
					writer.flush();
					transformResult.setText(stringWr.getBuffer().toString());
				} catch (Throwable e) {
					transformResult.setText(e.toString());
				}
			}
		}).start();
		
	}
	
	protected OneCMDBQuery2XML getOneCMDBQueryCmd() {
		OneCMDBQuery2XML xml = new OneCMDBQuery2XML();
		xml.setUsername("admin");
		xml.setPwd("123");
		xml.setServiceURL("http://localhost:8080/webservice/onecmdb");
		
		return(xml);
	}
	
	protected void doQuery(final String text, final JTextArea queryResult) {
		new ProgressTask(new Runnable() {

			public void run() {
				OneCMDBQuery2XML xml = getOneCMDBQueryCmd();
				StringReader queryReader = new StringReader(text);
				StringWriter stringWr = new StringWriter();
				PrintWriter writer = new PrintWriter(stringWr);
				try {
					xml.process(queryReader, null, writer);
					writer.flush();
					queryResult.setText(stringWr.getBuffer().toString());
				} catch (Throwable e) {
					queryResult.setText(e.toString());
				}
			}
			
		}).start();	
	}

	private String loadFile(File f) {
		try {
			FileInputStream fis = new FileInputStream(f);
			int x= fis.available();
			byte b[]= new byte[x];
			fis.read(b);
			String content = new String(b);
			return(content);
		} catch (Throwable t) {
			return(t.toString());
		}
	}
	
	private void storeFile(File oldFile, String text) {
		File f = selectSaveFile(oldFile);
		if (f == null) {
			return;
		}
		try {
			Writer out = new OutputStreamWriter(new FileOutputStream(f));
			out.write(text.toCharArray());
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public static void main(String argv[]) {
		/*
        try {
            String laf = UIManager.getSystemLookAndFeelClassName();             
            UIManager.setLookAndFeel(laf);  
        } catch ( Exception e ) {}
        */
        JFrame frame = new JFrame("OneCMDB Query/Transform");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainQueryTransform(), BorderLayout.CENTER);
        frame.setSize(600, 400);
        frame.pack();
        frame.setVisible(true);
	}
	
	
	
	
	
}

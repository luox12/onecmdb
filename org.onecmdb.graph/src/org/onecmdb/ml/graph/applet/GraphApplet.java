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
package org.onecmdb.ml.graph.applet;

import java.applet.AppletContext;
import java.awt.BorderLayout;

import javax.swing.JApplet;
import javax.swing.JPanel;

import org.onecmdb.ml.graph.test.TestCPEGraph;



public class GraphApplet extends JApplet {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

	@Override
	public AppletContext getAppletContext() {
		// TODO Auto-generated method stub
		return super.getAppletContext();
	}

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return super.getParameter(name);
	}

	@Override
	public String[][] getParameterInfo() {
		return(new String[][] {
				{"token","string","An authenticated token to OneCMDB"},
				{"url","string","The URL to oneCMDB WSDL"}
		});
	}

	@Override
	public void init() {
		super.init();
		//Logger.shutdown();
		/*
		Appender onecmdbLogAppender = Logger.getRootLogger().getAppender("onecmdb.log");
		Logger.getRootLogger().removeAppender(onecmdbLogAppender);
		Appender onecmdbErrAppender = Logger.getRootLogger().getAppender("onecmdb.err");
		Logger.getRootLogger().removeAppender(onecmdbErrAppender);
		*/
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		/*
		panel.add(new JLabel("Starting....."));
		panel.add(new JLabel("TOKEN=" + getParameter("token")));
		panel.add(new JLabel("URL=" + getParameter("url")));
		*/
		String url = null;
		if (url == null) {
			url = "http://localhost:8080/webservice/onecmdb";
		}
		TestCPEGraph t = new TestCPEGraph();
		t.setURL(url);
		try {
			t.login("admin", "123");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		panel.add(t.getPanel(), BorderLayout.CENTER);
		getContentPane().add(panel);
	}
		

}

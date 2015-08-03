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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.onecmdb.core.utils.bean.CiBean;

public class XMLComboBoxModel extends AbstractListModel implements ComboBoxModel {
	  private Object selectedItem;
	private List elements;
	private String value;
	 
	public static void main(String argv[]) {
		try {
			XMLComboBoxModel model = new XMLComboBoxModel(new URL(argv[0]), argv[1], argv[2]);
			System.out.println("Size=" + model.getSize());
			for (int i = 0; i < model.getSize(); i++) {
				System.out.println(i + ":" + model.getElementAt(i));
			}
		}catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	  public XMLComboBoxModel(URL url, String path, String value) throws DocumentException {
		 SAXReader reader = new SAXReader();
		 Document document = reader.read(url);
		 List<CiBean> beans = new ArrayList<CiBean>();
		 Element root = document.getRootElement();
		 
		 elements = root.elements(path);
		 
		 this.value = value;
	  }

	  public Object getSelectedItem() {
	    return selectedItem;
	  }

	  public void setSelectedItem(Object newValue) {
	    selectedItem = newValue;
	  }

	  public int getSize() {
	    return elements.size();
	  }

	  public Object getElementAt(int i) {
	    Element el = (Element) elements.get(i);
	    return(el.element(value).getText());
	  }

	public String getXML(int index, String path) {
	    Element el = (Element) elements.get(index);
	    return(el.element(path).getText());
	}
}

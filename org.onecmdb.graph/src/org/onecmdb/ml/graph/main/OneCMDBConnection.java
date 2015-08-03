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

import java.net.MalformedURLException;

import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.onecmdb.core.internal.model.QueryCriteria;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.core.utils.wsdl.IOneCMDBWebService;

public class OneCMDBConnection {

	private static OneCMDBConnection instance;
	private IOneCMDBWebService cmdbService;
	private String token;
	private String url = "http://localhost:8080/webservice/onecmdb";
	private String username = "admin";
	private String password = "123";
	private String iconURL;
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public IOneCMDBWebService getCmdbService() {
		return cmdbService;
	}

	public void setup() throws Exception {
		if (cmdbService == null) {
			Service serviceModel = new ObjectServiceFactory().create(IOneCMDBWebService.class);
			if (url == null) {
				throw new IllegalArgumentException("No OneCMDB url specified!");
			}
			cmdbService = (IOneCMDBWebService) new XFireProxyFactory().create(serviceModel, url);
		}
		if (token == null) {
			token = cmdbService.auth(username, password);
		}
	}
	
	public static void setInstance(OneCMDBConnection connection) {
		instance = connection;
	}
	
	public static OneCMDBConnection instance() {
		if (instance == null) {
			throw new IllegalArgumentException("No COneCMDb Connection initiated!");
		}
		return(instance);
	}

	public CiBean getBeanFromAlias(String alias) {
		QueryCriteria crit = new QueryCriteria();
		crit.setCiAlias(alias);
		
		CiBean[] result = cmdbService.search(token, crit);
		if (result.length == 1) {
			return(result[0]);
		}
		return(null);
	}

	public void setIconURL(String url) {
		this.iconURL = url;
	}
	public String getIconURL() {
		if (this.iconURL == null) {
			return("http://localhost:8080/onecmdb-desktop/onecmdb/icon");
		}
		return(this.iconURL);
	}

}

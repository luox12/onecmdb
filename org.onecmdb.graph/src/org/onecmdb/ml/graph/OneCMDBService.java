/*
 * OneCMDB, an open source configuration management project.
 * Copyright 2007, Lokomo Systems AB, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.onecmdb.ml.graph;

import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.onecmdb.core.utils.wsdl.IOneCMDBWebService;

public class OneCMDBService {
	
	private static OneCMDBService _instance;

	public static OneCMDBService getInstance() {
		return(_instance);
	}
	
	public static IOneCMDBWebService getService() {
		return(getInstance().getCMDBService());
	}
	public static String getToken() {
		return(getInstance().getAuthToken());
	}
	
	
	public static void allocInstance(String url) {
		_instance = new OneCMDBService();
		_instance.setURL(url);
	}
	

	protected OneCMDBService() {
	}

	private IOneCMDBWebService cmdbService;
	private String user;
	private String pwd;
	private String token;
	private String url;

	public void setURL(String url) {
		this.url = url;
	}

	
	public void setUserPwd(String user, String pwd) {
		this.user = user;
		this.pwd = pwd;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getAuthToken() {
		if (token == null) {
			try {
				token = cmdbService.auth(user, pwd);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return(token);
		
	}
	
	public IOneCMDBWebService getCMDBService() {
		if (cmdbService == null) {
			Service serviceModel = new ObjectServiceFactory().create(IOneCMDBWebService.class);
			String token = null;

			try {
				cmdbService  = (IOneCMDBWebService)
				new XFireProxyFactory().create(serviceModel, url);

		
				// Generate dependency graph...
				//setupGraph(cmdbService, token);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return(cmdbService);
	}

}

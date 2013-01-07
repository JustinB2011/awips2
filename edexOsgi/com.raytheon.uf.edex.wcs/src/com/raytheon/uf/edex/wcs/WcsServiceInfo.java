/*
 * The following software products were developed by Raytheon:
 *
 * ADE (AWIPS Development Environment) software
 * CAVE (Common AWIPS Visualization Environment) software
 * EDEX (Environmental Data Exchange) software
 * uFrame™ (Universal Framework) software
 *
 * Copyright (c) 2010 Raytheon Co.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 *
 * Contractor Name: Raytheon Company
 * Contractor Address:
 * 6825 Pine Street, Suite 340
 * Mail Stop B8
 * Omaha, NE 68106
 * 402.291.0100
 *
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2011            bclement     Initial creation
 *
 */
package com.raytheon.uf.edex.wcs;

import java.util.LinkedList;
import java.util.List;

public class WcsServiceInfo {

	protected String onlineResource;

	protected List<WcsOperationInfo> operations = new LinkedList<WcsOperationInfo>();

	/**
	 * 
	 */
	public WcsServiceInfo(String onlineResouce) {
		this.onlineResource = onlineResouce;
	}

	public void addOperationInfo(WcsOperationInfo info) {
		operations.add(info);
	}

	/**
	 * @return the onlineResource
	 */
	public String getOnlineResource() {
		return onlineResource;
	}

	/**
	 * @param onlineResource
	 *            the onlineResource to set
	 */
	public void setOnlineResource(String onlineResource) {
		this.onlineResource = onlineResource;
	}

	/**
	 * @return the operations
	 */
	public List<WcsOperationInfo> getOperations() {
		return operations;
	}

	/**
	 * @param operations
	 *            the operations to set
	 */
	public void setOperations(List<WcsOperationInfo> operations) {
		this.operations = operations;
	}

}

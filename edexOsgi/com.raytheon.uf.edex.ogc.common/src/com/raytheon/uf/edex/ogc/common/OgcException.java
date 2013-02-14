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
 */
package com.raytheon.uf.edex.ogc.common;


public class OgcException extends Exception {

	private static final long serialVersionUID = -5832661027013919871L;

	public enum Code {
		InvalidFormat, InvalidCRS, LayerNotDefined, MissingDimensionValue, InvalidDimensionValue, OperationNotSupported, MissingParameterValue, InvalidParameterValue, InternalServerError
	}
	
	protected Code code;

	public OgcException(Code code) {
		super();
		this.code = code;
	}

	public OgcException(Code code, String message) {
		super(message);
		this.code = code;
	}

	public OgcException(Code code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public OgcException(Code code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

	public Code getCode() {
		return code;
	}

	public void setCode(Code code) {
		this.code = code;
	}

}

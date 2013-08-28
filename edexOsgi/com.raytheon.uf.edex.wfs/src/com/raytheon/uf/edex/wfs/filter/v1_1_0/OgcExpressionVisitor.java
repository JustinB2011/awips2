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
 * Apr 21, 2011            bclement     Initial creation
 *
 */
package com.raytheon.uf.edex.wfs.filter.v1_1_0;

import java.util.List;

import net.opengis.filter.v_1_1_0.PropertyNameType;

/**
 * 
 * @author bclement
 * @version 1.0
 */
public interface OgcExpressionVisitor {

	public Object add(ExpressionProcessor left, ExpressionProcessor right,
			Object obj) throws Exception;

	public Object sub(ExpressionProcessor left, ExpressionProcessor right,
			Object obj) throws Exception;

	public Object mul(ExpressionProcessor left, ExpressionProcessor right,
			Object obj) throws Exception;

	public Object div(ExpressionProcessor left, ExpressionProcessor right,
			Object obj) throws Exception;

	public Object literal(List<Object> values, Object obj) throws Exception;

	public Object property(PropertyNameType prop, Object obj) throws Exception;

	public Object function(List<ExpressionProcessor> expressions, String name,
			Object obj) throws Exception;
}

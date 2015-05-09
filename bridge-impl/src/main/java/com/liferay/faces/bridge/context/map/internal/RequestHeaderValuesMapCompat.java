/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.bridge.context.map.internal;

import javax.portlet.PortletRequest;


/**
 * @author  Neil Griffin
 */
public abstract class RequestHeaderValuesMapCompat extends CaseInsensitiveHashMap<String[]> {

	// serialVersionUID
	private static final long serialVersionUID = 5256297252491398013L;

	// Private Constants
	private static final String HEADER_TRINIDAD_PPR = "Tr-XHR-Message";

	protected void addJSF1Headers(PortletRequest portletRequest) {

		String[] trinidadPPRHeader = get(HEADER_TRINIDAD_PPR);

		if (trinidadPPRHeader == null) {

			String pprHeader = portletRequest.getProperty(HEADER_TRINIDAD_PPR);

			if (pprHeader != null) {
				put(HEADER_TRINIDAD_PPR, new String[] { pprHeader });
			}
		}
	}
}

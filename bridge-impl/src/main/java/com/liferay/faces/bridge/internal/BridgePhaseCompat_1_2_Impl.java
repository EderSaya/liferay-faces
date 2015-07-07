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
package com.liferay.faces.bridge.internal;

import java.util.Map;

import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.faces.Bridge.PortletPhase;

import com.liferay.faces.bridge.config.BridgeConfig;
import com.liferay.faces.bridge.context.BridgeContext;


/**
 * This class provides a compatibility layer that isolates differences related to JSF 1.2.
 *
 * @author  Neil Griffin
 */
public abstract class BridgePhaseCompat_1_2_Impl extends BridgePhaseBaseImpl {

	// Private Constants
	private static final String MOJARRA_REQUEST_STATE_MANAGER = "com.sun.faces.util.RequestStateManager";
	private static final String MOJARRA_RENDER_KIT_IMPL_FOR_REQUEST = "com.sun.faces.renderKitImplForRequest";

	public BridgePhaseCompat_1_2_Impl(PortletConfig portletConfig, BridgeConfig bridgeConfig) {
		super(portletConfig, bridgeConfig);
	}

	@Override
	protected void init(PortletRequest portletRequest, PortletResponse portletResponse, PortletPhase portletPhase) {

		super.init(portletRequest, portletResponse, portletPhase);

		// Remove the render-kit attribute that Mojarra saves in the request map in order to prevent a
		// ClassCastException during Portlet 2.0 Events IPC.
		@SuppressWarnings("unchecked")
		Map<String, Object> mojarraRequestStateManager = (Map<String, Object>) portletRequest.getAttribute(
				MOJARRA_REQUEST_STATE_MANAGER);

		if (mojarraRequestStateManager != null) {
			mojarraRequestStateManager.remove(MOJARRA_RENDER_KIT_IMPL_FOR_REQUEST);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void removeBridgeContextAttribute(PortletRequest portletRequest) {
		portletRequest.removeAttribute(BridgeExt.BRIDGE_CONTEXT_ATTRIBUTE);
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void setBridgeContextAttribute(PortletRequest portletRequest, BridgeContext bridgeContext) {
		portletRequest.setAttribute(BridgeExt.BRIDGE_CONTEXT_ATTRIBUTE, bridgeContext);
	}
}

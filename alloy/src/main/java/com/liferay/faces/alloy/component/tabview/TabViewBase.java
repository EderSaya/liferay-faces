/**
 * Copyright (c) 2000-2014 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.alloy.component.tabview;
//J-

import javax.annotation.Generated;
import javax.faces.component.UIData;

import com.liferay.faces.util.component.Styleable;
import com.liferay.faces.util.component.ClientComponent;

/**
 * @author	Bruno Basto
 * @author	Kyle Stiemann
 */
@Generated(value = "com.liferay.alloy.tools.builder.FacesBuilder")
public abstract class TabViewBase extends UIData implements Styleable, ClientComponent {

	// Protected Enumerations
	protected enum TabViewPropertyKeys {
		clientKey,
		height,
		stacked,
		style,
		styleClass,
		width
	}

	@Override
	public String getClientKey() {
		return (String) getStateHelper().eval(TabViewPropertyKeys.clientKey, null);
	}

	@Override
	public void setClientKey(String clientKey) {
		getStateHelper().put(TabViewPropertyKeys.clientKey, clientKey);
	}

	public String getHeight() {
		return (String) getStateHelper().eval(TabViewPropertyKeys.height, null);
	}

	public void setHeight(String height) {
		getStateHelper().put(TabViewPropertyKeys.height, height);
	}

	public Boolean isStacked() {
		return (Boolean) getStateHelper().eval(TabViewPropertyKeys.stacked, null);
	}

	public void setStacked(Boolean stacked) {
		getStateHelper().put(TabViewPropertyKeys.stacked, stacked);
	}

	@Override
	public String getStyle() {
		return (String) getStateHelper().eval(TabViewPropertyKeys.style, null);
	}

	@Override
	public void setStyle(String style) {
		getStateHelper().put(TabViewPropertyKeys.style, style);
	}

	@Override
	public String getStyleClass() {
		return (String) getStateHelper().eval(TabViewPropertyKeys.styleClass, null);
	}

	@Override
	public void setStyleClass(String styleClass) {
		getStateHelper().put(TabViewPropertyKeys.styleClass, styleClass);
	}

	public String getWidth() {
		return (String) getStateHelper().eval(TabViewPropertyKeys.width, null);
	}

	public void setWidth(String width) {
		getStateHelper().put(TabViewPropertyKeys.width, width);
	}
}
//J+

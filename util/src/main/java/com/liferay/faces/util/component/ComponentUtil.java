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
package com.liferay.faces.util.component;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.ValueHolder;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import com.liferay.faces.util.lang.StringPool;


/**
 * @author  Neil Griffin
 */
public class ComponentUtil {

	// Private Constants
	private static final String DOUBLE_BACKSLASH_COLON = "\\\\\\\\:";
	private static final String REGEX_COLON = "[:]";

	public static String appendToCssClasses(String cssClass, String suffix) {

		String value = cssClass;

		if (value != null) {
			value = value.trim();

			if (value.length() > 0) {
				StringBuilder buf = new StringBuilder();
				String[] cssClasses = cssClass.trim().split(StringPool.SPACE);
				boolean firstClass = true;

				for (String curCssClass : cssClasses) {

					if (firstClass) {
						firstClass = false;
					}
					else {
						buf.append(StringPool.SPACE);
					}

					buf.append(curCssClass);
					buf.append(suffix);
				}

				value = buf.toString();
			}
		}

		return value;
	}

	public static String concatCssClasses(String... classNames) {

		StringBuilder cssClassBuilder = new StringBuilder();
		boolean first = true;

		for (String className : classNames) {

			if (className != null) {

				if (!first) {
					cssClassBuilder.append(StringPool.SPACE);
				}

				cssClassBuilder.append(className);
				first = false;
			}
		}

		String allClasses = cssClassBuilder.toString();

		if (allClasses.length() == 0) {
			allClasses = null;
		}

		return allClasses;
	}

	public static Object convertSubmittedValue(FacesContext facesContext, ValueHolder valueHolder,
		Object submittedValue) {

		Object convertedValue = submittedValue;

		if ((valueHolder != null) && (submittedValue != null)) {

			UIComponent uiComponent = (UIComponent) valueHolder;
			Converter converter = valueHolder.getConverter();

			if (converter == null) {

				ValueExpression valueExpression = uiComponent.getValueExpression(StringPool.VALUE);

				if (valueExpression != null) {

					ELContext elContext = facesContext.getELContext();
					Class<?> converterClassType = valueExpression.getType(elContext);

					if ((converterClassType != null) && !converterClassType.equals(Object.class)) {

						Application application = facesContext.getApplication();
						converter = application.createConverter(converterClassType);
					}
				}
			}

			if (converter != null) {
				String submittedValueAsString = submittedValue.toString();
				convertedValue = converter.getAsObject(facesContext, uiComponent, submittedValueAsString);
			}
		}

		return convertedValue;
	}

	public static String escapeClientId(String clientId) {
		String escapedClientId = clientId;

		if (escapedClientId != null) {

			// JSF clientId values contain colons, which must be preceeded by double backslashes in order to have them
			// work with JavaScript functions like AUI.one(String). http://yuilibrary.com/projects/yui3/ticket/2528057
			escapedClientId = escapedClientId.replaceAll(REGEX_COLON, DOUBLE_BACKSLASH_COLON);
		}

		return escapedClientId;
	}

	public static String findClientId(String expression) {
		return findClientId(FacesContext.getCurrentInstance(), expression);
	}

	public static String findClientId(FacesContext facesContext, String expression) {
		String clientId = null;
		UIViewRoot uiViewRoot = facesContext.getViewRoot();
		UIComponent uiComponent = uiViewRoot.findComponent(expression);

		if (uiComponent == null) {
			uiComponent = matchComponentInHierarchy(facesContext, uiViewRoot, expression);
		}

		if (uiComponent != null) {
			clientId = uiComponent.getClientId(facesContext);
		}

		return clientId;
	}

	public static UIComponent matchComponentInHierarchy(FacesContext facesContext, UIComponent parent,
		String partialClientId) {
		UIComponent uiComponent = null;

		if (parent != null) {

			String parentClientId = parent.getClientId(facesContext);

			if ((parentClientId != null) && (parentClientId.indexOf(partialClientId) >= 0)) {
				uiComponent = parent;
			}
			else {
				Iterator<UIComponent> itr = parent.getFacetsAndChildren();

				if (itr != null) {

					while (itr.hasNext()) {
						UIComponent child = itr.next();
						uiComponent = matchComponentInHierarchy(facesContext, child, partialClientId);

						if (uiComponent != null) {
							break;
						}
					}
				}
			}
		}

		return uiComponent;
	}

	public static UIComponent matchComponentInViewRoot(FacesContext facesContext, String partialClientId) {
		return matchComponentInHierarchy(facesContext, facesContext.getViewRoot(), partialClientId);
	}

	public static String getClientVarName(FacesContext facesContext, ClientComponent clientComponent) {

		char separatorChar = UINamingContainer.SEPARATOR_CHAR;
		String clientId = clientComponent.getClientId();
		String regex = StringPool.OPEN_BRACKET + separatorChar + StringPool.CLOSE_BRACKET;
		String clientVarName = clientId.replaceAll(regex, StringPool.UNDERLINE);

		return clientVarName;
	}

	public static String getComponentLabel(UIComponent uiComponent) {

		String componentLabel = null;

		if (uiComponent != null) {

			componentLabel = getParentFieldLabelValue(uiComponent);

			if (componentLabel == null) {
				componentLabel = getSiblingLabelValue((uiComponent));
			}
		}

		return componentLabel;
	}

	private static String getParentFieldLabelValue(UIComponent uiComponent) {

		String parentFieldLabel = null;

		if (uiComponent != null) {
			UIComponent parent = uiComponent.getParent();

			if (parent != null) {

				Method method = null;

				try {
					method = parent.getClass().getMethod("getLabel", (Class<?>[]) null);
				}
				catch (NoSuchMethodException e) {
					// ignore
				}

				if (method != null) {

					try {
						parentFieldLabel = (String) method.invoke(parent, (Object[]) null);
					}
					catch (Exception e) {
						// ignore
					}
				}
				else {
					parentFieldLabel = getParentFieldLabelValue(parent);
				}
			}
		}

		return parentFieldLabel;
	}

	private static String getSiblingLabelValue(UIComponent uiComponent) {

		String siblingLabelValue = null;

		if (uiComponent != null) {

			UIComponent parent = uiComponent.getParent();

			if (parent != null) {

				List<UIComponent> children = parent.getChildren();

				if (children != null) {

					for (UIComponent child : children) {

						if (child instanceof HtmlOutputLabel) {
							HtmlOutputLabel htmlOutputLabel = (HtmlOutputLabel) child;

							if (uiComponent.getId().equals(htmlOutputLabel.getFor())) {
								Object labelValue = htmlOutputLabel.getValue();

								if (labelValue != null) {
									siblingLabelValue = labelValue.toString();
								}
							}
						}
					}
				}
			}
		}

		return siblingLabelValue;
	}
}

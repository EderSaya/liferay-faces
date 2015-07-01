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
package com.liferay.faces.portal.renderkit;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.filter.PortletRequestWrapper;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import com.liferay.faces.portal.component.InputEditorInternal;
import com.liferay.faces.portal.render.internal.JspIncludeResponse;
import com.liferay.faces.portal.render.internal.ScriptTagUtil;
import com.liferay.faces.portal.servlet.ScriptCapturingHttpServletRequest;
import com.liferay.faces.util.context.FacesRequestContext;
import com.liferay.faces.util.factory.FactoryExtensionFinder;
import com.liferay.faces.util.jsp.JspAdapterFactory;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.liferay.portal.kernel.editor.EditorUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;


/**
 * This is a renderer for the liferay-ui-internal:input-editor component.
 *
 * @author  Neil Griffin
 */
public class InputEditorInternalRenderer extends Renderer {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(InputEditorInternalRenderer.class);

	// Private Constants
	private static final String COMMENT_CDATA_CLOSE = "// ]]>";
	private static final String CKEDITOR = "ckeditor";
	private static final String CDPL_INITIALIZE_FALSE = "var customDataProcessorLoaded = false;";
	private static final String CDPL_INITIALIZE_TRUE = "var customDataProcessorLoaded = true;";

	@Override
	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		super.encodeBegin(facesContext, uiComponent);

		InputEditorInternal inputEditorInternal = (InputEditorInternal) uiComponent;
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		ExternalContext externalContext = facesContext.getExternalContext();
		PortletRequest portletRequest = (PortletRequest) externalContext.getRequest();
		PortletResponse portletResponse = (PortletResponse) externalContext.getResponse();
		PortletRequest liferayPortletRequest = getLiferayPortletRequest(portletRequest);
		boolean resourcePhase = (liferayPortletRequest instanceof ResourceRequest);
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(portletRequest);
		HttpServletRequest scriptCapturingHttpServletRequest = new ScriptCapturingHttpServletRequest(
				httpServletRequest);

		HttpServletResponse httpServletResponse = PortalUtil.getHttpServletResponse(portletResponse);
		Map<String, Object> attributes = inputEditorInternal.getAttributes();
		String editorImpl = (String) attributes.get("editorImpl");

		if (editorImpl == null) {
			editorImpl = CKEDITOR;
		}

		// Build up a URL that can be used to invoke the liferay-ui:input-editor JSP tag.
		String url = "/resources/liferay-ui/jsp/input-editor.jsp";
		StringBuilder queryString = new StringBuilder();
		queryString.append("?editorImpl=");
		queryString.append(editorImpl);
		queryString.append("&height=");
		queryString.append(attributes.get("height"));
		queryString.append("&initMethod=");
		queryString.append(attributes.get("initMethod"));
		queryString.append("&name=");

		String editorName = (String) attributes.get("name");
		char separatorChar = UINamingContainer.getSeparatorChar(facesContext);
		editorName = editorName.replace(separatorChar, '_').concat("_jsptag");

		queryString.append(editorName);
		queryString.append("&onBlurMethod=");
		queryString.append(attributes.get("onBlurMethod"));
		queryString.append("&onChangeMethod=");
		queryString.append(attributes.get("onChangeMethod"));
		queryString.append("&onFocusMethod=");
		queryString.append(attributes.get("onFocusMethod"));
		queryString.append("&skipEditorLoading=");

		if (resourcePhase) {

			// FACES-1439: Ensure that the <script src=".../ckeditor.js" /> element is not included in the response by
			// specifying skipEditorLoading="true" during Ajax requests.
			queryString.append(Boolean.TRUE.toString());
		}
		else {
			queryString.append(Boolean.FALSE.toString());
		}

		queryString.append("&toolbarSet=");
		queryString.append(attributes.get("toolbarSet"));
		queryString.append("&width=");
		queryString.append(attributes.get("width"));
		url = url + queryString.toString();

		// Invoke the tag and capture it's output in a String, rather than having the output go directly to the
		// response.
		RequestDispatcher requestDispatcher = scriptCapturingHttpServletRequest.getRequestDispatcher(url);
		JspIncludeResponse jspIncludeResponse = new JspIncludeResponse(httpServletResponse);

		// FACES-1713: ThemeDisplay.isLifecycleResource() must return false during execution of the request dispatcher.
		ThemeDisplay themeDisplay = (ThemeDisplay) scriptCapturingHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);
		boolean lifecycleResourceBackup = themeDisplay.isLifecycleResource();
		themeDisplay.setLifecycleResource(false);

		try {
			requestDispatcher.include(scriptCapturingHttpServletRequest, jspIncludeResponse);
		}
		catch (ServletException e) {
			logger.error(e.getMessage());
			throw new IOException(e.getMessage());
		}

		// FACES-1713: Restore the value of ThemeDisplay.isLifecycleResource().
		themeDisplay.setLifecycleResource(lifecycleResourceBackup);

		String bufferedResponse = jspIncludeResponse.getBufferedResponse();

		if (bufferedResponse != null) {

			// Note: Trim the buffered response since there is typically over 100 newlines at the beginning.
			bufferedResponse = bufferedResponse.trim();

			// If rendering an instance of the CKEditor, then
			String clientId = inputEditorInternal.getClientId();
			String editorType = EditorUtil.getEditorValue(scriptCapturingHttpServletRequest, editorImpl);

			if (editorType.indexOf(CKEDITOR) >= 0) {

				// FACES-1439: If the component was rendered on the page on the previous JSF lifecycle, then prevent it
				// from being re-initialized by removing all <script>...</script> elements.
				boolean scriptsRemoved = false;

				if (resourcePhase && inputEditorInternal.isPreviouslyRendered()) {

					logger.debug("Preventing re-initialization of CKEditor for clientId=[{0}]", clientId);

					ParsedResponse parsedResponse = new ParsedResponse(bufferedResponse);
					bufferedResponse = parsedResponse.getNonScripts();
					scriptsRemoved = true;
				}

				// FACES-1422: Move the scripts to the <eval>...</eval> section of the partial-response so that they
				// will execute properly. This has the added benefit of preempt a DOM-diff with ICEfaces.
				if (resourcePhase && !scriptsRemoved) {

					logger.debug(
						"Moving CKEditor scripts to <eval>...</eval> section of the partial-response for clientId=[{0}]",
						clientId);

					ParsedResponse parsedResponse = new ParsedResponse(bufferedResponse);
					bufferedResponse = parsedResponse.getNonScripts();

					String scripts = parsedResponse.getScripts();

					FacesRequestContext facesRequestContext = FacesRequestContext.getCurrentInstance();
					facesRequestContext.addScript(scripts);
					logger.trace(scripts);
				}
			}

			// Write the captured HTML markup from the JSP tag to the Faces responseWriter.
			logger.trace(bufferedResponse);
			responseWriter.write(bufferedResponse);

			// The JSP (executed by the RequestDispatcher) encounters <aui:script> JSP tags that ultimately save
			// scripts in the WebKeys.AUI_SCRIPT_DATA request attribute. The intent (in a JSP environment) is to let
			// the ScriptDataPortletFilter render the script content at the bottom of the portal page on the initial
			// RenderRequest. However, in a JSF environment we need to render the scripts directly to the response
			// as part of the component markup, because the JSF environment might be utilizing the DOM-diff feature
			// of ICEfaces. If the scripts were rendered at the bottom of the page during the RenderRequest, and
			// then rendered inline during a subsequent ResourceRequest, then ICEfaces would detect a DOM-diff and
			// unnecessarily replace the DOM with a new editor.
			try {

				// Capture the scripts into a String.
				JspAdapterFactory jspAdapterFactory = (JspAdapterFactory) FactoryExtensionFinder.getFactory(
						JspAdapterFactory.class);
				JspWriter stringJspWriter = jspAdapterFactory.getStringJspWriter();
				PageContext pageContext = jspAdapterFactory.getStringPageContext(scriptCapturingHttpServletRequest,
						httpServletResponse, facesContext.getELContext(), stringJspWriter);

				// Note that flushing the ScriptData will only flush and write the scripts that were added by the
				// request dispatcher. This is because the ScriptCapturingHttpServletRequest protects the
				// WebKeys.AUI_SCRIPT_DATA in the underlying HttpServletRequest.
				ScriptTagUtil.flushScriptData(pageContext);

				String javaScriptFromRequestDispatcher = stringJspWriter.toString();

				// Remove all the "<![CDATA[" and "]]>" tokens since they will interfere with the JSF
				// partial-response.
				String[] tokensToRemove = new String[] { "<![CDATA[", "]]>" };

				for (String token : tokensToRemove) {
					int pos = javaScriptFromRequestDispatcher.indexOf(token);

					while (pos >= 0) {
						javaScriptFromRequestDispatcher = javaScriptFromRequestDispatcher.substring(0, pos) +
							javaScriptFromRequestDispatcher.substring(pos + token.length());
						pos = javaScriptFromRequestDispatcher.indexOf(token);
					}
				}

				// Create a JavaScript fragment that will change the way that the customDataProcessorLoaded
				// variable is initialized. Normally it is initialized to false, but if there is an old CKEditor
				// that was destroyed, then it should be initialized to true. That will guarantee that the
				// new CKEditor in the DOM will have its setData() method called with the value from the
				// hidden field.
				StringBuilder javaScriptFragment = new StringBuilder();
				javaScriptFragment.append("var oldEditor = CKEDITOR.instances['");
				javaScriptFragment.append(editorName);
				javaScriptFragment.append("'];if (oldEditor){");
				javaScriptFragment.append(CDPL_INITIALIZE_TRUE);
				javaScriptFragment.append("}else{");
				javaScriptFragment.append(CDPL_INITIALIZE_FALSE);
				javaScriptFragment.append("}");

				// Insert the JavaScript fragment into the JavaScript code at the appropriate location.
				int pos = javaScriptFromRequestDispatcher.indexOf(CDPL_INITIALIZE_FALSE);

				if (pos > 0) {
					javaScriptFromRequestDispatcher = javaScriptFromRequestDispatcher.substring(0, pos) +
						javaScriptFragment.toString() +
						javaScriptFromRequestDispatcher.substring(pos + CDPL_INITIALIZE_FALSE.length());
				}
				else {
					javaScriptFromRequestDispatcher = javaScriptFromRequestDispatcher + "\n" +
						javaScriptFragment.toString();
				}

				responseWriter.write(javaScriptFromRequestDispatcher);
			}
			catch (Exception e) {
				logger.error(e);
				throw new IOException(e.getMessage());
			}
		}
	}

	protected PortletRequest getLiferayPortletRequest(PortletRequest portletRequest) {

		PortletRequest liferayPortletRequest = portletRequest;

		if (liferayPortletRequest instanceof PortletRequestWrapper) {
			PortletRequestWrapper portletRequestWrapper = (PortletRequestWrapper) portletRequest;
			liferayPortletRequest = getLiferayPortletRequest(portletRequestWrapper.getRequest());
		}

		return liferayPortletRequest;
	}

	protected String getPortletId(PortletRequest portletRequest) {
		String portletId = " ";
		Portlet portlet = (Portlet) portletRequest.getAttribute(WebKeys.RENDER_PORTLET);

		if (portlet != null) {
			portletId = portlet.getPortletId();
		}

		return portletId;
	}

	protected class ParsedResponse {

		private String scripts;
		private String nonScripts;

		public ParsedResponse(String response) {

			StringBuilder scriptBuilder = new StringBuilder();

			boolean done1 = false;

			while (!done1) {
				int beginPos = response.indexOf("<script>");
				int endPos = response.indexOf("</script>", beginPos);

				if ((beginPos >= 0) && (endPos > beginPos)) {
					String script = response.substring(beginPos, endPos + "</script>".length());

					boolean done2 = false;

					while (!done2) {
						int cdataOpenPos = script.indexOf("<![CDATA[");

						if (cdataOpenPos > 0) {
							script = script.substring(cdataOpenPos + "<![CDATA[".length());

							int cdataClosePos = script.indexOf(COMMENT_CDATA_CLOSE);

							if (cdataClosePos > 0) {
								script = script.substring(0, cdataClosePos);
							}
							else {
								cdataClosePos = script.indexOf("]]>");

								if (cdataClosePos > 0) {
									script = script.substring(0, cdataClosePos);
								}
							}
						}
						else {
							done2 = true;
						}
					}

					scriptBuilder.append(script);
					response = response.substring(0, beginPos) + response.substring(endPos + "</script>".length());
				}
				else {
					done1 = true;
				}
			}

			this.scripts = scriptBuilder.toString().trim();
			this.nonScripts = response.trim();
		}

		public String getNonScripts() {
			return nonScripts;
		}

		public String getScripts() {
			return scripts;
		}

	}
}

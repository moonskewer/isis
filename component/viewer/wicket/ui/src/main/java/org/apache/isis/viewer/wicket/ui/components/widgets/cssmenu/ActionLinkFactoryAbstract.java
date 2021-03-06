/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.IResourceStream;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionPanel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.actionprompt.ActionPromptPage;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Links;

public abstract class ActionLinkFactoryAbstract implements ActionLinkFactory {

    private static final long serialVersionUID = 1L;

    public static void addTargetBlankIfActionReturnsUrl(final AbstractLink link, final ObjectAction action) {
        final ObjectSpecification returnType = action.getReturnType();
        if(returnType != null && "java.net.URL".equals(returnType.getFullIdentifier())) {
            link.add(new AttributeAppender("target", Model.of("_blank")));
            link.add(new CssClassAppender("noVeil"));
        }
    }
    
    /**
     * Either creates a link for the action be rendered in a {@link ModalWindow}, or (if none can be
     * {@link ActionPromptProvider#getActionPrompt() provided}, or creates a link to 
     * the {@link ActionPromptPage} (ie the {@link PageClassRegistry registered page} for 
     * {@link PageType#ACTION_PROMPT action}s).
     * 
     * <p>
     * If the action's {@link ObjectAction#getSemantics() semantics} are {@link ActionSemantics.Of#SAFE safe}, then
     * concurrency checking is disabled; otherwise it is enforced.
     */
    protected AbstractLink newLink(
            final String linkId, 
            final ObjectAdapter objectAdapter, final ObjectAction action, 
            final ActionPromptProvider actionPromptProvider) {
        
        final ActionPrompt actionPrompt = actionPromptProvider.getActionPrompt();
        if(actionPrompt != null) {
            final ActionModel actionModel = ActionModel.create(objectAdapter, action);
            actionModel.setActionPrompt(actionPrompt);
            
            final AjaxDeferredBehaviour ajaxDeferredBehaviour = determineDeferredBehaviour(action, actionModel);

            final AbstractLink link = new AjaxLink<Object>(linkId) {
                private static final long serialVersionUID = 1L;
                
                @Override
                public void onClick(AjaxRequestTarget target) {
                    
                    if(ajaxDeferredBehaviour != null) {
                        ajaxDeferredBehaviour.initiate(target);
                    } else {
                        final ActionPanel actionPromptPanel = 
                                (ActionPanel) getComponentFactoryRegistry().createComponent(
                                        ComponentType.ACTION_PROMPT, actionPrompt.getContentId(), actionModel);
                        
                        actionPrompt.setPanel(actionPromptPanel, target);
                        actionPrompt.show(target);
                        
                        target.focusComponent(actionPromptPanel);
                    }
                }
            };

            if(ajaxDeferredBehaviour != null) {
                link.add(ajaxDeferredBehaviour);
            }

            link.add(new CssClassAppender("noVeil"));

            return link;
            
        } else {
            
            // use the action semantics to determine whether invoking this action will require a concurrency check or not
            // if it's "safe", then we'll just continue without any checking. 
            final ConcurrencyChecking concurrencyChecking = ConcurrencyChecking.concurrencyCheckingFor(action.getSemantics());
            final PageParameters pageParameters = ActionModel.createPageParameters(objectAdapter, action, concurrencyChecking);
            final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ACTION_PROMPT);
            AbstractLink link = Links.newBookmarkablePageLink(linkId, pageParameters, pageClass);
            
            // special case handling if this a no-arg action is returning a URL
            if(action.getParameterCount()==0) {
                addTargetBlankIfActionReturnsUrl(link, action);
            }

            return link;
        }
    }

    private static AjaxDeferredBehaviour determineDeferredBehaviour(final ObjectAction action, final ActionModel actionModel) {
        if(isNoArgReturnTypeRedirect(action)) {
            return new AjaxDeferredBehaviour() {
                
                private static final long serialVersionUID = 1L;

                @Override
                protected IRequestHandler getRequestHandler() {
                    ObjectAdapter resultAdapter = executeActionHandlingApplicationExceptions(actionModel);
                    final Object value = resultAdapter.getObject();
                    return ActionModel.redirectHandler(value);
                }
            };
        } 
        if(isNoArgReturnTypeDownload(action)) {
            return new AjaxDeferredBehaviour() {
                
                private static final long serialVersionUID = 1L;
   
                @Override
                protected IRequestHandler getRequestHandler() {
                    ObjectAdapter resultAdapter = executeActionHandlingApplicationExceptions(actionModel);
                    final Object value = resultAdapter.getObject();
                    return ActionModel.downloadHandler(value);
                }
            };
        } 
        return null;
    }

    private static boolean isNoArgReturnTypeRedirect(final ObjectAction action) {
        return action.getParameterCount() == 0 &&
               action.getReturnType() != null && 
               action.getReturnType().getCorrespondingClass() == java.net.URL.class;
    }

    private static boolean isNoArgReturnTypeDownload(final ObjectAction action) {
        return action.getParameterCount() == 0 && action.getReturnType() != null && 
                (action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Blob.class ||
                action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Clob.class);
    }
    
    // adapted from similar code in ActionPanel :-(
    private static ObjectAdapter executeActionHandlingApplicationExceptions(final ActionModel actionModel) {
        try {
            return actionModel.getObject();

        } catch (RuntimeException ex) {
            
            // see if is an application-defined exception
            final ApplicationException appEx = ActionModel.getApplicationExceptionIfAny(ex);
            if (appEx != null) {
                IsisContext.getMessageBroker().setApplicationError(appEx.getMessage());
                return null;
            } 

            // not handled, so propagate
            throw ex;
        }
    }

    protected LinkAndLabel newLinkAndLabel(final ObjectAction action, final AbstractLink link, final String disabledReasonIfAny) {
        final String label = ObjectAction.Utils.nameFor(action);
        final boolean blobOrClob = CssMenuItem.returnsBlobOrClob(action);
        final boolean prototype = CssMenuItem.isExplorationOrPrototype(action);
        final String actionIdentifier = CssMenuItem.actionIdentifierFor(action);
        final String cssClass = CssMenuItem.cssClassFor(action);
        
        return new LinkAndLabel(link, label, disabledReasonIfAny, blobOrClob, prototype, actionIdentifier, cssClass);
    }


    // ////////////////////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////////////////////
    
    protected ComponentFactoryRegistry getComponentFactoryRegistry() {
        return ((ComponentFactoryRegistryAccessor)Application.get()).getComponentFactoryRegistry();
    }
    
    protected PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) Application.get()).getPageClassRegistry();
    }

}

/*******************************************************************************
 * Copyright (c) 2019, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.client.security;

import javax.jms.JMSException;

import org.eclipse.kapua.client.security.amqpclient.Client;
import org.eclipse.kapua.client.security.bean.AuthRequest;
import org.eclipse.kapua.client.security.bean.AuthResponse;
import org.eclipse.kapua.client.security.bean.EntityRequest;
import org.eclipse.kapua.client.security.bean.EntityResponse;
import org.eclipse.kapua.client.security.bean.Request;
import org.eclipse.kapua.client.security.bean.ResponseContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Security service. Implementation through AMQP messaging layer.
 */
public class ServiceClientMessagingImpl implements ServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ServiceClientMessagingImpl.class);

    private static final int TIMEOUT = 5000;
    private final KapuaMessageListener messageListener;

    private Client client;

    public ServiceClientMessagingImpl(KapuaMessageListener messageListener, Client client) {
        this.messageListener = messageListener;
        this.client = client;
    }

    @Override
    public AuthResponse brokerConnect(AuthRequest authRequest)
            throws InterruptedException, JMSException, JsonProcessingException {//TODO review exception when Kapua code will be linked (throw KapuaException)
        String requestId = MessageHelper.getNewRequestId();
        authRequest.setRequestId(requestId);
        authRequest.setAction(SecurityAction.brokerConnect.name());
        ResponseContainer<AuthResponse> responseContainer = ResponseContainer.createAnRegisterNewMessageContainer(messageListener, authRequest);
        logRequest(authRequest);
        client.sendMessage(MessageHelper.getBrokerConnectMessage(client.createTextMessage(), authRequest));
        synchronized (responseContainer) {
            responseContainer.wait(TIMEOUT);
        }
        messageListener.removeCallback(requestId);
        return responseContainer.getResponse();
    }

    @Override
    public AuthResponse brokerDisconnect(AuthRequest authRequest) throws JMSException, InterruptedException, JsonProcessingException {
        String requestId = MessageHelper.getNewRequestId();
        authRequest.setRequestId(requestId);
        authRequest.setAction(SecurityAction.brokerDisconnect.name());
        ResponseContainer<AuthResponse> responseContainer = ResponseContainer.createAnRegisterNewMessageContainer(messageListener, authRequest);
        logRequest(authRequest);
        client.sendMessage(MessageHelper.getBrokerDisconnectMessage(client.createTextMessage(), authRequest));
        synchronized (responseContainer) {
            responseContainer.wait(TIMEOUT);
        }
        messageListener.removeCallback(requestId);
        return responseContainer.getResponse();
    }

    @Override
    public EntityResponse getEntity(EntityRequest entityRequest) throws JMSException, InterruptedException, JsonProcessingException {
        String requestId = MessageHelper.getNewRequestId();
        entityRequest.setRequestId(requestId);
        ResponseContainer<EntityResponse> responseContainer = ResponseContainer.createAnRegisterNewMessageContainer(messageListener, entityRequest);
        logRequest(entityRequest);
        client.sendMessage(MessageHelper.getEntityMessage(client.createTextMessage(), entityRequest));
        synchronized (responseContainer) {
            responseContainer.wait(TIMEOUT);
        }
        messageListener.removeCallback(requestId);
        return responseContainer.getResponse();
    }

    private void logRequest(Request request) {
        logger.info("Request id: {} - action: {} - requester: {}",
                request.getRequestId(), request.getAction(), request.getRequester());
    }
}

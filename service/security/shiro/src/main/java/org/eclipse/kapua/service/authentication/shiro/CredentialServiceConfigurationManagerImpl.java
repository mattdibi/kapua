/*******************************************************************************
 * Copyright (c) 2016, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.authentication.shiro;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaIllegalArgumentException;
import org.eclipse.kapua.commons.configuration.RootUserTester;
import org.eclipse.kapua.commons.configuration.ServiceConfigRepository;
import org.eclipse.kapua.commons.configuration.ServiceConfigurationManagerImpl;
import org.eclipse.kapua.model.config.metatype.KapuaTocd;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authentication.AuthenticationDomains;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.credential.shiro.CredentialServiceImpl;
import org.eclipse.kapua.service.authentication.shiro.setting.KapuaAuthenticationSetting;
import org.eclipse.kapua.service.authentication.shiro.setting.KapuaAuthenticationSettingKeys;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.storage.TxManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CredentialServiceConfigurationManagerImpl extends ServiceConfigurationManagerImpl implements CredentialServiceConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialServiceConfigurationManagerImpl.class);
    private final int systemMinimumPasswordLength;

    public CredentialServiceConfigurationManagerImpl(
            TxManager txManager,
            ServiceConfigRepository serviceConfigRepository,
            PermissionFactory permissionFactory,
            AuthorizationService authorizationService,
            RootUserTester rootUserTester) {
        super(CredentialService.class.getName(),
                AuthenticationDomains.CREDENTIAL_DOMAIN,
                txManager,
                serviceConfigRepository,
                permissionFactory,
                authorizationService,
                rootUserTester);
        systemMinimumPasswordLength = fixMinimumPasswordLength();
    }

    @Override
    protected boolean validateNewConfigValuesCoherence(KapuaTocd ocd, Map<String, Object> updatedProps, KapuaId scopeId, Optional<KapuaId> parentId) throws KapuaException {
        if (updatedProps.get(CredentialServiceImpl.PASSWORD_MIN_LENGTH_ACCOUNT_CONFIG_KEY) != null) {
            // If we're going to set a new limit, check that it's not less than system limit
            int newPasswordLimit = Integer.parseInt(updatedProps.get(CredentialServiceImpl.PASSWORD_MIN_LENGTH_ACCOUNT_CONFIG_KEY).toString());
            if (newPasswordLimit < systemMinimumPasswordLength || newPasswordLimit > CredentialServiceImpl.SYSTEM_MAXIMUM_PASSWORD_LENGTH) {
                throw new KapuaIllegalArgumentException(CredentialServiceImpl.PASSWORD_MIN_LENGTH_ACCOUNT_CONFIG_KEY, String.valueOf(newPasswordLimit));
            }
        }
        return true;
    }

    private int fixMinimumPasswordLength() {
        final int systemMinimumPasswordLength;
        //TODO: Why is this logic in a constructor?
        int minPasswordLengthConfigValue;
        try {
            minPasswordLengthConfigValue = KapuaAuthenticationSetting.getInstance().getInt(KapuaAuthenticationSettingKeys.AUTHENTICATION_CREDENTIAL_USERPASS_PASSWORD_MINLENGTH);
        } catch (NoSuchElementException ex) {
            LOGGER.warn("Minimum password length not set, 12 characters minimum will be enforced");
            minPasswordLengthConfigValue = 12;
        }
        if (minPasswordLengthConfigValue < 12) {
            LOGGER.warn("Minimum password length too short, 12 characters minimum will be enforced");
            minPasswordLengthConfigValue = 12;
        }
        systemMinimumPasswordLength = minPasswordLengthConfigValue;
        return systemMinimumPasswordLength;
    }

    @Override
    public int getSystemMinimumPasswordLength() {
        return systemMinimumPasswordLength;
    }
}
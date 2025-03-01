/*******************************************************************************
 * Copyright (c) 2021, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.device.management.configuration.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.kapua.commons.core.AbstractKapuaModule;
import org.eclipse.kapua.commons.jpa.KapuaJpaTxManagerFactory;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.device.management.configuration.DeviceConfigurationFactory;
import org.eclipse.kapua.service.device.management.configuration.DeviceConfigurationManagementService;
import org.eclipse.kapua.service.device.management.configuration.internal.settings.DeviceConfigurationManagementSettings;
import org.eclipse.kapua.service.device.management.configuration.store.DeviceConfigurationStoreService;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.service.device.registry.event.DeviceEventFactory;
import org.eclipse.kapua.service.device.registry.event.DeviceEventService;

import com.google.inject.Provides;

public class DeviceManagementConfigurationModule extends AbstractKapuaModule {

    @Override
    protected void configureModule() {
        bind(DeviceConfigurationFactory.class).to(DeviceConfigurationFactoryImpl.class).in(Singleton.class);
        bind(DeviceConfigurationManagementSettings.class).in(Singleton.class);
    }

    @Provides
    @Inject
    DeviceConfigurationManagementService deviceConfigurationManagementService(
            AuthorizationService authorizationService,
            PermissionFactory permissionFactory,
            DeviceEventService deviceEventService,
            DeviceEventFactory deviceEventFactory,
            DeviceRegistryService deviceRegistryService,
            DeviceConfigurationFactory deviceConfigurationFactory,
            DeviceConfigurationStoreService deviceConfigurationStoreService,
            KapuaJpaTxManagerFactory jpaTxManagerFactory,
            XmlUtil xmlUtil) {
        return new DeviceConfigurationManagementServiceImpl(
                jpaTxManagerFactory.create("kapua-device_management_operation_registry"),
                authorizationService,
                permissionFactory,
                deviceEventService,
                deviceEventFactory,
                deviceRegistryService,
                deviceConfigurationFactory,
                deviceConfigurationStoreService,
                xmlUtil
        );
    }
}

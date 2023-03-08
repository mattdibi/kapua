/*******************************************************************************
 * Copyright (c) 2017, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.job.execution;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.storage.KapuaUpdatableEntityRepository;
import org.eclipse.kapua.storage.TxContext;

public interface JobExecutionRepository
        extends KapuaUpdatableEntityRepository<JobExecution, JobExecutionListResult> {
    long countByJobId(TxContext tx, KapuaId scopeId, KapuaId jobId) throws KapuaException;
}

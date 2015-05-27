/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.shared;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

/**
 * Result of a cherry-pick.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface CherryPickResult {
    public enum CherryPickStatus {
        OK, FAILED, CONFLICTS
    }

    /** @return status of merge */
    CherryPickStatus getStatus();

    /** @return files that failed to merge (not files that has conflicts). */
    List<String> getFailed();
}
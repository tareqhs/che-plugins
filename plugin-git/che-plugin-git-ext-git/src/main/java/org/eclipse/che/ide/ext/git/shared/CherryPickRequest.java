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
 * Cherry-pick some commit into the current branch.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface CherryPickRequest extends GitRequest {

    List<String> getRefSpec();

    void setRefSpec(List<String> refSpec);

    CherryPickRequest withRefSpec(List<String> refSpec);

}

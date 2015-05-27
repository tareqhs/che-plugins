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
 * Rebase the current HEAD relative to the remote branch it is set to track.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface RevertRequest extends GitRequest {

    List<String> getRefSpec();

    void setRefSpec(List<String> refSpec);

    RevertRequest withRefSpec(List<String> refSpec);

}

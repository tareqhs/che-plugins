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

import org.eclipse.che.dto.shared.DTO;

/**
 * Rebase the current HEAD relative to the remote branch it is set to track.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface RebaseRequest extends GitRequest {

    /**
     * Type of rebase operation.
     */
    public enum RebaseOperation {
        /** Change the ref and the index, the workdir is not changed (default). */
        ABORT("--abort"),
        /** Just change the ref, the index and workdir are not changed. */
        CONTINUE("--continue"),
        /** Change the ref, the index and the workdir. */
        SKIP("--skip");

        private final String value;

        private RebaseOperation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * The operation performed by this rebase: --abort, --continue or --skip. A null (default) initiates a new rebase.
     */
    public RebaseOperation getOperation();

    public void setOperation(RebaseOperation v);

    public RebaseRequest withOperation(RebaseOperation v);

}

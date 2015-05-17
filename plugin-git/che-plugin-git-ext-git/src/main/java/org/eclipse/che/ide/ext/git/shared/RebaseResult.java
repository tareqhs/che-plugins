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
 * The result of a Git rebase operation.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface RebaseResult {
    public enum RebaseStatus {
        OK("OK"), //
        ABORTED("Aborted"), //
        FAST_FORWARD("Fast-forward"), //
        ALREADY_UP_TO_DATE("Already up-to-date"), //
        FAILED("Failed"), //
        MERGED("Merged"), //
        CONFLICTING("Conflicting"), //
        STOPPED("Stopped"), //
        UNCOMMITTED_CHANGES("Uncommitted Changes"), //
        NOT_SUPPORTED("Not-yet-supported");

        private final String value;

        private RebaseStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /** @return status of merge */
    RebaseStatus getRebaseStatus();

    /** @return files that has conflicts. May return <code>null</code> or empty array if there is no conflicts */
    List<String> getConflicts();

    /** @return files that failed to merge (not files that has conflicts). */
    List<String> getFailed();
}
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
package org.eclipse.che.ide.ext.git.server.jgit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.che.ide.ext.git.shared.RebaseResult;

/**
 * The JGit implementation of RebaseResult.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public class JGitRebaseResult implements RebaseResult {

    private final org.eclipse.jgit.api.RebaseResult _jgitResult;
    private List<String> _failed;
    private List<String> _conflicts;

    public JGitRebaseResult(org.eclipse.jgit.api.RebaseResult jgitResult) {
        _jgitResult = jgitResult;
    }

    @Override
    public RebaseStatus getRebaseStatus() {
        switch (_jgitResult.getStatus()) {
        case ABORTED:
            return RebaseStatus.ABORTED;
        case CONFLICTS:
            return RebaseStatus.CONFLICTING;
        case UP_TO_DATE:
            // return RebaseStatus.ALREADY_UP_TO_DATE;
        case FAST_FORWARD:
            // return RebaseStatus.FAST_FORWARD;
        case NOTHING_TO_COMMIT:
        case OK:
            return RebaseStatus.OK;
        case STOPPED:
            return RebaseStatus.STOPPED;
        case UNCOMMITTED_CHANGES:
            return RebaseStatus.UNCOMMITTED_CHANGES;
        default:
            return RebaseStatus.FAILED;
        }
    }

    @Override
    public List<String> getConflicts() {
        if (_conflicts != null) {
            return _conflicts;
        }
        return _conflicts = copyList(_jgitResult.getConflicts());
    }

    @Override
    public List<String> getFailed() {
        if (_failed != null) {
            return _failed;
        }
        return _failed = copyList(_jgitResult.getFailingPaths() != null ? _jgitResult.getFailingPaths().keySet() : null);
    }

    private static <T> List<T> copyList(Collection<T> coll) {
        return coll == null ? Collections.<T> emptyList() : new ArrayList<T>(coll);
    }
}

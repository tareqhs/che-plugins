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
import java.util.Collections;
import java.util.List;

import org.eclipse.che.ide.ext.git.shared.CherryPickResult;

/**
 * Result of a cherry-pick operation, based on a JGit result.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public class JGitCherryPickResult implements CherryPickResult {

    private final org.eclipse.jgit.api.CherryPickResult _jgitResult;
    private List<String> _failed;

    public JGitCherryPickResult(org.eclipse.jgit.api.CherryPickResult jgitResult) {
        _jgitResult = jgitResult;
    }

    @Override
    public CherryPickStatus getStatus() {
        switch (_jgitResult.getStatus()) {
        case CONFLICTING:
            return CherryPickStatus.CONFLICTS;
        case FAILED:
            return CherryPickStatus.FAILED;
        case OK:
            return CherryPickStatus.OK;

        }
        return null;
    }

    @Override
    public List<String> getFailed() {
        if (_failed != null) {
            return _failed;
        }
        if (_jgitResult.getFailingPaths() == null) {
            return _failed = Collections.<String> emptyList();
        }
        return _failed = new ArrayList<String>(_jgitResult.getFailingPaths().keySet());
    }
}
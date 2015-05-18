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

import java.util.Arrays;

import org.eclipse.che.ide.ext.git.server.GitException;
import org.eclipse.che.ide.ext.git.shared.RevertRequest;
import org.testng.annotations.Test;

/**
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public class RevertTest extends JGitBaseTest {

    @Test
    public void testEmptyRevert() throws Exception {
        // Commit two files
        commitFile("file_1.txt", "the content 1");
        // Revert the last two commits
        RevertRequest req = newDTO(RevertRequest.class);
        // Issue it
        getConnection().revert(req);

    }

    @Test
    public void testRevertSeveralCommits() throws Exception {
        // Commit two files
        commitFile("file_1.txt", "the content 1");
        commitFile("file_2.txt", "the content 1");
        commitFile("file_3.txt", "the content 1");
        // Revert the last two commits
        RevertRequest req = newDTO(RevertRequest.class).withRefSpec(Arrays.asList("HEAD", "HEAD~2"));
        // Issue it
        getConnection().revert(req);

    }

    @Test(expectedExceptions = GitException.class)
    public void testBadRevert() throws Exception {
        commitFile("file_1.txt", "the content 1");
        RevertRequest req = newDTO(RevertRequest.class).withRefSpec(Arrays.asList("HEAD~10"));
        getConnection().revert(req);

    }
}

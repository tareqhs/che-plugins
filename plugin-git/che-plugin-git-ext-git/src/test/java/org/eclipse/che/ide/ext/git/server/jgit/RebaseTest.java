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

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.che.ide.ext.git.server.GitConnection;
import org.eclipse.che.ide.ext.git.shared.RebaseRequest;
import org.eclipse.che.ide.ext.git.shared.RebaseResult;
import org.eclipse.che.ide.ext.git.shared.RebaseResult.RebaseStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public class RebaseTest extends JGitBaseTest {

    private Git _trackingGit;
    private GitConnection _testConnection;

    private static final String ADDITIONAL_FILE_NAME = "READYOU.txt";

    @BeforeMethod
    protected void cloneRepo() throws Exception {
        // Create the tracking repository
        File trackingRepoDir = new File(getTarget().toFile(), "rebaseTrackerRepo");
        // Initialize a tracking repository
        _trackingGit = Git.cloneRepository().setURI(getGit().getRepository().getDirectory().getAbsolutePath())
                .setDirectory(trackingRepoDir).call();
        forClean.add(trackingRepoDir);
        // Add a new file to the original repository
        addFile(ADDITIONAL_FILE_NAME, "content of another file!");
        getGit().add().addFilepattern(ADDITIONAL_FILE_NAME).call();
        getGit().commit().setMessage("adding file2.txt").call();
        // Fetch the changes to the tracking repository to make it ready for this test
        _trackingGit.fetch().call();
        // Test object
        _testConnection = createConnection(trackingRepoDir);
    }

    @AfterMethod
    void releaseStuff() {
        _trackingGit.close();
    }

    private void validateTrackingHead() throws Exception {
        ObjectId trackedHead = getGit().getRepository().resolve(Constants.HEAD);
        ObjectId trackingHead = _trackingGit.getRepository().resolve(Constants.HEAD);
        assertEquals(trackingHead, trackedHead);
    }

    @Test
    public void testSimpleRebase() throws Exception {
        // Rebase without any local changes
        RebaseRequest req = newDTO(RebaseRequest.class);
        // Validate result
        RebaseResult result = _testConnection.rebase(req);
        assertEquals(result.getRebaseStatus(), RebaseStatus.OK);
        assertEquals(result.getConflicts().size(), 0);
        assertEquals(result.getFailed().size(), 0);
        // Validate heads
        validateTrackingHead();
    }

    @Test
    public void testRebaseFailsOnConflicts() throws Exception {
        // Modify the main file that was added
        Path p = Paths.get(_trackingGit.getRepository().getWorkTree().getAbsolutePath());
        addFile(p, ADDITIONAL_FILE_NAME, "this is just a modification...");
        _trackingGit.add().addFilepattern(ADDITIONAL_FILE_NAME).call();
        // Rebase without any local changes
        RebaseRequest req = newDTO(RebaseRequest.class);
        // Validate result
        RebaseResult result = _testConnection.rebase(req);
        assertEquals(result.getRebaseStatus(), RebaseStatus.CONFLICTING);
        assertEquals(result.getConflicts(), Arrays.asList(ADDITIONAL_FILE_NAME));
        assertEquals(result.getFailed().size(), 0);
    }
}

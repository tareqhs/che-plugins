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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.che.ide.ext.git.server.GitException;
import org.eclipse.che.ide.ext.git.shared.CherryPickRequest;
import org.eclipse.che.ide.ext.git.shared.CherryPickResult;
import org.eclipse.che.ide.ext.git.shared.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public class CherryPickTest extends JGitBaseTest {

    private static final String TEST_BRANCH = "Branch1";

    @BeforeMethod
    protected void cloneRepo() throws Exception {
        // Create the test branch for later comparison
        getGit().branchCreate().setName(TEST_BRANCH).call();
    }

    @Test
    public void testSimpleCherryPick() throws Exception {
        // Commit a file into the main repository
        commitFile("file_1.txt", "the content 1");
        commitFile("file_2.txt", "the content 1");
        // Move to the test branch
        getGit().checkout().setName(TEST_BRANCH).call();
        // Cherry-pick the head into the test repository
        CherryPickRequest req = newDTO(CherryPickRequest.class).withRefSpec(Arrays.asList("master"));
        CherryPickResult result = getConnection().cherryPick(req);
        // Validate result
        assertEquals(result.getStatus(), CherryPickStatus.OK);
        assertEquals(result.getFailed(), Collections.<String> emptyList());
        // Validate the difference
        AbstractTreeIterator it1 = createRevTreeIterator("HEAD");
        AbstractTreeIterator it2 = createRevTreeIterator("master");
        List<DiffEntry> diffs = getGit().diff().setOldTree(it1).setNewTree(it2).call();
        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getChangeType(), ChangeType.ADD);
        assertEquals(diffs.get(0).getNewPath(), "file_1.txt");
    }

    @Test(expectedExceptions = GitException.class)
    public void testBadRefSpec() throws Exception {
        // Cherry-pick the head into the test repository
        CherryPickRequest req = newDTO(CherryPickRequest.class).withRefSpec(Arrays.asList("origin/badbranchname"));
        getConnection().cherryPick(req);
    }
}

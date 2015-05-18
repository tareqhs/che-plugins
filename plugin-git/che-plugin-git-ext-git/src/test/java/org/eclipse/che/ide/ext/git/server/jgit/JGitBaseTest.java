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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.che.ide.ext.git.server.nativegit.BaseTest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Base for all tests that run on JGit connection
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@Listeners(MockitoTestNGListener.class)
public class JGitBaseTest extends BaseTest {

    private Git _git;

    @BeforeMethod
    void initializeJGitTest() throws IOException {
        Path repoPath = getRepository();
        Repository repo = new FileRepository(new File(repoPath.toString(), Constants.DOT_GIT));
        _git = new Git(repo);
    }

    @AfterMethod
    void releaseResources() {
        _git.close();
    }

    protected final Git getGit() {
        return _git;
    }

    /**
     * Create a new file, write contents, add it to the index and commit the changes.
     */
    protected RevCommit commitFile(String fileName, String fileContent) throws IOException, GitAPIException {
        addFile(fileName, fileContent);
        getGit().add().addFilepattern(fileName).call();
        return getGit().commit().setMessage("Added file " + fileName).call();
    }

}

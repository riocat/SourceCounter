package com.yang.sourcecounter.command;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.Properties;

/**
 * Created by Administrator on 2018/10/10.
 */
public class GitCommandExecute {

    private static GitCommandExecute gitCommandExecute;

    private GitCommandExecute() {
    }

    public synchronized static GitCommandExecute getGitCommandExecute() {
        if (GitCommandExecute.gitCommandExecute == null) {
            GitCommandExecute.gitCommandExecute = new GitCommandExecute();
        }
        return GitCommandExecute.gitCommandExecute;
    }

    public void pullCode(String gitPath, Properties properties) throws Exception {
        Git git = null;
        PullResult pr = null;
        try {
            Repository existingRepo = new FileRepositoryBuilder().setGitDir(new File(gitPath + "\\.git")).build();
            git = new Git(existingRepo);


            String username = properties.getProperty("username");
            String password = properties.getProperty("password");

            if (username != null && "".equals(properties.getProperty("username")) &&
                    password != null && "".equals(properties.getProperty("password"))) {

                UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new
                        UsernamePasswordCredentialsProvider(username, password);

                pr = git.pull().setRemoteBranchName("master").setCredentialsProvider(usernamePasswordCredentialsProvider).call();
            } else {
                pr = git.pull().setRemoteBranchName("master").call();
            }

            if (!pr.isSuccessful()) {
                throw new RuntimeException(pr.toString());
            }

        } finally {
            if (git != null) {
                git.close();
            }
        }
    }
}

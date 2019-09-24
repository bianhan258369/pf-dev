package com.ECNU.util;

import com.ECNU.bean.LogBean;
import com.ECNU.bean.VersionInfo;
import net.sf.json.JSONObject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.ReflogEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * http://wiki.jikexueyuan.com/project/pro-git-two/jgit.html
 * https://blog.51cto.com/5162886/2094475
 *
 * https://download.eclipse.org/jgit/site/5.4.2.201908231537-r/apidocs/index.html
 *
 * @author PC
 */

@SuppressWarnings("all")
public class GitUtil {

    // git init
    public static Repository createRepository(String directoryName) throws IOException {
        // prepare a new folder(directory)
        File localPath = new File(directoryName);
        if (localPath.mkdirs()) {
            System.out.println(localPath + " 目录创建成功...");
        } else {
            System.out.println(localPath + " 目录创建失败...该目录已存在...");
        }
        // create the directory
        Repository repository =  FileRepositoryBuilder.create(new File(localPath,".git"));
        repository.create();
        return repository;
    }

    // get a repository
    public static Repository getRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
    }

    // get a repository
    public static Repository getRepository(String directoryName) throws IOException {
        directoryName = directoryName + "/.git";
        return getRepository(new File(directoryName));
    }

    // get a repository
    public static Repository getRepository(File directory) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.setGitDir(directory)
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir() // scan up the file system tree
                .build();
    }

    public static String currentBranch(String directoryName) throws IOException {
        return getRepository(directoryName).getBranch();
    }

    public static boolean createBranch(String branchName,String directoryName) throws IOException, GitAPIException {
        createBranch(branchName,directoryName,"master");
        return true;
    }

    public static boolean createBranch(String branchName,String directoryName,String startPoint) throws IOException, GitAPIException {
        try(Repository repository = getRepository(directoryName)) {
            try(Git git = new Git(repository)) {
                // run the add-call:add branch
                Ref ref = git.branchCreate().setForce(false).setName(branchName).setStartPoint(startPoint).call();
                System.out.println(ref.getName().replace("refs/heads/","") + " 分支创建成功...");
                return true;
            }
        }
    }

    public static boolean deleteBranch(String directoryName,String userName) throws IOException, GitAPIException {
        deleteBranches(directoryName,userName);
        return true;
    }

    public static boolean deleteBranches(String directoryName,String... userNames) throws IOException, GitAPIException {
        try(Repository repository = getRepository(directoryName)) {
            try(Git git = new Git(repository)) {
                // run the delete-call:delete branch
                List<String> branceList = git.branchDelete().setForce(true).setBranchNames(userNames).call();
                for(String ref: branceList) {
                    System.out.println(ref.replace("refs/heads/","") + " 分支删除成功...");
                }
                return true;
            }
        }
    }

    // judge branch if exists
    private static boolean branchNameExist(Git git, String branchName) throws GitAPIException {
        List<Ref> refs = git.branchList().call();

        for(Ref ref: refs) {
            if(ref.getName().endsWith(branchName)) {
                return true;
            }
        }
        return false;
    }

    // judge branch if exists
    public static boolean branchNameExist(String branchName,String directoryName) throws Exception {
        try (Repository repository = getRepository(directoryName)) {
            try (Git git = new Git(repository)) {
                if (branchNameExist(git,branchName)) {
                    return true;
                }else {
                    return false;
                }
            }
        }
    }

    // git checkout testuser1: 切换分支
    public static void gitCheckout(String branchName,String directoryName) throws Exception {
        try (Repository repository = getRepository(directoryName)) {
            try (Git git = new Git(repository)) {
                if(branchNameExist(git,branchName)) {
                    Ref ref = git.checkout().setCreateBranch(false).setName(branchName).call();
                    System.out.println("切换分支成功... " + "Result of checking out the branch: "
                            + ref.getName().replace("refs/heads/",""));
                } else {
                    throw new Exception("该分支 "+ branchName + " 不存在，需要创建(createBranch)...");
                }
            }
        }
    }

    // git branch -a:  list local Branches in a Git repository
    // 查看分支:
    public static Map<String,String> gitLocalBranch(String directoryName) throws IOException, GitAPIException {
        Map<String,String> refMap = null;
        try(Repository repository = getRepository(directoryName)) {
            System.out.println("Listing local branches:");
            try (Git git = new Git(repository)) {
                List<Ref> call = git.branchList().call();
                refMap = new HashMap<String,String>();
                for(Ref ref: call) {
                    refMap.put(ref.getName(),ref.getObjectId().getName());
                    System.out.println("Branch: " + ref.getName().replace("refs/heads/","") + " " + ref.getObjectId().getName());
                }
            }
        }
        return refMap;
    }

    // git branch -a:  list all Branches in a Git repository
    public static Map<String,String> gitAllBranch(String directoryName) throws IOException, GitAPIException {
        Map<String,String> refMap = null;
        try(Repository repository = getRepository(directoryName)) {
            System.out.println("Listing all branches(including remote branches):");
            try (Git git = new Git(repository)) {
                List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                refMap = new HashMap<String,String>();
                for(Ref ref: call) {
                    refMap.put(ref.getName(),ref.getObjectId().getName());
                    System.out.println("Branch: " + ref + " " + ref.getName().replace("refs/heads/","")
                            + " " + ref.getObjectId().getName());
                }
            }
        }
        return refMap;
    }

    // git add xxx.txt + git commit -am "Information"
    public static boolean RecordUploadProjAt(String commitMessage,String directoryName,String localFileOrDirectoryName) throws IOException, GitAPIException {
        try(Repository repository = getRepository(directoryName)) {
            try(Git git = new Git(repository)) {
                // Stage all files in the repo including new files
                // git add xxx.txt, addFilepattern(localFileOrDirectoryName): 将某个文件或某个目录下的所有文件add到git缓存区
                git.add().addFilepattern(localFileOrDirectoryName).call();
                // git commit -am "Information"
                git.commit().setMessage(commitMessage).call();
                System.out.println("uploaded Successfully..." + "文件存储在: " + localFileOrDirectoryName);
                return true;
            }
        }
    }

    // git add xxx.txt + git commit -am "Information"
    // commit author,committer
    public static boolean RecordUploadProjAt(String commitMessage,String directoryName,String localFileOrDirectoryName,String authorName,String authorEmail,
                                             String committerName,String committerEmail)
            throws IOException, GitAPIException {
        try(Repository repository = getRepository(directoryName)) {
            try(Git git = new Git(repository)) {
                // Stage all files in the repo including new files
                // git add xxx.txt, addFilepattern(localFileOrDirectoryName): 将某个文件或某个目录下的所有文件add到git缓存区
                git.add().addFilepattern(localFileOrDirectoryName).call();
                // git commit -am "Information"
                git.commit().setAuthor(authorName,authorEmail).setCommitter(committerName,committerEmail).setMessage(commitMessage).call();
                System.out.println("uploaded Successfully..." + "文件存储在: " + localFileOrDirectoryName);
                return true;
            }
        }
    }

    // git log: a specified directory in a branch: rollBackDirectoryName路径分割符必须为'/'
    private static List<LogBean> gitLog(String branchName, String directoryName, String rollBackDirectoryName) throws IOException, GitAPIException {
        try (Repository repository = getRepository(directoryName)) {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> logs = git.log().add(repository.resolve(branchName)).addPath(rollBackDirectoryName).call();
                int count = 0;

                List<LogBean> logBeans = new ArrayList<LogBean>();
                for(RevCommit rev:logs) {
                    LogBean logBean = new LogBean(
                            rev.getAuthorIdent(),rev.getCommitterIdent(),
                            rev.getName(),rev.getId(),rev.getCommitTime(),rev.getFullMessage(),
                            rev.getShortMessage()
                    );
                    logBeans.add(logBean);
                    count++;
                }
                System.out.println(branchName + " @ " +rollBackDirectoryName + " changed " + count + " times");
                return logBeans;
            }
        }
    }

    // git log: a specified directory in a branch: rollBackDirectoryName路径分割符必须为'/',加一个参数NewestID
    public static List<LogBean> gitLog(String branchName,String directoryName,String rollBackDirectoryName,ObjectId NewestID) throws IOException, RevisionSyntaxException, NoHeadException, GitAPIException {
        try (Repository repository = getRepository(directoryName)) {
            try (Git git = new Git(repository)) {
                Iterable<RevCommit> logs = git.log().add(repository.resolve(branchName)).add(NewestID).addPath(rollBackDirectoryName).call();
                int count = 0;

                List<LogBean> logBeans = new ArrayList<LogBean>();
                for(RevCommit rev:logs) {
                    LogBean logBean = new LogBean(
                            rev.getAuthorIdent(),rev.getCommitterIdent(),
                            rev.getName(),rev.getId(),rev.getCommitTime(),rev.getFullMessage(),
                            rev.getShortMessage()
                    );
                    logBeans.add(logBean);
                    count++;
                }
                System.out.println(branchName + " @ " +rollBackDirectoryName + "NewestID changed " + count + " times");
                return logBeans;
            }
        }
    }

    // git merge: user merge to master
    public static boolean gitMergeToMaster(String branchName,String directoryName,String mergeMessage) throws Exception {
        try(Repository repository = getRepository(directoryName)) {
            try(Git git = new Git(repository)) {
                // check out "master"
                gitCheckout("master",directoryName);
                // retrieve the objectId of the latest commit on branch
                ObjectId mergeBase = repository.resolve(branchName);
                // perform the actual merge, here we disable FastForward to see the
                // actual merge-commit even though the merge is trivial
                MergeResult merge = git.merge().include(mergeBase).setCommit(true)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        //.setSquash(false)
                        .setMessage(mergeMessage)
                        .call();
                System.out.println("Merge-Results for id: " + mergeBase + ": " + merge);
                return true;
            }
        }
    }

    // git merge: user dispatch to branch
    public static boolean gitDispatchToBranch(String branchName,String directoryName,String dispatchMessage) throws Exception {
        try(Repository repository = getRepository(directoryName)) {
            try(Git git = new Git(repository)) {
                // check out branchName
                gitCheckout(branchName,directoryName);
                // retrieve the objectId of the latest commit on master
                ObjectId mergeBase = repository.resolve("master");
                // perform the actual merge, here we disable FastForward to see the
                // actual merge-commit even though the merge is trivial
                MergeResult merge = git.merge().include(mergeBase).setCommit(true)
                        .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                        //.setSquash(false)
                        .setMessage(dispatchMessage)
                        .call();
                System.out.println("Merge-Results for id: " + mergeBase + ": " + merge);
                return true;
            }
        }
    }

    // rollback to a specified version
    public static void rollback(String branchName, String directoryName, String rollbackDate, List<VersionInfo> versions) throws IOException, GitAPIException {
        try (Repository repository = getRepository(directoryName)){
            try (Git git = new Git(repository)) {
                if(versions != null) {
                    for(VersionInfo version: versions) {
                        if(version.getTime().equals(rollbackDate)) {
                            Ref ref = git.reset().setMode(ResetType.HARD).
                                    setRef(version.getVersionId()).call();
//                				String command = "git reflog " + project + "/";
//                				String dir = "G:\\PF\\Project";
//                				File check = new File(dir);
//                				String newVersion = null;
//                				try {
//                					Process p1 = Runtime.getRuntime().exec(command,null,check);
//                					BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
//                					String s;
//
//                			        while ((s = br.readLine()) != null) {
//                			        	if(s.indexOf("commit") != -1) {
//                			        		newVersion = s.substring(0, 7);
//                			        		break;
//                			        	}
//                			        	System.out.println(s);
//                			        }
//                				} catch (IOException e) {
//                					// TODO Auto-generated catch block
//                					e.printStackTrace();
//                				}
                            System.out.println("rollBack Successfully...");
                        }
                    }
//            			for(String time : versions.keySet()) {
//            				if(time.equals(rollbackDate)) {
//            					String value = versions.get(time);
//            					Ref ref = git.reset().setMode(ResetType.HARD).
//                        				setRef(value).call();
//                            System.out.println("rollBack Successfully...");
//            				}
//            			}
                }
                return;
            }
        }
    }

    // rollback to a specified version
    public static ObjectId rollback(String branchName, String directoryName, String rollBackDirectoryName, String rollbackDate) throws IOException, GitAPIException {
        try (Repository repository = getRepository(directoryName)){
            try (Git git = new Git(repository)) {
                List<LogBean> logs = gitLog(branchName,directoryName,rollBackDirectoryName);
                for(LogBean log: logs) {
                    if(log.getAuthorTime().equals(rollbackDate)) {
//                            RevCommit revCommit = git.revert().include(log.getId()).call();
                        Ref ref = git.reset().setMode(ResetType.HARD).
                                setRef(log.getId().getName()).call();
                        System.out.println("rollBack Successfully...");
                    }
                }
                return logs.get(0).getId();
            }
        }
    }


    // Map<Datetime,message>: list all versions in a specified directory,, upload()
    public static Map<String,String> listDirVersions(String branchName,String directoryName,String rollBackDirectoryName) throws IOException, GitAPIException {
        List<LogBean> logs = gitLog(branchName,directoryName,rollBackDirectoryName);
        Map<String,String> versions = new HashMap<String,String>();
        System.out.println("logs:" + logs.size());
        for(LogBean log:logs) {
            System.out.println(log.getAuthorTime() + ":" + log.getShortMessage() + " listDirVersion");
            versions.put(log.getAuthorTime(), log.getShortMessage());
        }
        return versions;
    }


    // Map<Datetime,message>: list all versions in a specified directory,, rollBack()
    public static Map<String,String> listDirVersions(String branchName,String directoryName,String rollBackDirectoryName, ObjectId NewestID) throws IOException, GitAPIException {
        List<LogBean> logs = gitLog(branchName,directoryName,rollBackDirectoryName,NewestID);
        Map<String,String> versions = new HashMap<String,String>();
        for(LogBean log:logs) {
            System.out.println(log.getAuthorTime() + ":" + log.getShortMessage() + " listDirVersion");
            versions.put(log.getAuthorTime(), log.getShortMessage());
        }
        return versions;
    }

    public static void main(String[] args) throws Exception {
        // 创建git目录
        String ROOTADDRESS = "E:\\JavaProject\\pfdev\\GitRepository";
        JSONObject result = new JSONObject();
        Map map = GitUtil.gitAllBranch(ROOTADDRESS);
        Iterator it = map.keySet().iterator();
        while(it.hasNext()){
            String branch = it.next().toString();
            branch = branch.substring(branch.lastIndexOf("/") + 1);
            result.accumulate("filelist",branch);
        }
        System.out.println(result.toString());

        // 获得当前的分支名
//        System.out.println(currentBranch("E:/GitTest"));
//
//        RecordUploadProjAt("upload","E:/GitTest","E:/GitTest/a/");
//        RecordUploadProjAt("master","D:/GitTest","D:/GitTest/a/hello.txt");
        // 创建分支
//        createBranch("testuser01","E:/GitTest");
        // 查看某一目录下所有分支
 //         Map<String,String> refs = gitLocalBranch("E:/GitTest");
        // 切换分支
//        gitCheckout("testuser01","E:/GitTest");
//        System.out.println(currentBranch("G:/GitTest"));
        // commit testuser01分支
//        System.out.println(currentBranch("G:/GitTest"));
//        RecordUploadProjAt("testuser01Msg","D:/GitTest","D:/GitTest/a/");
//        RecordUploadProjAt("master","D:/GitTest","D:/GitTest/a/hello.txt");

//        createBranch("testuser02","D:/GitTest");
//        gitCheckout("testuser02","D:/GitTest");
//        System.out.println(currentBranch("D:/GitTest"));
        //deleteBranch("testuser02","D:/GitTest");

        // 合并分支到master主干
//        System.out.println(currentBranch("D:/GitTest"));
//        gitMergeToMaster("testuser02","D:/GitTest","mergeMsgTestuser02");

        // 主干分发到testuser02分支
//        System.out.println(currentBranch("D:/GitTest"));
//        gitDispatchToBranch("testuser02","D:/GitTest","dispatchMsgTestuser02");

        // 主干分发到testuser01分支
//        System.out.println(currentBranch("D:/GitTest"));
//        gitDispatchToBranch("testuser01","D:/GitTest","dispatchMsgTestuser01");

        // 查看主干testuser01更改的log
//        List<LogBean> logBeans = gitLog("testuser01","D:/GitTest","");
//        for(LogBean log: logBeans) {
//            System.out.println(log.getBranchNameSHA1());
//            System.out.println(log.getId());
//            System.out.println(log.getAuthorIdent());
//            System.out.println(log.getAuthorTime());
//            System.out.println(log.getAuthorEmail());
//            System.out.println(log.getCommitterIdent());
//            System.out.println(log.getCommitterTime());
//            System.out.println(log.getCommitterEmail());
//            System.out.println(log.getCommitTime());
//            System.out.println(log.getFullMessage());
//            System.out.println(log.getShortMessage()); // "dispatchMsgTestuser01"
//        }
        // 查看testuser01 a文件夹下所有版本号
//        Map<String,String> versions = listDirVersions("testuser01","G:/GitTest","a");
//        Map<String,String> versions = listDirVersions("test","G:/PF/Project","test1-pdv1");
////        for(String time:versions.keySet()) {
////            // 2019-09-02 18:27:36:add d.txt
////            System.out.println(time + ":" + versions.get(time));
////        }

        // 回滚testuser01 a文件夹下某一个时间的版本:
//        rollback("testuser01","D:/GitTest","a","2019-09-02 18:27:36");

    }
}

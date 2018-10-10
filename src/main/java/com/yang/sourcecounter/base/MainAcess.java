package com.yang.sourcecounter.base;

import com.yang.sourcecounter.command.CommandExecute;
import com.yang.sourcecounter.command.GitCommandExecute;
import com.yang.sourcecounter.command.WindowsCmdExecute;
import org.eclipse.jgit.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by Administrator on 2018/10/10.
 */
public class MainAcess {

    public static final String MAIN_PROPERTIES = "main.properties";

    public static void main(String[] args) throws Exception {

        // 获取所有配置
        Properties properties = new Properties();
        URL propertiesUrl = MainAcess.class.getClassLoader().getResource(MainAcess.MAIN_PROPERTIES);
        InputStream ins = new FileInputStream(propertiesUrl.getFile());
        try {
            properties.load(ins);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 获取cloc路径
        URL classUrl = Thread.currentThread().getContextClassLoader().getResource("");
            String clocBasePath = classUrl.getPath();

        // 读取配置获取git目录更新所有git项目
        String gitBasePath = properties.getProperty("gitBasePath");
        List<String> gitDirectorys = new ArrayList<String>();
        File gitBaseDirectory = new File(gitBasePath);
        List<File> gitFiles = Arrays.asList(gitBaseDirectory.listFiles());
        for (File file : gitFiles) {
            gitDirectorys.add(file.getName());
        }

        GitCommandExecute gitCommandExecute = GitCommandExecute.getGitCommandExecute();

        // 清空clocTxt文件夹
        File clocTxt = new File(clocBasePath + "clocTxt");
        if (clocTxt.exists()) {
            FileUtils.delete(clocTxt);
        }
        clocTxt.mkdirs();

        // 根据配置获取和操作系统相对应的命令执行类
        String systemtype = properties.getProperty("systemtype");

        CommandExecute commandExecute = null;
        if ("windows".equals(systemtype)) {
            commandExecute = WindowsCmdExecute.getWindowsCmdExecute();
        }

        for (String gitDirectory : gitDirectorys) {
            String gitPath = gitBasePath + "/" + gitDirectory;
            gitCommandExecute.pullCode(gitPath, properties);

            // 使用cloc 统计个项目的代码量 并在temp文件夹中生成统计txt

            List<String> paramterStrs = new ArrayList<String>();
            paramterStrs.add(clocBasePath);
            paramterStrs.add(gitPath);
            paramterStrs.add(gitDirectory);

            commandExecute.execute(systemtype, paramterStrs);
        }

        // 解析cloc生成的文件


        // 生成报告excle

        // 将报告发送给用户
    }
}

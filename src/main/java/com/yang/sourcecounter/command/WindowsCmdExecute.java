package com.yang.sourcecounter.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by Administrator on 2018/10/10.
 */
public class WindowsCmdExecute implements CommandExecute {

    private static WindowsCmdExecute windowsCmdExecute;

    private WindowsCmdExecute() {
    }

    public synchronized static WindowsCmdExecute getWindowsCmdExecute() {
        if (WindowsCmdExecute.windowsCmdExecute == null) {
            WindowsCmdExecute.windowsCmdExecute = new WindowsCmdExecute();
        }
        return WindowsCmdExecute.windowsCmdExecute;
    }

    public String execute(String commandStr) throws Exception {

        BufferedReader br = null;

        System.out.println(commandStr);

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cmd /c " + commandStr);
            br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder build = new StringBuilder();
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                build.append(line);
            }
            System.out.println(build.toString());

            return build.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String execute(List<String> paramterStrs) throws IOException {

        // cloc.exe所在文件夹 （windows）
        String clocBasePath = paramterStrs.get(0);

        StringBuffer stringBuffer = new StringBuffer();

        if ('/' == (clocBasePath.charAt(0))) {
            clocBasePath = clocBasePath.substring(1);
        }

        // 拼接命令字符串  paramterStrs.get(1)为每个项目的git文件夹 paramterStrs.get(2)每个项目文件夹的名称
        stringBuffer.append(clocBasePath).append("cloc-1.80.exe ").append(paramterStrs.get(1)).append(" > ").append(clocBasePath).append("clocTxt/").append(paramterStrs.get(2)).append(".txt");

        String commandStr = stringBuffer.toString();

        commandStr = commandStr.replace('/','\\');

        BufferedReader br = null;

        System.out.println(commandStr);

        // 在命令行下执行cloc 并获取输出
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cmd /c " + commandStr);
            br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder build = new StringBuilder();
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                build.append(line);
            }
            System.out.println(build.toString());

            return build.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

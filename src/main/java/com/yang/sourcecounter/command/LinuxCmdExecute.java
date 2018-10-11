package com.yang.sourcecounter.command;

import java.io.*;
import java.util.List;

/**
 * Created by rio on 2018/10/11.
 */
public class LinuxCmdExecute implements CommandExecute {

    private static LinuxCmdExecute linuxCmdExecute;

    private LinuxCmdExecute() {
    }

    public synchronized static LinuxCmdExecute getLinuxCmdExecute() {
        if (LinuxCmdExecute.linuxCmdExecute == null) {
            LinuxCmdExecute.linuxCmdExecute = new LinuxCmdExecute();
        }
        return LinuxCmdExecute.linuxCmdExecute;
    }

    @Override
    public String execute(String commandStr) throws Exception {
        return null;
    }

    @Override
    public String execute(List<String> paramterStrs) throws Exception {
        StringBuffer errorInfo = new StringBuffer();
        Process process = null;
        BufferedReader bufReader = null;
        BufferedReader bufError = null;
        BufferedWriter bufWriter = null;

        try {
            // 实际为classPath的实际路径
            String clocBasePath = paramterStrs.get(0);
            // 每个项目的git文件夹
            String gitPath = paramterStrs.get(1);
            // 每个项目文件夹的名称
            String gitDirectory = paramterStrs.get(2);

            process = Runtime.getRuntime().exec("cloc " + gitPath);

            process.waitFor();

            bufReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

            bufError = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));

            bufWriter = new BufferedWriter(new FileWriter(clocBasePath + "clocTxt/" + gitDirectory + ".txt"));

            String line = null;

            while ((line = bufReader.readLine()) != null) {
                bufWriter.write(line);
            }

            while ((line = bufError.readLine()) != null) {
                errorInfo.append(line).append('\n');
            }

            if (errorInfo.length() > 0)
                throw new RuntimeException(errorInfo.toString());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {

            if(process!=null){
                process.destroy();
            }

            if(bufReader!=null){
                bufReader.close();
            }

            if(bufError!=null){
                bufError.close();
            }

            if(bufWriter!=null){
                bufWriter.close();
            }
        }

        return null;
    }
}

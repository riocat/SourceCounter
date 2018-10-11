package com.yang.sourcecounter.base;

import com.yang.sourcecounter.command.CommandExecute;
import com.yang.sourcecounter.command.GitCommandExecute;
import com.yang.sourcecounter.command.WindowsCmdExecute;
import com.yang.sourcecounter.entity.ProjectSourceAmount;
import com.yang.sourcecounter.entity.SourceAmountEntity;
import com.yang.sourcecounter.util.EmailSend;
import com.yang.sourcecounter.util.ExcelGeneraor;
import com.yang.sourcecounter.util.TxtAnaylsis;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.LayoutMode;

import java.io.*;
import java.net.URL;
import java.util.*;

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
            properties.load(new InputStreamReader(ins, "UTF-8"));
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
            FileUtils.deleteDirectory(clocTxt);
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
        List<ProjectSourceAmount> projectSourceAmounts = TxtAnaylsis.getExcelDataFromTXT(clocTxt.getPath());

        // 只保留每个项目的总和数据
        String sumonly = properties.getProperty("sumonly");
        if (sumonly != null && "true".equals(sumonly)) {
            for (ProjectSourceAmount projectSourceAmount : projectSourceAmounts) {
                List<SourceAmountEntity> sourceAmountEntityList = projectSourceAmount.getSourceAmountEntityList();

/*                List<SourceAmountEntity> targetList = sourceAmountEntityList.stream().filter((sourceAmountEntity) -> {
                    return "SUM".equals(sourceAmountEntity.getLanguage());
                });*/

                List<SourceAmountEntity> targetList = new ArrayList<SourceAmountEntity>();
                for (SourceAmountEntity sourceAmountEntity : sourceAmountEntityList) {
                    if ("SUM".equals(sourceAmountEntity.getLanguage())) {
                        targetList.add(sourceAmountEntity);
                        break;
                    }
                }
                projectSourceAmount.setSourceAmountEntityList(targetList);
            }
        }

        // 生成报告excle
        // 清空excle报告文件夹
        File reportDirectory = new File(clocBasePath + "report");
        if (reportDirectory.exists()) {
            FileUtils.deleteDirectory(reportDirectory);
        }
        reportDirectory.mkdirs();
        ExcelGeneraor excelGeneraor = ExcelGeneraor.getExcelGeneraor();
        String reportPath = excelGeneraor.createExcel(projectSourceAmounts, reportDirectory, properties, classUrl);

        // 将报告发送给用户
        EmailSend.getEmailSend().sendReportEmail(reportPath, properties);
    }
}

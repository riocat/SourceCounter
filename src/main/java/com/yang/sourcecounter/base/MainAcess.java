package com.yang.sourcecounter.base;

import com.yang.sourcecounter.command.CommandExecute;
import com.yang.sourcecounter.command.GitCommandExecute;
import com.yang.sourcecounter.command.WindowsCmdExecute;
import com.yang.sourcecounter.entity.ProjectSourceAmount;
import com.yang.sourcecounter.fileanaylsis.TxtAnaylsis;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Workbook;

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

        // 生成报告excle
        createExcel(projectSourceAmounts);

        // 将报告发送给用户
    }

    public static void createExcel(List<ProjectSourceAmount> projectSourceAmounts) throws Exception {
        String tempBasePath = "/home/emplat/temp";
        tempBasePath.replaceAll("\\/", "\\" + File.separator);
        File basePath = new File(tempBasePath);
        if (!basePath.exists()) {
            basePath.mkdirs();
        }
        String tempPath = tempBasePath + File.separator + new Date().getTime() + ".xlsx";
        XLSTransformer transformer = new XLSTransformer();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("contractLedgers", "");
        InputStream is = MainAcess.class.getResourceAsStream("/template/ContractLedgerTemplate.xlsx");
        Workbook workbook = transformer.transformXLS(is, map);
        OutputStream tempOS = new FileOutputStream(tempPath);
        workbook.write(tempOS);
        tempOS.flush();
        tempOS.close();
    }

    /*private String getExcel(String id, HttpServletRequest request) throws Exception {
        String path = request.getSession().getServletContext().getRealPath("") + "/uploads/";

        path.replaceAll("\\/", "\\" + File.separator);
        File tmepBasePath = new File(path);
        if (!tmepBasePath.exists()) {
            tmepBasePath.mkdirs();
        }

        CapitalJournal capitalJournal = capitalJournalService.selectByPrimaryKey(id);
        String basePath = "/Template/CapitalJournalTemplate/template/";
        String templateFile = "";
        String journalDateString = "";
        String sheetName = "";
        SimpleDateFormat sdfOne = new SimpleDateFormat("yyyy年 MM月 dd日");
        SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy.MM.dd");
        Map<String, Object> map = new HashMap<String, Object>();
        switch (capitalJournal.getFkCapitalJournalType()) {
            case 1:
                map.put("ascriptionType", "博智集团");
                map.put("datas", capitalJournalService.getGroupReportDatas(capitalJournal));
                templateFile = basePath + "jituantemplate.xls";
                journalDateString = sdfTwo.format(capitalJournal.getJournalDate());
                sheetName = "集团";
                break;
            case 2:
                String ascriptionTypeName = "";
                switch (capitalJournal.getAscriptionType()) {
                    case 2:
                        ascriptionTypeName = "博智置业";
                        map.put("ascription", "天津博智置业发展有限公司");
                        sheetName = "博智";
                        break;
                    case 9:
                        ascriptionTypeName = "理想置地";
                        map.put("ascription", "天津理想置地有限公司");
                        sheetName = "理想";
                        break;
                    case 10:
                        ascriptionTypeName = "天津博智南郡投资有限公司";
                        map.put("ascription", "天津博智南郡投资有限公司");
                        sheetName = "南郡";
                        break;
                    case 11:
                        ascriptionTypeName = "天津博智陈塘商务信息咨询有限公司";
                        map.put("ascription", "天津博智陈塘商务信息咨询有限公司");
                        sheetName = "信息";
                        break;
                    case 12:
                        ascriptionTypeName = "天津亿隆博远企业管理咨询有限公司";
                        map.put("ascription", "天津亿隆博远企业管理咨询有限公司");
                        sheetName = "亿龙博远";
                        break;
                    case 13:
                        ascriptionTypeName = "境外公司";
                        map.put("ascription", "境外公司");
                        sheetName = "境外公司";
                        break;
                }
                map.put("ascriptionType", ascriptionTypeName);
                map.put("datas", capitalJournalService.getReportDatas(capitalJournal));
                templateFile = basePath + "zhiyetemplate.xls";
                journalDateString = sdfOne.format(capitalJournal.getJournalDate());
                break;
            case 3:
                map.put("ascriptionType", "天兴投资（宝坻）");
                map.put("ascription", "天津天兴投资发展有限公司（宝坻）");
                map.put("datas", capitalJournalService.getReportDatas(capitalJournal));
                templateFile = basePath + "tianxingtemplate.xls";
                journalDateString = sdfOne.format(capitalJournal.getJournalDate());
                sheetName = "天兴";
                break;
            case 4:
                map.put("ascriptionType", "蓝山投资");
                map.put("ascription", "天津南郡蓝山投资有限公司");
                map.put("datas", capitalJournalService.getReportDatas(capitalJournal));
                templateFile = basePath + "lanshantemplate.xls";
                journalDateString = sdfOne.format(capitalJournal.getJournalDate());
                sheetName = "蓝山";
                break;
            case 5:
                map.put("ascriptionType", "天津博智恒达商业管理有限公司");
                map.put("ascription", "天津博智恒达商业管理有限公司");
                map.put("datas", capitalJournalService.getReportDatas(capitalJournal));
                templateFile = basePath + "shangyetemplate.xls";
                journalDateString = sdfOne.format(capitalJournal.getJournalDate());
                sheetName = "商业";
                break;
            case 6:
                map.put("ascriptionType", "博智房地产总公司");
                map.put("ascription", "天津博智房地产经纪有限公司");
                map.put("datas", capitalJournalService.getHousingBrokerageReportDatas(capitalJournal));
                templateFile = basePath + "bozhifangdichantemplate.xls";
                journalDateString = sdfTwo.format(capitalJournal.getJournalDate());
                sheetName = "博智房地产";
                break;
            case 7:
                String ascriptionTypeName2 = "";
                switch (capitalJournal.getAscriptionType()) {
                    case 7:
                        ascriptionTypeName2 = "天津博智嘉颐物业服务有限公司宝坻分公司";
                        map.put("ascription", "天津博智嘉颐物业服务有限公司宝坻分公司");
                        sheetName = "物业";
                        break;
                    case 8:
                        ascriptionTypeName2 = "天津博智嘉颐物业服务有限公司";
                        map.put("ascription", "天津博智嘉颐物业服务有限公司");
                        sheetName = "物业总公司";
                        break;
                }
                map.put("ascriptionType", ascriptionTypeName2);
                map.put("datas", capitalJournalService.getReportDatas(capitalJournal));
                templateFile = basePath + "wuyetemplate.xls";
                journalDateString = sdfOne.format(capitalJournal.getJournalDate());
                break;
        }

        String fileName = new Date().getTime() + ".xls";
        String tempPath = path + File.separator + fileName;
        XLSTransformer transformer = new XLSTransformer();
        map.put("journalDateString", journalDateString);
        map.put("beginningAvailableFunds", capitalJournal.getBeginningAvailableFunds());
        InputStream is = this.getClass().getResourceAsStream(templateFile);
        Workbook workbook = transformer.transformXLS(is, map);
        workbook.setSheetName(0, sheetName);
        OutputStream tempOS = new FileOutputStream(tempPath);
        try {
            workbook.write(tempOS);
            tempOS.flush();
        } catch (Exception e) {
            new RuntimeException(e);
        } finally {
            tempOS.close();
        }
        return tempPath;
    }*/
}

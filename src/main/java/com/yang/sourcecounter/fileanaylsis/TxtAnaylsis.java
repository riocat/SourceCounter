package com.yang.sourcecounter.fileanaylsis;

import com.yang.sourcecounter.entity.ProjectSourceAmount;
import com.yang.sourcecounter.entity.SourceAmountEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/10/10.
 */
public class TxtAnaylsis {

    private Pattern pattern = Pattern.compile("\\b(.+)\\b(/d+)\\b(/d+)\\b(/d+)\\b(/d+)\\b");

    public List<ProjectSourceAmount> getExcelDataFromTXT(String clocTxt) throws Exception {
        List<ProjectSourceAmount> result = new ArrayList<ProjectSourceAmount>();

        File txtPath = new File(clocTxt);
        ProjectSourceAmount projectSourceAmount = new ProjectSourceAmount();
        projectSourceAmount.setPorjectName(clocTxt.substring(0,clocTxt.lastIndexOf(".")));
        List<File> projectTxts = Arrays.asList(txtPath.listFiles());

        for (File projectTxt : projectTxts) {
            boolean arriveCoreTxt = false;
            int lineCount = 0;
            List<String> needStrings = new ArrayList<String>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(projectTxt));
            String tempStr = null;
            while ((tempStr = bufferedReader.readLine()) != null) {
                if (tempStr.length() > 0 && '-' == tempStr.charAt(0)) {
                    lineCount++;
                    if (lineCount == 2) {
                        arriveCoreTxt = true;
                    }
                }

                if (arriveCoreTxt && '-' != tempStr.charAt(0)) {
                    Matcher matcher = pattern.matcher(tempStr);
                    if(matcher.find()){
                        SourceAmountEntity sourceAmountEntity = new SourceAmountEntity();
                        // Language
                        sourceAmountEntity.setLanguage(matcher.group(1));
                        // files
                        sourceAmountEntity.setFiles(Integer.parseInt(matcher.group(2)));
                        // blank
                        sourceAmountEntity.setBlank(Integer.parseInt(matcher.group(3)));
                        // comment
                        sourceAmountEntity.setComment(Integer.parseInt(matcher.group(4)));
                        // code
                        sourceAmountEntity.setCode(Integer.parseInt(matcher.group(5)));
                    }
                }
            }
        }

        return result;
    }
}

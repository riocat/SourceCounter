package com.yang.sourcecounter.command;

import java.util.List;

/**
 * Created by Administrator on 2018/10/10.
 */
public interface CommandExecute {
    public String execute(String commandStr) throws Exception;

    public String execute(String systemtype, List<String> paramterStrs) throws Exception;
}

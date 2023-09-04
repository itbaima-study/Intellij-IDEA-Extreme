package com.intellij.entity;

/**
 * 进程运行结果实体类
 */
public class ProcessResult {
    private final int exitCode;
    private final String output;

    public ProcessResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }
}

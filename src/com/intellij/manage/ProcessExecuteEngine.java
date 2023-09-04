package com.intellij.manage;

import com.intellij.entity.ProcessResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * 这是进程执行引擎，包括使用javac、javap、java命令实现对项目的：
 * - 编译源代码操作
 * - 反编译.class文件操作
 * - 执行程序操作
 * 所有跟项目相关的操作都使用此执行引擎完成。
 */
public class ProcessExecuteEngine {

    private enum OS { Windows, Linux, MacOS }

    /**
     * 执行命令主体
     * @param commands 多个命令
     * @return 最后一条命令启动的进程
     */
    private static Process runCommand(String... commands){
        try {
            Runtime runtime = Runtime.getRuntime();
            OS os = osType();   //不同操作系统执行的命令不同
            Process process = null;
            for (String command : commands) {
                if(os == OS.Linux || os == OS.MacOS) {
                    process = runtime.exec(new String[]{"/bin/bash", "-c", command});
                }else {
                    process = runtime.exec(command);
                }
            }
            return process;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static OS osType() {
        switch (System.getProperty("os.name")) {
            case "Mac OS X":
                return OS.MacOS;
            case "Linux":
                return OS.Linux;
            case "Windows 11":
            case "Windows 10":
            case "Windows 7":
            case "Windows 8":
            case "Windows 8.1":
                return OS.Windows;
            default:
                throw new IllegalStateException("未知的操作系统类型！");
        }
    }

    /**
     * 编译Java源代码，并将代码生成到out目录下
     * @param projectPath 项目根目录
     */
    public static ProcessResult buildProject(String projectPath){
        OS os = osType();
        Process process;
        if(os == OS.Linux || os == OS.MacOS) {
            process = runCommand("javac -s "+projectPath+" -d "+projectPath+"/out $(find '"+projectPath+"/src' -name '*.java')");
        } else {
            process = runCommand("cmd /C cd "+projectPath+" & dir *.java/s/b > "+projectPath+"/.list",
                    "javac -s "+projectPath+" -d "+projectPath+"/out @"+projectPath+"/.list");
        }
        if(process == null) return new ProcessResult(-1, "未知错误");
        try {
            int exitCode = process.waitFor();
            runCommand("cmd /C cd "+projectPath+" & del .list");
            return exitCode == 0 ? new ProcessResult(0, "")
                    : new ProcessResult(exitCode, streamToString(process.getErrorStream()));
        } catch (Exception e) {
            e.printStackTrace();
            return new ProcessResult(-1, "未知错误");
        }
    }

    /**
     * 运行项目
     * @param projectPath 项目根目录
     * @param mainClass 主类
     */
    public static ProcessResult startProcess(String projectPath, String javaCommand, String mainClass, Consumer<String> redirect){
        try {
            currentProcess = runCommand(javaCommand+" -cp " + projectPath + "/out " + mainClass);
            if(currentProcess == null) return new ProcessResult(-1, "未知错误");
            InputStreamReader reader = new InputStreamReader(currentProcess.getInputStream());
            char[] chars = new char[1024];
            int len;
            while ((len = reader.read(chars)) > 0)
                redirect.accept(new String(chars, 0, len));
            int code = currentProcess.waitFor();
            return new ProcessResult(code, streamToString(currentProcess.getErrorStream()));
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            synchronized (ProcessExecuteEngine.class) {    //多线程控制，防止并发修改
                currentProcess = null;
            }
        }
        return new ProcessResult(-1, "未知错误");
    }

    private static Process currentProcess = null;

    /**
     * 如果当前正在运行进程，停止当前正在运行的进程
     */
    public static void stopProcess(){
        synchronized (ProcessExecuteEngine.class) {   //多线程控制，防止并发修改
            if(currentProcess != null)
                currentProcess.destroyForcibly();
        }
    }

    /**
     * 反编译项目，并返回反编译结果
     * @param classFilePath .class文件路径
     * @return 反编译结果
     */
    public static String decompileCode(String classFilePath){
        Process process = runCommand("javap -c " + classFilePath);
        if(process == null) return "";
        return streamToString(process.getInputStream());
    }

    /**
     * 使用git命令从远程仓库下载代码
     * @param url 远程地址
     * @param branch 分支
     * @param dir 保存位置
     * @return 下载结果
     */
    public static ProcessResult fetchFromGit(String url, String branch, String dir){
        Process process = runCommand("git clone "+url+" -b "+branch + " " +dir);
        if(process == null) return new ProcessResult(-1, "未知错误");
        try {
            int exitCode = process.waitFor();
            return exitCode == 0 ? new ProcessResult(0, "")
                    : new ProcessResult(exitCode, streamToString(process.getErrorStream()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new ProcessResult(-1, "未知错误");
        }
    }

    /**
     * 将输入的字符串重定向给当前正在运行的进程
     * @param input 输入
     */
    public static void redirectToProcess(String input){
        synchronized (ProcessExecuteEngine.class) {   //多线程控制，防止并发修改
            if(currentProcess != null) {
                OutputStream stream = currentProcess.getOutputStream();
                try {
                    stream.write(input.getBytes());
                    stream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 快速将流中内容转换为字符串
     * @param stream 输入流
     * @return 字符串
     */
    private static String streamToString(InputStream stream){
        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        try {
            int len;
            char[] chars = new char[1024 * 1024];
            while ((len = reader.read(chars)) > 0)
                builder.append(chars, 0, len);
        }catch (IOException e){
            e.printStackTrace();
        }
        return builder.toString();
    }
}

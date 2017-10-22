package com.docker.utils;

import chat.main.ServerStart;
import com.docker.file.adapters.GridFSFileHandler;
import com.docker.server.OnlineServer;
import com.docker.storage.DBException;
import com.docker.storage.mongodb.MongoHelper;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import script.file.FileAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeployServiceUtils {
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options opt = new Options();
        opt.addOption("h", "help", false, "help")
                .addOption("p",true, "Service path")
//			.addOption("a",true, "async servlet map")
                .addOption("d",true, "Docker name")
                .addOption("s",true, "Service name")
                .addOption("f",true, "Mongodb GridFS host, or other dfs host")
                .addOption("v",true, "Version");

        org.apache.commons.cli.CommandLine line = parser.parse(opt, args);
        System.out.println("commandLine " + Arrays.toString(args));
        List<String> argList = line.getArgList();
        if (line.hasOption('h') || line.hasOption("help")) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        String servicePath = null;
        String dockerName = null;
        String serviceName = null;
        String gridfsHost = null;
        String versionStr = null;

        if(line.hasOption('p')){
            servicePath = line.getOptionValue('p');
        }else{
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if(line.hasOption('d')){
            dockerName = line.getOptionValue('d');
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if(line.hasOption('s')){
            serviceName = line.getOptionValue('s');
        } else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if(line.hasOption('f')){
            gridfsHost = line.getOptionValue('f');
        }else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        int version = 1;
        if(line.hasOption('v')){
            versionStr = line.getOptionValue('v');
            try {
                version = Integer.valueOf(versionStr);
            } catch(Exception e) {}
        }else {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }

        deploy(servicePath, dockerName, serviceName, gridfsHost, version);
    }

    public static void deploy(String servicePath, String dockerName, String serviceName, String gridfsHost, int version) throws Exception {
        final CommandLine cmdLine = CommandLine.parse("sh " + servicePath + "/build/build.sh " + dockerName + " " + serviceName + "_v" + version);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis(5));//设置超时时间
        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(baos, baos));
        executor.setWatchdog(watchdog);
        executor.setExitValue(0);//由于ping被到时间终止，所以其默认退出值已经不是0，而是1，所以要设置它
        int exitValue = executor.execute(cmdLine);
        final String result = baos.toString().trim();
        System.out.println("import log " + result);

        MongoHelper helper = new MongoHelper();
        helper.setHost(gridfsHost);
        helper.setDbName("gridfiles");
//		helper.setUsername("socialshopsim");
//		helper.setPassword("eDANviLHQtjwmFlywyKu");
        helper.init();
//		helper.setUsername();

        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        fileHandler.setBucketName("imfs");
        fileHandler.init();

        File directory = new File(servicePath + "/build/deploy");
//        File directory = new File("/home/aplomb/dev/github/DiscoveryService/deploy");
        Collection<File> files = FileUtils.listFiles(directory, new String[]{"zip"}, true);
        if(files != null) {
            for(File file : files) {
                String filePath = file.getAbsolutePath();
                String dirPath = directory.getAbsolutePath();
                String thePath = filePath.substring(dirPath.length());
//				System.out.println("file " + thePath);

                FileAdapter.PathEx path = new FileAdapter.PathEx(thePath);
                fileHandler.saveFile(FileUtils.openInputStream(new File(filePath)), path, FileAdapter.FileReplaceStrategy.REPLACE);

                System.out.println("File " + thePath + " saved!");
            }
        }
    }
}

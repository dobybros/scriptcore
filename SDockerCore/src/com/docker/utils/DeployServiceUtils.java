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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import script.file.FileAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DeployServiceUtils {
    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new PosixParser();
        Options opt = new Options();
        opt.addOption("h", "help", false, "help")
                .addOption("p",true, "Service path")
//			.addOption("a",true, "async servlet map")
                .addOption("l",true, "Dependency library path")
                .addOption("x",true, "Prefix name")
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
        String prefix = null;
        String servicePath = null;
        String dockerName = null;
        String serviceName = null;
        String gridfsHost = null;
        String versionStr = null;
        String libPath = null;

        if(line.hasOption('x')){
            prefix = line.getOptionValue('x');
        }
        if(line.hasOption('p')){
            servicePath = line.getOptionValue('p');
        }else{
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("DeployServiceUtils[options:]", opt, false);
            return;
        }
        if(line.hasOption('l')){
            libPath = line.getOptionValue('l');
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
        Integer version = null;
        if(line.hasOption('v')){
            versionStr = line.getOptionValue('v');
            try {
                version = Integer.valueOf(versionStr);
            } catch(Exception e) {}
        }

        deploy(prefix, servicePath, dockerName, serviceName, gridfsHost, version, libPath);
    }

    public static void deploy(String prefix, String servicePath, String dockerName, String serviceName, String gridfsHost, Integer version) throws Exception {
        deploy(prefix, servicePath, dockerName, serviceName, gridfsHost, version, null);
    }

    public static void deploy(String prefix, String servicePath, String dockerName, String serviceName, String gridfsHost, Integer version, String libPath) throws Exception {
        File deploy = new File(servicePath + "/build/deploy/classes");
        File root = new File(servicePath + "/build/deploy");
        FileUtils.deleteDirectory(root);
        //copy libs
        if(libPath != null) {
//            List<String> libPathList = new ArrayList<>();
            String[] libPaths = libPath.split(",");
            for(String libP : libPaths) {
                File libGroovyFile = new File(libP + "/src/main/groovy");
                if(libGroovyFile.isDirectory() && libGroovyFile.exists())
                    FileUtils.copyDirectory(libGroovyFile, deploy);
                File libResourceFile = new File(libP + "/src/main/resources");
                if(libResourceFile.exists() && libResourceFile.isDirectory())
                    FileUtils.copyDirectory(libResourceFile, deploy);
            }
        }

        //copy source
        File groovyFile = new File(servicePath + "/src/main/groovy");
        if(groovyFile.isDirectory() && groovyFile.exists()) {
            FileUtils.copyDirectory(groovyFile, deploy);
        }
        File resourceFile = new File(servicePath + "/src/main/resources");
        if(resourceFile.exists() && resourceFile.isDirectory()) {
            FileUtils.copyDirectory(resourceFile, deploy);
        }

        if(version != null)
            serviceName = serviceName + "_v" + version;
        doZip(new File(FilenameUtils.separatorsToUnix(root.getAbsolutePath()) + (prefix != null ? "/" + prefix : "") + "/" + dockerName + "/" + serviceName + "/groovy.zip"), deploy);
//        clean(deploy, ".zip");
        FileUtils.deleteQuietly(deploy);

        File[] toRemoveEmptyFolders = root.listFiles();
        for(File findEmptyFolder : toRemoveEmptyFolders) {
            if(getAllEmptyFoldersOfDir(findEmptyFolder)) {
                FileUtils.deleteDirectory(findEmptyFolder);
            }
        }
//        if(true)
//            return;

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
                String filePath = FilenameUtils.separatorsToUnix(file.getAbsolutePath());
                String dirPath = FilenameUtils.separatorsToUnix(directory.getAbsolutePath());
                String thePath = filePath.substring(dirPath.length());
//				System.out.println("file " + thePath);

                FileAdapter.PathEx path = new FileAdapter.PathEx(thePath);
                fileHandler.saveFile(FileUtils.openInputStream(new File(filePath)), path, FileAdapter.FileReplaceStrategy.REPLACE);

                System.out.println("File " + thePath + " saved!");
            }
        }
    }


    static boolean getAllEmptyFoldersOfDir(File current){
        if(current.isDirectory()){
            File[] files = current.listFiles();
            if(files.length == 0){ //There is no file in this folder - safe to delete
                System.out.println("Safe to delete - empty folder: " + FilenameUtils.separatorsToUnix(current.getAbsolutePath()));
                return true;
            } else {
                int totalFolderCount = 0;
                int emptyFolderCount = 0;
                for(File f : files){
                    if(f.isDirectory()){
                        totalFolderCount++;
                        if(getAllEmptyFoldersOfDir(f)){ //safe to delete
                            emptyFolderCount++;
                        }
                    }

                }
                if(totalFolderCount == files.length && emptyFolderCount == totalFolderCount){ //only if all folders are safe to delete then this folder is also safe to delete
                    System.out.println("Safe to delete - all subfolders are empty: " + FilenameUtils.separatorsToUnix(current.getAbsolutePath()));
                    return true;
                }
            }
        }
        return false;
    }

    public static void clean(File folder, String endWith) {
        Collection<File> fList = FileUtils.listFiles(folder, null, true);
        for (File file : fList) {
            if (file.isFile() && !file.getName().endsWith(endWith)) {
//                file.delete();
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        FileUtils.listFiles(folder, new FileFileFilter(){}, new DirectoryFileFilter(){
            public boolean accept(File file) {
                if(file.isDirectory()) {
                    Collection<File> hasFiles = FileUtils.listFiles(file, null, true);
                    if(hasFiles == null || hasFiles.isEmpty()) {
//                        file.delete();
                        try {
                            FileUtils.forceDelete(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });
    }

    //压缩文件夹内的文件
    public static void doZip(File zipFile, File zipDirectory){//zipDirectoryPath:需要压缩的文件夹名
        File file;
        File zipDir;

        zipDir = zipDirectory;

        try{
            ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(FileUtils.openOutputStream(zipFile)));
            handleDir(zipDir, zipDir, zipOut, zipFile);
            zipOut.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    //由doZip调用,递归完成目录文件读取
    private static void handleDir(File root, File dir , ZipOutputStream zipOut, File zipFile)throws IOException{
        FileInputStream fileIn;
        File[] files;

        files = dir.listFiles();

        if(files.length == 0){//如果目录为空,则单独创建之.
            //ZipEntry的isDirectory()方法中,目录以"/"结尾.
            zipOut.putNextEntry(new ZipEntry(dir.toString() + "/"));
            zipOut.closeEntry();
        }
        else{//如果目录不为空,则分别处理目录和文件.
            for(File fileName : files){
                //System.out.println(fileName);
                int readedBytes;
                byte[] buf = new byte[64 * 1024];
                if(fileName.isDirectory()){
                    handleDir(root, fileName , zipOut, zipFile);
                }
                else if(!fileName.getAbsolutePath().equals(zipFile.getAbsolutePath())){
                    fileIn = new FileInputStream(fileName);
                    String zipPath = FilenameUtils.separatorsToUnix(fileName.getAbsolutePath()).substring(FilenameUtils.separatorsToUnix(root.getAbsolutePath()).length());
                    if(zipPath.startsWith("/"))
                        zipPath = zipPath.substring(1);
                    zipOut.putNextEntry(new ZipEntry(zipPath));

                    while((readedBytes = fileIn.read(buf))>0){
                        zipOut.write(buf , 0 , readedBytes);
                    }

                    zipOut.closeEntry();
                }
            }
        }
    }
}

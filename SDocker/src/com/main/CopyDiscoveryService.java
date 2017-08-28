package com.main;

import com.docker.storage.DBException;
import com.docker.storage.mongodb.MongoHelper;
import com.docker.file.adapters.GridFSFileHandler;
import org.apache.commons.io.FileUtils;
import script.file.FileAdapter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public class CopyDiscoveryService {
    public static void main(String[] args) throws IOException, DBException {
        MongoHelper helper = new MongoHelper();
        helper.setHost("mongodb://localhost:7900");
        helper.setDbName("gridfiles");
//		helper.setUsername("socialshopsim");
//		helper.setPassword("eDANviLHQtjwmFlywyKu");
        helper.init();
//		helper.setUsername();

        GridFSFileHandler fileHandler = new GridFSFileHandler();
        fileHandler.setResourceHelper(helper);
        fileHandler.setBucketName("imfs");
        fileHandler.init();

//		File directory = new File("/home/aplomb/dev/github/PKUserService/deploy");
        File directory = new File("/home/aplomb/dev/github/DiscoveryService/build/deploy");
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

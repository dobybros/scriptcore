package file

import script.file.LocalFileHandler
import script.groovy.annotation.Bean

/**
 * Created by aplombchen on 3/7/16.
 */
@Bean(name = "localFileHandler")
class FileHandler extends LocalFileHandler{
    public FileHandler() {
        setRootPath("data/");
    }
}

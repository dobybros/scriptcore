package controllers

import groovy.json.JsonSlurper

import java.util.zip.GZIPInputStream

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileItemFactory
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.bson.types.ObjectId

import script.file.FileAdapter
import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import script.groovy.servlet.annotation.ControllerMapping
import script.groovy.servlet.annotation.PathVariable
import script.groovy.servlet.annotation.RequestHeader
import script.groovy.servlet.annotation.RequestMapping
import script.groovy.servlets.GroovyServlet
import chat.errors.CoreException
import chat.logs.LoggerEx

import common.accounts.services.UserService
import common.distribution.Article
import common.distribution.ArticleService
import common.index.IndexAction
import common.index.IndexActionService
import common.controllers.GroovyServletEx
import common.utils.Utils

@ControllerMapping(interceptClass = "intercepters/EmployeeIntercepter.groovy")
public class ArticleForEmployeeController extends GroovyServletEx {
    public static final int ERRORCODE_ARTICLE_UPLOAD_ILLEGAL = 200;
    public static final int ERRORCODE_ARTICLE_FILE_EMPTY = 201;
    public static final int ERRORCODE_ARTICLE_EXCEED_MAXIMUM = 202;
    public static final int ERRORCODE_ARTICLE_UPLOAD_FAILED = 203;
    public static final int ERRORCODE_READCONTENT_FAILED = 204;

    public static final int MAXIMUM = 1024 * 1024 * 100; //100m

    private static final String TAG = ArticleForEmployeeController.class.getSimpleName();
    @Bean
    private GroovyObjectEx<UserService> userService;

    @Bean
    private GroovyObjectEx<ArticleService> articleService;

    @Bean
    private GroovyObjectEx<IndexActionService> indexActionService;

    @Bean(name = "localFileHandler")
    private GroovyObjectEx<FileAdapter> fileHandler;

    @RequestMapping(uri = "rest/article/{articleId}", method = GroovyServlet.DELETE)
    public void deleteArticle(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable(key = "articleId") String articleId,
            @RequestHeader(key = "User-Agent", required = false) String ua) throws CoreException {
        HttpSession session = request.getSession();
        String userId = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERID);
        articleService.getObject().deleteArticle(articleId);

        fileHandler.getObject().deleteFile(new FileAdapter.PathEx(Utils.getDocumentPath(articleId, null),
                articleId, null));
        //if AWS S3, don't need delete directory... this is only for local file to remove the empty folder.
        fileHandler.getObject().deleteDirectory(new FileAdapter.PathEx(Utils.getDocumentPath(articleId, null),
                articleId, null));
        def obj = success();
        respond(response, obj);

        IndexAction indexAction = new IndexAction();
        indexAction.setUserId(userId);
        indexAction.setAction(IndexAction.ACTION_DELETE);
        indexAction.setType(IndexAction.TYPE_ARTICLE);
        indexAction.setTargetId(articleId);
//        indexAction.setTargetUpdateTime(article.getUpdateTime());
        indexActionService.getObject().addIndexAction(indexAction);
    }

    @RequestMapping(uri = "rest/articles", method = GroovyServlet.POST)
    public void uploadArticle(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(key = "User-Agent", required = false) String ua) throws CoreException{
        HttpSession session = request.getSession();
        if(session != null) {
            List<String> myCompanies = session.getAttribute(UserController.SESSION_ATTRIBUTE_MYCOMPANYIDS);
            String userId = session.getAttribute(UserController.SESSION_ATTRIBUTE_USERID);
            final String ARTICLE = "article";
            final String HTML = "html";
            FileItemResource fileItemResource = readForFileItems(request, [ARTICLE, HTML]);

            String htmlContent = null;
            String articleJson = null;
            Map<String, List<FileItem>> itemMap = fileItemResource.getFileItemMap();
            if(itemMap != null) {
                List<FileItem> htmlItems = itemMap.get(HTML);
                if(htmlItems == null || htmlItems.size() != 1) {
                    throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_ILLEGAL, HTML + " item doesn't be found");
                }

                FileItem htmlItem = htmlItems.get(0);
                String contentType = htmlItem.getContentType();
//                if(contentType == null || !contentType.toLowerCase().contains("text/")) {
//                    throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_ILLEGAL, HTML + "'s contentType is illegal, " + contentType);
//                }
                InputStream htmlIs = htmlItem.getInputStream();
                htmlContent = IOUtils.toString(htmlIs, "utf8");
                IOUtils.closeQuietly(htmlIs);

                List<FileItem> jsonItems = itemMap.get(ARTICLE);
                if(jsonItems == null || jsonItems.size() != 1) {
                    throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_ILLEGAL, ARTICLE + " item doesn't be found");
                }

                FileItem jsonItem = jsonItems.get(0);
                contentType = jsonItem.getContentType();
//                if(contentType == null || !contentType.toLowerCase().contains("application/json")) {
//                    throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_ILLEGAL, ARTICLE + "'s contentType is illegal, " + contentType);
//                }
                InputStream jsonIs = jsonItem.getInputStream();
                articleJson = IOUtils.toString(jsonIs, "utf8");
                IOUtils.closeQuietly(jsonIs);
            }

            if(StringUtils.isBlank(htmlContent) || StringUtils.isBlank(articleJson))
                throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_ILLEGAL, ARTICLE + " or " + HTML + " is null, " + articleJson + " | " + htmlContent);

            FileItem[] items = fileItemResource.getOtherFileItems();

            String htmlResourceId = ObjectId.get().toString();
            HashSet<String> addedFiles = new HashSet<>();
            Article article = null;
            try {
                if(items != null) {
                    for(FileItem item : items) {
                        saveFile(item.getName(), htmlResourceId, item, null);
                        addedFiles.add(item.getName());
                        htmlContent = htmlContent.replace("{" + item.getFieldName() + "}", item.getName());
                    }
                }
                final String INDEX = "index.html";
                saveFile(INDEX, htmlResourceId, htmlContent, null);
                addedFiles.add(INDEX);

                def slurper = new JsonSlurper()
                def json = slurper.parseText(articleJson);

                article = new Article();
                article.setId(htmlResourceId);
                article.setCompanyIds(myCompanies);
                article.setSummary(json["summary"]);
                article.setTitle(json["title"]);
                article.setUserId(userId);
                article.setUrl("/resource/" + htmlResourceId + "/" + INDEX);
                articleService.getObject().addArticle(article);

                def obj = success();
                obj.id = article.getId();
                obj.updateTime = article.getUpdateTime();
                obj.createTime = article.getCreateTime();
                respond(response, obj);
            } catch(Throwable t) {
                t.printStackTrace();
                LoggerEx.error(TAG, "upload article " + htmlResourceId + " failed, " + t.getMessage());
                //Remove obsoleted file after an error occurred.
                for(String fileName : addedFiles) {
                    fileHandler.getObject().deleteFile(new FileAdapter.PathEx(Utils.getDocumentPath(htmlResourceId, fileName),
                            htmlResourceId, null))
                }
                throw t;
            }
            if(article != null) {
                IndexAction indexAction = new IndexAction();
                indexAction.setUserId(userId);
                indexAction.setAction(IndexAction.ACTION_ADD);
                indexAction.setType(IndexAction.TYPE_ARTICLE);
                indexAction.setTargetId(article.getId());
                indexAction.setTargetUpdateTime(article.getUpdateTime());
                indexActionService.getObject().addIndexAction(indexAction);
            }
        }
    }

    private void saveFile(String fileName, String resourceId, FileItem fileItem, FileAdapter.MetadataEx metadata) throws CoreException {
        InputStream is = fileItem.getInputStream();
        try {
            saveFile(fileName, resourceId, is, metadata);
        } finally {
            if(is != null)
                IOUtils.closeQuietly(is);
        }
    }

    private void saveFile(String fileName, String resourceId, String content, FileAdapter.MetadataEx metadata) throws CoreException {
        InputStream is = new ByteArrayInputStream(content.getBytes("utf8"));
        try {
            saveFile(fileName, resourceId, is, metadata);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private void saveFile(String fileName, String resourceId, InputStream is, FileAdapter.MetadataEx metadata) throws CoreException {
        try {
            fileHandler.getObject().saveFile(is,
                    new FileAdapter.PathEx(Utils.getDocumentPath(resourceId, fileName),
                            resourceId, metadata), FileAdapter.FileReplaceStrategy.REPLACE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_FAILED, "Save upload file " + resourceId + " failed, " + e.getMessage());
        }
    }

    public static class FileItemResource {
        private Map<String, List<FileItem>> fileItemMap;
        private List<FileItem> otherFileItems;
        private String content;

        public Map<String, List<FileItem>> getFileItemMap() {
            return fileItemMap;
        }
        public void setFileItemMap(Map<String, List<FileItem>> fileItemMap) {
            this.fileItemMap = fileItemMap;
        }
        public List<FileItem> getOtherFileItems() {
            return otherFileItems;
        }
        public void setOtherFileItems(List<FileItem> otherFileItems) {
            this.otherFileItems = otherFileItems;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }
    private FileItemResource readForFileItems(HttpServletRequest request, Collection<String> names) throws CoreException {
        String contentType = request.getContentType();
        String content = null;
        Map<String, List<FileItem>> fileItemMap = new HashMap<>();
        List<FileItem> otherFileItems = new ArrayList<>();
        if(contentType != null && contentType.contains("multipart/form-data")) {
            FileItem[] fileItems = readFileItems(request);
            boolean hit = false;
            for(FileItem item : fileItems) {
                if(names != null) {
                    for(String name : names) {
                        List<FileItem> items = fileItemMap.get(name);
                        if(items == null) {
                            items = new ArrayList<>();
                            fileItemMap.put(name, items);
                        }
                        if(item.getFieldName() != null && (name.endsWith("*") ? item.getFieldName().regionMatches(0, name, 0, name.length() - 1) : item.getFieldName().equals(name))) {
                            items.add(item);
                            hit = true;
                            break;
                        }
                    }
                }
                if(!hit)
                    otherFileItems.add(item);
                else
                    hit = false;
            }
        } else {
            content = readContent(request);
        }
        if(content == null && (fileItemMap.isEmpty() && otherFileItems.isEmpty()))
            throw new CoreException(ERRORCODE_READCONTENT_FAILED, "content is empty");
        FileItemResource iconResource = new FileItemResource();
        iconResource.setContent(content);
        iconResource.setFileItemMap(fileItemMap);
        iconResource.setOtherFileItems(otherFileItems);
        return iconResource;
    }

    private String readContent(HttpServletRequest request) throws CoreException{
        InputStream is = null;
        try {
            is = new BufferedInputStream(request.getInputStream());
            String contentEncoding = request.getHeader("Content-Encoding");
            if(contentEncoding != null && contentEncoding.indexOf("gzip") != -1) {
                is = new GZIPInputStream(is);
            }

            String json = IOUtils.toString(is, "utf8");
            LoggerEx.info(TAG, request.getRequestURL().toString() + " Request Body = " + json);
            return json;
        } catch (IOException e) {
            e.printStackTrace();
            throw new CoreException(ERRORCODE_READCONTENT_FAILED, e.getMessage());
        } finally {
            if(is != null) {
                IOUtils.closeQuietly(is);
            }
        }
    }

    private FileItem[] readFileItems(HttpServletRequest request) throws CoreException {
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            List<FileItem> items = upload.parseRequest(request);
            List<FileItem> newItems = new ArrayList<FileItem>();
            for(FileItem fi : items) {
                if(fi.getSize() <= 0)
                    throw new CoreException(ERRORCODE_ARTICLE_FILE_EMPTY, "File is empty.");
                if(fi.getSize() > MAXIMUM)
                    throw new CoreException(ERRORCODE_ARTICLE_EXCEED_MAXIMUM, "FileItem has over the maximum limits : 10k.");
                newItems.add(fi);
            }
            if(newItems.size() > 0)
                return newItems.toArray(new FileItem[newItems.size()]);
        } catch (FileUploadException e) {
            e.printStackTrace();
            throw new CoreException(ERRORCODE_ARTICLE_UPLOAD_FAILED, "Upload failed, " + e.getMessage());
        }
        return null;
    }
}

package timertasks

import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.io.IOUtils
import org.jsoup.Jsoup;

import script.groovy.annotation.Bean
import script.groovy.object.GroovyObjectEx
import solr.ArticleIndex;
import common.distribution.Article
import common.distribution.ArticleService
import common.index.IndexAction
import common.index.IndexActionService

@TimerTask(period = 10000L)
class HandleIndexActionTask {
	
	@Bean 
	private GroovyObjectEx<IndexActionService> indexActionService;
	
	@Bean
	private GroovyObjectEx<ArticleService> articleService; 
	
	public void main() {
		String urlHost = "http://localhost:6066/rest"
		IndexAction indexAction = null;
		while((indexAction = indexActionService.getObject().findAndDelete()) != null) {
			Integer type = indexAction.getType();
			if(type != null) {
				switch(type) {
					case IndexAction.TYPE_ARTICLE:
						Integer action = indexAction.getAction();
						String articleId = indexAction.getTargetId();
						if(action != null && articleId != null) {
							switch(action) {
								case IndexAction.ACTION_ADD:
								case IndexAction.ACTION_UPDATE:
									Article article = articleService.getObject().getArticle(articleId);
									if(article != null) {
										String url = article.getUrl();
										if(url != null) {
											HttpClient httpClient = new HttpClient();
											GetMethod method = new GetMethod(urlHost + url);
											int code = httpClient.executeMethod(method);
											if(code == 200) {
												InputStream is = method.getResponseBodyAsStream();
												String responseStr = IOUtils.toString(is, "utf8");
												String textOnly = Jsoup.parse(responseStr).text();

												ArticleIndex index = new ArticleIndex();
												index.fromArticle(article);
												index.setContent(textOnly);
												SolrIndexService.getInstance().addIndex(index);
											}
										}
									}
									break;
								case IndexAction.ACTION_DELETE:
									SolrIndexService.getInstance().deleteIndex(articleId, ArticleIndex.class);
									break;
							}
						}
						break;
				}
			} 
		}
	}
}

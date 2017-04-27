
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.commons.lang3.StringUtils;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {

	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp3|zip|gz))$");

	/**
	 * This method receives two parameters. The first parameter is the page in
	 * which we have discovered this new url and the second parameter is the new
	 * url. You should implement this function to specify whether the given url
	 * should be crawled or not (based on your crawling logic). In this example,
	 * we are instructing the crawler to ignore urls that have css, js, git, ...
	 * extensions and to only accept urls that start with
	 * "http://www.ics.uci.edu/". In this case, we didn't need the referringPage
	 * parameter to make the decision.
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		return !FILTERS.matcher(href).matches() && href.startsWith("http://www.gov.cn/");
	}

	/**
	 * This function is called when a page is fetched and ready to be processed
	 * by your program.
	 */
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		String fname = UUID.randomUUID().toString();
		System.out.println("URL: " + url + " filename:" + fname);

		if (page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			Set<WebURL> links = htmlParseData.getOutgoingUrls();

			PrintWriter out = null;
			PrintWriter out2 = null;
			PrintWriter out3 = null;
			try {

				out = new PrintWriter("F:/data/nlp/" + fname + ".txt");
				out.println(url);
				out.println(text);
				out.flush();

				// corenlp
				out2 = new PrintWriter("F:/data/nlp/" + fname + ".corenlp.output");
				out2.println(url);
				long begin2 = System.currentTimeMillis();
				Annotation annotation = new Annotation(text);
				CrawlerWithNLPTest.pipeline.annotate(annotation);

				List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

				if (sentences != null) {
					for (int i = 0, sz = sentences.size(); i < sz; i++) {
						CoreMap sentence = sentences.get(i);
						List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
						String[] tokenAnnotations = { "Text", "PartOfSpeech" };
						for (CoreLabel token : tokens) {
							out2.print(token.toShorterString(tokenAnnotations));
							out2.println();
						}

					}
				}
				long end2 = System.currentTimeMillis();
				out2.println(CrawlerWithNLPTest.pipeline.timingInformation());
				out2.println("corenlp total length" + text.length() + "total time consumed:" + (end2 - begin2));
				out2.flush();
				System.out.println("corenlp total length" + text.length() + " total time consumed:" + (end2 - begin2));

				// ansj
				out3 = new PrintWriter("F:/data/nlp/" + fname + ".ansj.output");
				out3.println(url);
				long begin3 = System.currentTimeMillis();

				for (Term t : ToAnalysis.parse(text).getTerms()) {
				
					if (!StringUtils.isBlank(t.getName())) {
						out3.print("[Text=" + t.getName() + " PartOfSpeech=" + t.getNatureStr() + "]");
						out3.println();
					}
				}
				long end3 = System.currentTimeMillis();
				out3.println("ansj total length" + text.length() + "total time consumed:" + (end3 - begin3));
				out3.flush();
				System.out.println("ansj    total length" + text.length() + " total time consumed:" + (end3 - begin3));
				
				

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				IOUtils.closeIgnoringExceptions(out);
				IOUtils.closeIgnoringExceptions(out2);
				IOUtils.closeIgnoringExceptions(out3);
			}
		}
	}
}
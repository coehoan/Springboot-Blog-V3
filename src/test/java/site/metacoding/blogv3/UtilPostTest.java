package site.metacoding.blogv3;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;

public class UtilPostTest {

    @Test
    public void getContentWithoutImg() {
        // 1. 가짜데이터
        String html = "안녕 <img src='#'> 반가워 <img src='#'>";
        Document doc = Jsoup.parse(html);
        // System.out.println(doc);

        // 2. 실행
        Elements els = doc.select("img");
        // System.out.println(els.size());
        for (Element el : els) {
            el.remove();
        }
        // Elements response = doc.select("img").remove();

        // System.out.println(response);
        System.out.println(doc);
        // 3. 검증
        Elements els2 = doc.select("img");
        assertTrue(els2.size() == 0);
    }
}

package site.metacoding.blogv3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer { // web.xml = poko 설정파일

    @Value("${file.path}")
    private String uploadFolder;

    // web.xml의 기본설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);

        registry
                .addResourceHandler("/upload/**")
                .addResourceLocations("file:///" + uploadFolder) // file 프로토콜은 :/// 사용
                .setCachePeriod(60 * 60) // 초 단위 -> 한시간
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

}

package hunre.it.backendchess.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // áp dụng cho tất cả các endpoint có đường dẫn bắt đầu bằng /api/
                .allowedOrigins("http://localhost:5173","https://chess-frontend-one-roan.vercel.app/") // cho phép request từ origin này
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

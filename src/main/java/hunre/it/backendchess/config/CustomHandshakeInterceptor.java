package hunre.it.backendchess.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class CustomHandshakeInterceptor implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        // Ví dụ: lấy username từ query string
        String query = request.getURI().getQuery(); // "username=abc" chẳng hạn
        if (query != null && query.contains("username=")) {
            String username = query.split("username=")[1];
            // Lưu username vào attributes để sau này dùng cho Principal
            attributes.put("username", username);
            return true;
        }
        // Nếu không có thông tin, bạn có thể từ chối handshake hoặc gán giá trị mặc định
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Không cần xử lý sau handshake
    }
}

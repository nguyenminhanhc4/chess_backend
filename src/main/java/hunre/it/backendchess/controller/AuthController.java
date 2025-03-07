package hunre.it.backendchess.controller;

import hunre.it.backendchess.DTO.RegisterRequest;
import hunre.it.backendchess.DTO.JwtAuthenticationResponse;
import hunre.it.backendchess.DTO.LoginRequest;
import hunre.it.backendchess.exception.CustomException;
import hunre.it.backendchess.models.User;
import hunre.it.backendchess.repository.UserRepository;
import hunre.it.backendchess.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        // Kiểm tra xác nhận mật khẩu
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new CustomException("Mật khẩu xác nhận không khớp!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new CustomException("Email đã được sử dụng!");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new CustomException("Tên người dùng đã tồn tại!");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        // Mã hóa mật khẩu trước khi lưu vào DB
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // Ví dụ: kiểm tra đăng nhập và sinh token
        // Nếu không tìm thấy user hoặc mật khẩu không đúng, ném ngoại lệ:
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new CustomException("Email hoặc mật khẩu không chính xác!"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new CustomException("Email hoặc mật khẩu không chính xác!");
        }

        // Sinh JWT token
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new JwtAuthenticationResponse(token));
    }
}

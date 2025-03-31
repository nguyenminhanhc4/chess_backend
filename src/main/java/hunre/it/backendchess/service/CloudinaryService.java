package hunre.it.backendchess.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

@Service
public class CloudinaryService {

    private Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    // Phương thức upload file từ MultipartFile
    public String uploadFile(MultipartFile multipartFile) throws Exception {
        // Tạo file tạm thời
        File file = convertMultiPartToFile(multipartFile);
        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
        file.delete(); // Xóa file tạm sau khi upload
        return (String) uploadResult.get("secure_url");
    }

    private File convertMultiPartToFile(MultipartFile file) throws Exception {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}

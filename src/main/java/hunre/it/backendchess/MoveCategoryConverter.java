package hunre.it.backendchess;
import hunre.it.backendchess.models.MoveCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoveCategoryConverter implements AttributeConverter<MoveCategory, String> {

    @Override
    public String convertToDatabaseColumn(MoveCategory category) {
        if (category == null) {
            return null;
        }
        return category.name().toLowerCase(); // Lưu vào DB dạng chữ thường
    }

    // Nếu DB đang lưu dạng "Genius_Move" hoặc "genius_move"
    @Override
    public MoveCategory convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        String formatted = dbValue.toUpperCase().replace(' ', '_'); // Xử lý thêm nếu cần
        return MoveCategory.valueOf(formatted);
    }
}

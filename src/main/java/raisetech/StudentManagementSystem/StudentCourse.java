package raisetech.StudentManagementSystem;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@JsonPropertyOrder({"id", "studentId", "courseName", "courseStartAt", "courseEndAt"})
public class StudentCourse {

  private Integer id;
  private Integer studentId;
  private String courseName;
  private LocalDateTime courseStartAt;
  private LocalDateTime courseEndAt;
}

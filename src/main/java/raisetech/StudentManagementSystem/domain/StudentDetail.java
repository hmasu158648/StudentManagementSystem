package raisetech.StudentManagementSystem.domain;

import java.util.List;
import lombok.Data;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourses;

/**
 * ドメインオブジェクト
 */
@Data
public class StudentDetail {

  private Student student;
  private List<StudentCourses> studentCourses;
}

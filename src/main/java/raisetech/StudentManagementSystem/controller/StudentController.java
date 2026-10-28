package raisetech.StudentManagementSystem.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagementSystem.controller.converter.StudentConverter;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.service.StudentService;

@RestController
public class StudentController {

  private final StudentService service;
  private final StudentConverter converter;

  @Autowired
  public StudentController(StudentService service, StudentConverter converter) {
    this.service = service;
    this.converter = converter;
  }

  @GetMapping("/studentList")
  public List<StudentDetail> getStudentList() {
    // ①studentテーブルから全件をListで取得
    List<Student> students = service.searchStudentList();
    // ②studentCoursesテーブルから全件をListで取得
    List<StudentCourses> studentCourses = service.searchStudentCourseList();

    // ⑪全ての拡張for文が終了後に、studentDetailsを返す（生徒全員分）
    // 各生徒とコース情報が紐づくStudentDetailクラスのListを返す
    return converter.convertStudentDetails(students, studentCourses);
  }


  @GetMapping("/studentCourseList")
  public List<StudentCourses> getStudentCourseList() {
    return service.searchStudentCourseList();
  }
}

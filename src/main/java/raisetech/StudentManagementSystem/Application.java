package raisetech.StudentManagementSystem;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

  @Autowired
  private StudentRepository repository;

  public static void main(String[] args) {
    // localhost:8080
    SpringApplication.run(Application.class, args);
  }

  @GetMapping("/studentList")
  public List<Student> getStudentList() {
    return repository.searchStudents();
  }

  @GetMapping("/studentCourseList")
  public List<StudentCourse> getStudentCourseList() {
    return repository.searchStudentCourses();
  }
}

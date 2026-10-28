package raisetech.StudentManagementSystem.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourses;
import raisetech.StudentManagementSystem.repository.StudentRepository;

@Service
public class StudentService {

  private final StudentRepository repository;

  @Autowired
  public StudentService(StudentRepository repository) {
    this.repository = repository;
  }

  public List<Student> searchStudentList() {

    return repository.search();

    // 検索処理
    // List<Student> studentList = repository.search();

    // 絞り込みをする。年齢が30代の人のみを抽出する。
    // 抽出したリストをコントローラーに返す。
    // return studentList.stream()
    //     .filter(student -> student.getAge() != null)
    //     .filter(student -> student.getAge() >= 30)
    //     .toList();
  }

  public List<StudentCourses> searchStudentCourseList() {

    return repository.searchStudentCourses();

    // 検索処理
    // List<StudentCourses> studentsCourseList = repository.searchStudentCourses();

    // 絞り込み検索で「Javaコース」のコース情報のみを抽出する。
    // 抽出したリストをコントローラーに返す。
    // return studentsCourseList.stream()
    //     .filter(studentCourses -> studentCourses.getCourseName().equals("Javaコース"))
    //     .toList();
  }
}

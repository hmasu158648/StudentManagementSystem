package raisetech.StudentManagementSystem.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;
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

  public List<StudentsCourses> searchStudentCourseList() {

    return repository.searchStudentCourses();

    // 検索処理
    // List<StudentCourses> studentsCourseList = repository.searchStudentCourses();

    // 絞り込み検索で「Javaコース」のコース情報のみを抽出する。
    // 抽出したリストをコントローラーに返す。
    // return studentsCourseList.stream()
    //     .filter(studentCourses -> studentCourses.getCourseName().equals("Javaコース"))
    //     .toList();
  }

  // 受講生とコース情報をまとめて登録する
  // @Transactional：途中で失敗したら、それまでの登録をすべて取り消す
  @Transactional
  public void registerStudent(StudentDetail studentDetail) {
    // 1. 申込用紙から受講生を取り出して登録する
    //    （@Options のおかげで、登録後に student の id へ自動採番された番号が入る）
    repository.registerStudent(studentDetail.getStudent());

    // 2. List<StudentCourses>の棚から1つ取り出して登録する
    for (StudentsCourses studentCourses : studentDetail.getStudentsCourses()) {
      // 2-1. どの受講生のコースかを示すため、受講生の id をセットする
      studentCourses.setStudentId(studentDetail.getStudent().getId());
      // 2-2. 開始日に登録日（今の日時）をセットする（終了日は空のまま）
      studentCourses.setCourseStartAt(LocalDateTime.now());
      // 2-3. コースを登録する
      repository.registerStudentCourse(studentCourses);
    }
  }
}

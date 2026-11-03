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
  }

  public List<StudentsCourses> searchStudentCourseList() {
    return repository.searchStudentCoursesList();
  }

  /**
   * 受講生とコース情報を新規登録します。
   * 受講生を登録したあと、採番された受講生IDと開始日時（現在日時）を各コースにセットして登録します。
   * 処理の途中でエラーが発生した場合は、すべての登録を取り消します。
   *
   * @param studentDetail 登録する受講生情報とコース情報
   */
  @Transactional
  public void registerStudent(StudentDetail studentDetail) {
    repository.registerStudent(studentDetail.getStudent());
    for (StudentsCourses studentCourses : studentDetail.getStudentsCourses()) {
      // 2-1. どの受講生のコースかを示すため、受講生の id をセットする
      studentCourses.setStudentId(studentDetail.getStudent().getId());
      // 2-2. 開始日に登録日（今の日時）をセットする（終了日は空のまま）
      studentCourses.setCourseStartAt(LocalDateTime.now());
      // 2-3. コースを登録する
      repository.registerStudentCourse(studentCourses);
    }
  }

  /**
   * 受講生IDをもとに、受講生情報とその受講生に紐づくコース情報を検索し、
   * 受講生詳細（StudentDetail）としてまとめて返します。
   *
   * @param id 検索対象の受講生ID
   * @return 受講生情報とコース情報をまとめた受講生詳細
   */
  public StudentDetail searchStudent(Integer id) {
    // ①引数のIDに一致する受講生情報をデータベースから取得し、studentに格納する
    Student student = repository.searchStudent(id);
    // ②受講生のIDを使って、その受講生のコース情報一覧を取得する
    List<StudentsCourses> studentsCourses =
        repository.searchStudentCourses(student.getId());

    // ③StudentDetailクラスのインスタンスを作成する
    StudentDetail studentDetail = new StudentDetail();

    // ④上記で作成したインスタンスのフィールドに値をセットする
    studentDetail.setStudent(student);
    studentDetail.setStudentsCourses(studentsCourses);

    // ⑤StudentDetailクラスのインスタンスを返す
    return studentDetail;
  }

  /**
   * １人の受講生情報と、その受講生に紐づくコース情報を更新します。
   * 処理の途中でエラーが発生した場合は、すべての更新を取り消します。
   *
   * @param studentDetail 更新する受講生情報とコース情報
   */
  @Transactional
  public void updateStudent(StudentDetail studentDetail) {
    // ①studentDetailからstudentオブジェクトを取り出して受講生情報を更新する
    repository.updateStudent(studentDetail.getStudent());
    // ②コース情報を1件ずつ取り出して更新する
    for (StudentsCourses studentsCourses : studentDetail.getStudentsCourses()) {
      repository.updateStudentCourse(studentsCourses);
    }
  }
}

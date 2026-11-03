package raisetech.StudentManagementSystem.controller.converter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;

/**
 * 受講生とコース情報を、画面表示用の受講生詳細（StudentDetail）に変換するコンバーターです。
 * Springのコンポーネントとして管理され、コントローラーから利用されます。
 */
@Component
public class StudentConverter {

  /**
   * 受講生一覧とコース情報一覧から、受講生ごとの詳細情報（受講生＋その人のコース）を作成します。
   * 受講生のidとコース情報の受講生idが一致するものを、その受講生のコースとして紐づけます。
   *
   * @param students        受講生一覧
   * @param studentsCourses 受講生コース情報一覧（全受講生分）
   * @return 受講生ごとにコース情報をまとめた受講生詳細の一覧
   */
  public List<StudentDetail> convertStudentDetails(List<Student> students, List<StudentsCourses> studentsCourses) {
    // ③ドメインクラス(StudentDetail)のListを作成
    List<StudentDetail> studentDetails = new ArrayList<>();
    // ④ forEach（ラムダ式）で生徒を1人ずつ処理する
    students.forEach(student -> {
      // ⑤ドメインクラスのインスタンスを生成
      StudentDetail studentDetail = new StudentDetail();
      // ⑥生成したドメインクラスのインスタンスに生徒情報をセット
      studentDetail.setStudent(student);

      // 生徒全体のコース情報が入ったstudentCoursesを生徒個別用に作成したconvertStudentCoursesに入れる
      List<StudentsCourses> convertStudentCourses = studentsCourses.stream()
          // ⑦studentのIDとstudentCourseのstudentIDが一致しているものを抽出
          // ⑧抽出したStudentCourse情報をListに変換
          .filter(studentCourse -> studentCourse.getStudentId().equals(student.getId()))
          .toList();
      // ⑨生徒１人分のコース情報をListに変換後に、新規作成していたstudentDetailにconvertStudentCoursesリストをセットする
      studentDetail.setStudentsCourses(convertStudentCourses);
      // ⑩上記で生徒情報とその生徒に紐づくコース情報を持ったstudentDetailインスタンスを
      // studentDetailsリストにセット（生徒１人分）
      studentDetails.add(studentDetail);
    });
    // 生徒全員分のループ処理が終わったあとはStudentDetailsリストを返す。
    return studentDetails;
  }
}

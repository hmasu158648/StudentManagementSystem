package raisetech.StudentManagementSystem.controller.converter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;

/**
 * 受講生詳細を受講生や受講生コース情報、もしくはその逆の変換をおこなうコンバーターです。
 * Springのコンポーネントとして管理され、サービスから利用されます。
 */
// @Component：Springが管理する部品として登録する（Serviceのコンストラクターに自動で渡される）
@Component
public class StudentConverter {

  /**
   * 受講生一覧とコース情報一覧から、受講生ごとの詳細情報（受講生＋その人のコース）を作成します。
   * 受講生のidとコース情報の受講生idが一致するものを、その受講生のコースとして紐づけます。
   *
   * @param studentList       受講生一覧
   * @param studentCourseList 受講生コース情報一覧（全受講生分）
   * @return 受講生ごとにコース情報をまとめた受講生詳細の一覧
   */
  public List<StudentDetail> convertStudentDetails(List<Student> studentList, List<StudentCourse> studentCourseList) {
    // ①②引数で、受講生の一覧(studentList)と、全受講生分のコース情報の一覧(studentCourseList)を受け取っている
    // ③ドメインクラス(StudentDetail)のListを作成（ここに受講生1人分ずつの詳細を追加していく）
    List<StudentDetail> studentDetails = new ArrayList<>();
    // ④ forEach（ラムダ式）で生徒を1人ずつ処理する
    studentList.forEach(student -> {
      // ⑤ドメインクラスのインスタンスを生成
      StudentDetail studentDetail = new StudentDetail();
      // ⑥生成したドメインクラスのインスタンスに生徒情報をセット
      studentDetail.setStudent(student);

      // 全受講生分のコース情報(studentCourseList)から、この生徒のコースだけを選び出して
      // 生徒個別用のconvertStudentCoursesに入れる
      List<StudentCourse> convertStudentCourseList = studentCourseList.stream()
          // ⑦studentのIDとstudentCourseのstudentIDが一致しているものを抽出
          // ⑧抽出したStudentCourse情報をListに変換
          .filter(studentCourse -> studentCourse.getStudentId().equals(student.getId()))
          .toList();
      // ⑨生徒１人分のコース情報をListに変換後に、新規作成していたstudentDetailにconvertStudentCoursesリストをセットする
      studentDetail.setStudentCourseList(convertStudentCourseList);
      // ⑩上記で生徒情報とその生徒に紐づくコース情報を持ったstudentDetailインスタンスを
      // studentDetailsリストにセット（生徒１人分）
      studentDetails.add(studentDetail);
    });
    // ⑪生徒全員分のループ処理が終わったあとはStudentDetailsリストを返す。
    // （呼び出し元のServiceがこの一覧を受け取り、Controllerを通してJSONのレスポンスボディとして返される）
    return studentDetails;
  }
}

package raisetech.StudentManagementSystem.controller.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;

@Component
public class StudentConverter {

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
          .filter(studentCourse -> student.getId().equals(studentCourse.getStudentId())).collect(Collectors.toList());
      // ⑨ループ終了後に、新規作成していたstudentDetailにconvertStudentCoursesリストをセットする
      studentDetail.setStudentsCourses(convertStudentCourses);
      // ⑩上記で生徒情報とその生徒に紐づくコース情報を持ったstudentDetailインスタンスを
      // studentDetailsリストにセット（生徒１人分）
      studentDetails.add(studentDetail);
    });
    // ループ処理が終わったあとはStudentDetailsリストを返す。
    return studentDetails;
  }
}

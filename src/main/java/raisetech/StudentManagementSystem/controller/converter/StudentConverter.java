package raisetech.StudentManagementSystem.controller.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;

@Component
public class StudentConverter {

  public List<StudentDetail> convertStudentDetails(List<Student> students, List<StudentCourses> studentCourses) {
    // ③コンバータークラス(StudentDetail)のListを作成
    List<StudentDetail> studentDetails = new ArrayList<>();
    // ④studentsを拡張for文で回す
    students.forEach(student -> {
      // ⑤コンバータークラスのインスタンスを生成
      StudentDetail studentDetail = new StudentDetail();
      // ⑥生成したコンバーターインスタンスに生徒情報をセット
      studentDetail.setStudent(student);
      List<StudentCourses> convertStudentCourses = studentCourses.stream()
          // ⑦studentのIDとstudentCourseのstudentIDが一致しているものを抽出
          // ⑧抽出したStudentCourse情報をListに変換
          .filter(studentCourse -> student.getId().equals(studentCourse.getStudentId())).collect(Collectors.toList());
      // ⑨コースリストのループ終了後に、studentDetailにconvertStudentCoursesをセットする
      studentDetail.setStudentCourses(convertStudentCourses);
      // ⑩上記で生徒情報とその生徒に紐づくコース情報を持ったstudentDetailインスタンスを
      // studentDetailsリストにセット（生徒１人分）
      studentDetails.add(studentDetail);
    });
    return studentDetails;
  }
}

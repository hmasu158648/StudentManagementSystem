package raisetech.StudentManagementSystem.controller.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;

class StudentConverterTest {

  private StudentConverter sut;

  @BeforeEach
  void before() {
    sut = new StudentConverter();
  }

  @Test
  void 受講生一覧とコース情報から_受講生ごとの詳細情報を作成できること() {
    // 【事前準備】受講生を1人と、その人のコースを１つ用意する
    Integer id = 999;
    Student student = new Student();
    student.setId(id);

    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId(id);

    //【実行】
    List<StudentDetail> actual = sut.convertStudentDetails(List.of(student), List.of(studentCourse));

    // 【検証】
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getStudent()).isEqualTo(student);
    assertThat(actual.get(0).getStudentCourseList()).containsExactly(studentCourse);
  }

  @Test
  void 受講生一覧とコース情報から_受講生ごとに適切なコース情報が割り当てられて作成されること() {
    // 【事前準備】受講生１人と、その人のコース・別のコースを１つずつ用意する
    Integer id = 999;
    Integer otherId = 111; // 別の受講生のid

    Student student = new Student();
    student.setId(id);

    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId(id);

    StudentCourse otherStudentCourse = new StudentCourse();
    otherStudentCourse.setStudentId(otherId);

    //【実行】
    List<StudentDetail> actual = sut.convertStudentDetails(List.of(student), List.of(studentCourse, otherStudentCourse));

    //【検証】
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getStudent()).isEqualTo(student);
    assertThat(actual.get(0).getStudentCourseList()).containsExactly(studentCourse);
  }
}

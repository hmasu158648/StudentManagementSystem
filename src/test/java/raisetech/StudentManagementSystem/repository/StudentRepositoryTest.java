package raisetech.StudentManagementSystem.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;

/**
 * StudentRepository（データベースとやり取りする係）のテストクラスです。
 */
// @MybatisTest：「MyBatis（SQLを実行する道具）とデータベースの部分だけを動かしてテストするよ」という目印。
// ControllerやServiceは読み込まれないので、Repositoryの動きだけを調べられる
@MybatisTest
class StudentRepositoryTest {

  @Autowired
  private StudentRepository sut;

  @Test
  void 受講生の全件検索がおこなえること() {
    List<Student> actual = sut.search();
    assertThat(actual.size()).isEqualTo(16);
  }

  @Test
  void 受講生をidで1人検索できること() {
    Student actual = sut.searchStudent(1);

    assertThat(actual.getName()).isEqualTo("山田太郎");
  }

  @Test
  void 受講生コースの全件検索がおこなえること() {
    List<StudentCourse> actual = sut.searchAllStudentCoursesList();
    assertThat(actual.size()).isEqualTo(23);
  }

  @Test
  void 受講生IDに紐づく受講生コース情報を検索できること() {

    List<StudentCourse> actual = sut.searchStudentCourseList(1);

    assertThat(actual.size()).isEqualTo(3);

    assertThat(actual)
        .extracting(StudentCourse::getCourseName)
        .containsExactlyInAnyOrder("Javaコース", "AWSフルコース", "AIエンジニア育成コース");

    assertThat(actual)
        .extracting(StudentCourse::getStudentId)
        .containsOnly(1);
  }

  @Test
  void 存在しない受講生IDでコースを検索すると空の一覧が返ること() {
    List<StudentCourse> actual = sut.searchStudentCourseList(999);

    assertThat(actual).isEmpty();
  }

  @Test
  void 受講生の登録がおこなえること() {
    Student student = createValidStudent();

    sut.registerStudent(student);

    List<Student> actual = sut.search();

    assertThat(actual.size()).isEqualTo(17);
  }

  @Test
  void 受講生の情報を更新できること() {
    Student student = sut.searchStudent(1);
    student.setNickname("たろさん");
    student.setArea("兵庫県");

    sut.updateStudent(student);

    Student actual = sut.searchStudent(1);

    assertThat(actual.getNickname()).isEqualTo("たろさん");
    assertThat(actual.getArea()).isEqualTo("兵庫県");
  }

  @Test
  void 受講生のコース情報を更新できること() {
    List<StudentCourse> studentCourseList = sut.searchStudentCourseList(21);
    StudentCourse studentCourse = studentCourseList.get(0);
    studentCourse.setCourseName("Webマーケティングコース");

    sut.updateStudentCourse(studentCourse);

    List<StudentCourse> actual = sut.searchStudentCourseList(21);

    assertThat(actual.get(0).getCourseName()).isEqualTo("Webマーケティングコース");
  }

  @Test
  void 特定の受講生を論理削除できること() {
    Student student = sut.searchStudent(1);
    student.setIsDeleted(true);

    sut.updateStudent(student);

    Student actual = sut.searchStudent(1);

    assertThat(actual.getIsDeleted()).isTrue();
  }

  @Test
  void 受講生のコースが登録されること() {
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setCourseName("AIエンジニア育成コース");
    studentCourse.setStudentId(21);

    sut.registerStudentCourse(studentCourse);

    List<StudentCourse> actual = sut.searchStudentCourseList(21);

    assertThat(actual)
        .extracting(StudentCourse::getCourseName)
        .containsExactlyInAnyOrder("Javaコース", "AIエンジニア育成コース");

    assertThat(actual)
        .extracting(StudentCourse::getStudentId)
        .containsOnly(21);
  }

  private Student createValidStudent() {
    // ①空の受講生を作る
    Student student = new Student();
    // ②名前・ふりがな（全角カタカナ）・メールアドレスを、ルールを守った正しい形で入れる
    student.setName("岡本太郎");
    student.setKanaName("オカモトタロウ");
    student.setNickname("オカモッチャン");
    student.setEmail("okamoto_taro@example.com");
    student.setArea("栃木県");
    student.setAge(63);
    student.setSex("男性");
    student.setRemark("");
    student.setIsDeleted(false);
    // ③できあがった受講生を、テストで使えるように返す
    return student;
  }
}
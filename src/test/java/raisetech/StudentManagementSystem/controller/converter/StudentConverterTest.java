package raisetech.StudentManagementSystem.controller.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;

/**
 * StudentConverter（受講生とコース情報を、受講生詳細にまとめる係）のテストクラスです。
 * Springやデータベースは使わず、StudentConverterを直接 new して動きを確かめます。
 *
 * <p>【テストって何？】
 * 「このメソッドは、こういう入力のとき、こういう結果になるはず」という予想を、プログラムで自動チェックするものです。
 * テストは、次の3つの手順で書くとわかりやすくなります。
 * <ol>
 *   <li>【事前準備】テストに使うデータを用意する</li>
 *   <li>【実行】テストしたいメソッドを呼ぶ</li>
 *   <li>【検証】結果が予想どおりか確認する</li>
 * </ol>
 */
class StudentConverterTest {

  // sut：System Under Test（テストの対象）の略。ここではテストしたいStudentConverterのこと
  private StudentConverter sut;

  /**
   * 各テストが始まる前に毎回呼ばれ、テスト対象のStudentConverterを新しく作ります。
   * テストごとに作り直すので、前のテストの影響を受けません。
   */
  // @BeforeEach：「それぞれの@Testの前に、毎回このメソッドを実行してね」という目印
  @BeforeEach
  void before() {
    sut = new StudentConverter();
  }

  /**
   * 受講生1人と、その人のコース1つを渡したとき、
   * 受講生とコースがセットになった受講生詳細が1件作られることを確かめます。
   */
  // @Test：「このメソッドはテストだよ」という目印
  @Test
  void 受講生一覧とコース情報から_受講生ごとの詳細情報を作成できること() {
    // 【事前準備】受講生を1人と、その人のコースを１つ用意する
    Integer id = 999;
    Student student = new Student();
    student.setId(id);

    // コースの持ち主として、同じid（999）を studentId にセットする
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId(id);

    //【実行】受講生の一覧とコースの一覧を渡して、受講生詳細の一覧を作ってもらう
    List<StudentDetail> actual = sut.convertStudentDetails(List.of(student), List.of(studentCourse));

    // 【検証】assertThat(調べたい値)に続けて「こうなっているはず」を書く。違っていたらテストは失敗する
    // 受講生詳細が1件だけできている（hasSize(1)：要素数が1）
    assertThat(actual).hasSize(1);
    // 入れた受講生が、そのまま入っている（isEqualTo：同じ内容かどうか）
    assertThat(actual.get(0).getStudent()).isEqualTo(student);
    // コースの一覧に、用意したコースだけが入っている（containsExactly：ちょうどこの要素だけ）
    assertThat(actual.get(0).getStudentCourseList()).containsExactly(studentCourse);
  }

  /**
   * 他の受講生のコースが混ざっていても、
   * 受講生のidとコースの受講生idが一致するコースだけが割り当てられることを確かめます。
   */
  @Test
  void 受講生一覧とコース情報から_受講生IDが一致するコースだけが割り当てられること() {
    // 【事前準備】受講生１人と、その人のコース・別のコースを１つずつ用意する
    Integer id = 999;
    Integer otherId = 111; // 別の受講生のid

    Student student = new Student();
    student.setId(id);

    // この受講生（999）のコース
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setStudentId(id);

    // 別の受講生（111）のコース。999の人には割り当てられないはず
    StudentCourse otherStudentCourse = new StudentCourse();
    otherStudentCourse.setStudentId(otherId);

    //【実行】2つのコースを両方渡して、振り分けてもらう
    List<StudentDetail> actual = sut.convertStudentDetails(List.of(student), List.of(otherStudentCourse, studentCourse));

    //【検証】
    // 受講生は1人だけなので、受講生詳細も1件
    assertThat(actual).hasSize(1);
    assertThat(actual.get(0).getStudent()).isEqualTo(student);
    // 別の受講生のコース（otherStudentCourse）は入らず、自分のコースだけが入っている
    assertThat(actual.get(0).getStudentCourseList()).containsExactly(studentCourse);
  }
}

package raisetech.StudentManagementSystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import raisetech.StudentManagementSystem.controller.converter.StudentConverter;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.repository.StudentRepository;

/**
 * StudentService（業務処理）の単体テストです。
 * 本物のDBは使わず、Mockito（モック＝偽物の部品）でRepositoryとConverterを置き換えて、
 * 「Serviceが必要な部品を正しく呼び出しているか」を確認します。
 */
// @ExtendWith(MockitoExtension.class)：このテストでMockito（@Mockなど）を使えるようにする
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  // @Mock：本物ではなく、呼び出しの記録だけできる偽物のRepositoryを作る（DBには接続しない）
  @Mock
  private StudentRepository repository;

  // 同じく、偽物のConverterを作る
  @Mock
  private StudentConverter converter;

  // sut＝System Under Test（テスト対象）。ここではテストしたいStudentService
  private StudentService sut;

  // @BeforeEach：各テストの実行前に毎回呼ばれる準備処理
  @BeforeEach
  void before() {
    // 偽物のRepositoryとConverterを渡して、テスト対象のServiceを作る
    sut = new StudentService(repository, converter);
  }

  // @Test：このメソッドがテストであることを示す
  // （テスト名は日本語で「何をテストし、どうなればOKか」を表している）
  @Test
  void 受講生詳細の一覧検索_リポジトリとコンバーターの処理が適切に呼び出せていること() {
    // 【事前準備】偽物のRepositoryが返す値（空のリスト）を決めておく
    List<Student> studentList = new ArrayList<>();
    List<StudentCourse> studentCourseList = new ArrayList<>();
    // when(...).thenReturn(...)：「このメソッドが呼ばれたら、このリストを返す」という約束
    when(repository.search()).thenReturn(studentList);
    when(repository.searchAllStudentCoursesList()).thenReturn(studentCourseList);

    // 【実行】テストしたいメソッドを呼ぶ（戻り値の中身は、このテストでは確認しない）
    // List<StudentDetail> actual = sut.searchStudentList();
    sut.searchStudentList();

    // 【検証】Serviceが、各部品を正しく呼び出したかを確認する
    // Assertions.assertEquals(expected, actual);
    // ①Repositoryの受講生全件取得が、ちょうど1回呼ばれたこと
    verify(repository, times(1)).search();
    // ②Repositoryのコース全件取得が、ちょうど1回呼ばれたこと
    verify(repository, times(1)).searchAllStudentCoursesList();
    // ③Converterが、①②で取得したリストを受け取って、ちょうど1回呼ばれたこと
    verify(converter, times(1)).convertStudentDetails(studentList, studentCourseList);

    // 【後処理】本物のDBは使っていないので、元に戻す作業は不要
  }

  // 受講生1人分の検索（searchStudent）が、Repositoryを正しく呼び出し、
  // 受け取った結果をStudentDetailにまとめて返すことを確認するテスト
  @Test
  void 受講生詳細の検索_リポジトリの処理が適切に呼び出せていること() {
    // 【事前準備】テスト用のID（999）と、そのIDの受講生を用意する
    Integer id = 999;
    // ①テスト用の受講生を作り、IDをセットする
    //   （Service内で student.getId() を使うため、IDが必要）
    Student student = new Student();
    student.setId(id);
    // ②コース一覧は空のリストにしておく（このテストでは中身は重要ではない）
    List<StudentCourse> studentCourseList = new ArrayList<>();
    // ③偽物のRepositoryに「ID 999で受講生を検索したら、①の受講生を返す」と約束させる
    when(repository.searchStudent(id)).thenReturn(student);
    // ④同じく「ID 999でコースを検索したら、②のコース一覧を返す」と約束させる
    when(repository.searchStudentCourseList(id)).thenReturn(studentCourseList);

    // 【実行】テストしたいメソッドを呼び、戻り値（actual＝実際の結果）を受け取る
    StudentDetail actual = sut.searchStudent(id);

    // 【検証】
    // ①受講生の検索が、ID 999 で1回呼ばれたこと
    verify(repository, times(1)).searchStudent(id);
    // ②コース情報の検索が、受講生のID 999 で１回呼ばれたこと
    verify(repository, times(1)).searchStudentCourseList(id);
    // ③戻り値に、Repositoryから受け取った受講生とコース一覧が入っていること
    //   （assertEquals(期待する値, 実際の値)：同じなら合格、違えば失敗）
    assertEquals(student, actual.getStudent());
    assertEquals(studentCourseList, actual.getStudentCourseList());
  }

  // 受講生の新規登録（registerStudent）が、受講生とコースをRepositoryに登録し、
  // 削除フラグを未削除（false）にすることを確認するテスト
  @Test
  void 受講生の登録_リポジトリの処理が適切に呼び出せていること() {
    // 【事前準備】受講生１人とコース１件を持つ受講生詳細を作る
    // ①テスト用の受講生を作る
    Student student = new Student();
    // ②削除フラグをわざと true にして、Serviceが false に上書きするかを確かめる
    student.setIsDeleted(true);
    // ③テスト用のコースを1件作る（開始・終了日時などはServiceがセットするので、ここでは空のまま）
    StudentCourse studentCourse = new StudentCourse();
    // ④受講生とコース一覧（1件だけのリスト）を1セットにして、登録する受講生詳細にする
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));

    // 【実行】テストしたいメソッド（受講生詳細の登録）を呼ぶ
    // （Repositoryは偽物なので、DBには何も登録されない）
    sut.registerStudent(studentDetail);

    // 【検証】
    // ①受講生の登録が、この受講生で１回呼ばれたこと
    verify(repository, times(1)).registerStudent(student);
    // ②コースの登録が1回呼ばれたこと（コースが1件なので1回）
    verify(repository, times(1)).registerStudentCourse(studentCourse);
    // ③削除フラグがfalse(未削除）に上書きされていること
    //   （assertFalse：値がfalseなら合格。②でtrueにした値が、Serviceでfalseに変わったことを確認する）
    assertFalse(student.getIsDeleted());
  }

  // 受講生情報の更新（updateStudent）が、受講生とコースの更新をRepositoryに依頼することを確認するテスト
  @Test
  void 受講生詳細の更新_リポジトリの処理が適切に呼び出せていること() {
    // 【事前準備】受講生1人とコース1件を持つ受講生詳細を作る
    // ①テスト用の受講生を作る（更新の内容は、このテストでは重要ではないので空のまま）
    Student student = new Student();
    // ②テスト用のコースを1件作る
    StudentCourse studentCourse = new StudentCourse();
    // ③受講生とコース一覧（1件だけのリスト）を1セットにして、更新する受講生詳細にする
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));

    // 【実行】テストしたいメソッド（受講生詳細の更新）を呼ぶ
    // （Repositoryは偽物なので、DBは何も更新されない）
    sut.updateStudent(studentDetail);

    // 【検証】
    // ①受講生の更新が、この受講生で1回呼ばれたこと
    verify(repository, times(1)).updateStudent(student);
    // ②コースの更新が、このコースで1回呼ばれたこと（コースが1件なので1回）
    verify(repository, times(1)).updateStudentCourse(studentCourse);
  }

  // コース情報の初期化（initStudentCourse）が、受講生ID・開始日時・終了日時をセットすることを確認するテスト
  // （このメソッドをテストから呼べるよう、StudentService側では private を付けていない）
  @Test
  void 受講生コースの情報の初期化_受講生IDとコース開始日時と終了日時が設定されること() {
    // 【事前準備】ID999の受講生と、空のコース情報を準備する
    // ①ID 999 の受講生を作る（コースに、この受講生のIDがセットされるはず）
    Student student = new Student();
    student.setId(999);
    // ②何も値が入っていない、空のコースを作る（これをServiceが初期化する）
    StudentCourse studentCourse = new StudentCourse();

    // 【実行】実行の直前と直後の日時を記録しておく
    // （開始日時は「実行した瞬間の今」なので、この2つの日時の間に入るはず）
    LocalDateTime before = LocalDateTime.now();
    // コースに、受講生ID・開始日時・終了日時をセットする
    sut.initStudentCourse(studentCourse, student);
    LocalDateTime after = LocalDateTime.now();

    // 【検証】
    // ①受講生IDがセットされていること
    assertEquals(999, studentCourse.getStudentId());
    // ②開始日時が、実行の直前～直後の間に入っていること
    //   （「直前より前ではない」かつ「直後より後ではない」＝間に入っている）
    LocalDateTime startAt = studentCourse.getCourseStartAt();
    Assertions.assertTrue(!startAt.isBefore(before) && !startAt.isAfter(after));
    // ③終了日時が、開始日時のちょうど1年後であること
    assertEquals(startAt.plusYears(1), studentCourse.getCourseEndAt());
  }
}

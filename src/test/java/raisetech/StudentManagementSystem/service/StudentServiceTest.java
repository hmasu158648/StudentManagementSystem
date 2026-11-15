package raisetech.StudentManagementSystem.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
  void 受講生詳細の登録_受講生とコースが登録され_コースに受講生IDと開始日時と終了日時が設定されること() {
    // 【事前準備】
    // ID 999 の受講生（DBで採番されたIDの代わりに、あらかじめセットしておく）
    Student student = new Student();
    student.setId(999);
    student.setIsDeleted(true); // わざと true にして、false に上書きされるか確かめる
    StudentCourse studentCourse = new StudentCourse();
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));

    // 【実行】実行の直前と直後の時刻を記録しておく
    LocalDateTime before = LocalDateTime.now();
    sut.registerStudent(studentDetail);
    LocalDateTime after = LocalDateTime.now();

    // 【検証】①受講生とコースの登録が1回ずつ呼ばれたこと
    verify(repository, times(1)).registerStudent(student);
    verify(repository, times(1)).registerStudentCourse(studentCourse);
    // ②削除フラグが false（未削除）に上書きされていること
    assertFalse(student.getIsDeleted());
    // ③コースに受講生IDがセットされていること
    assertEquals(999, studentCourse.getStudentId());
    // ④開始日時が、実行の直前〜直後の間に入っていること
    LocalDateTime startAt = studentCourse.getCourseStartAt();
    assertTrue(!startAt.isBefore(before) && !startAt.isAfter(after));
    // ⑤終了日時が、開始日時のちょうど1年後であること
    assertEquals(startAt.plusYears(1), studentCourse.getCourseEndAt());
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

  @Test
  void 受講生詳細の一覧検索_受講生の全件取得で例外が発生した場合_例外が投げられ後続の処理は呼ばれないこと() {
    // 【事前準備】受講生の全件取得で、DBエラーが起きたことにする
    when(repository.search()).thenThrow(new RuntimeException("DBエラー"));

    // 【実行・検証】①例外がそのまま呼び出し元に投げられること
    assertThrows(RuntimeException.class, () -> sut.searchStudentList());
    // ②途中で止まるため、コース取得とコンバーターは呼ばれないこと
    verify(repository, never()).searchAllStudentCoursesList();
    verify(converter, never()).convertStudentDetails(any(), any());
  }

  @Test
  void 受講生詳細の検索_存在しないIDの場合_NullPointerExceptionが発生しコース検索は呼ばれないこと() {
    // 【事前準備】存在しないIDのため、Repositoryが null を返すことにする
    Integer id = 999;
    when(repository.searchStudent(id)).thenReturn(null);

    // 【実行・検証】①現状の実装では NullPointerException が発生すること
    //   （今後、存在しないIDの例外処理を追加する際に期待値を変更する予定）
    assertThrows(NullPointerException.class, () -> sut.searchStudent(id));
    // ②コース検索は呼ばれないこと
    verify(repository, never()).searchStudentCourseList(any());
  }

  @Test
  void 受講生詳細の登録_受講生の登録で例外が発生した場合_例外が呼び出し元へ投げられコースは登録されないこと() {
    // 【事前準備】
    Student student = new Student();
    StudentCourse studentCourse = new StudentCourse();
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));
    // 受講生の登録で、DBエラーが起きたことにする（戻り値のないメソッドは doThrow を使う）
    doThrow(new RuntimeException("DBエラー")).when(repository).registerStudent(student);

    // 【実行・検証】①例外がそのまま呼び出し元に投げられること
    assertThrows(RuntimeException.class, () -> sut.registerStudent(studentDetail));
    // ②コースの登録は呼ばれないこと
    verify(repository, never()).registerStudentCourse(any());
  }

  @Test
  void 受講生詳細の更新_受講生の更新で例外が発生した場合_例外が呼び出し元へ投げられコースは更新されないこと() {
    // 【事前準備】
    Student student = new Student();
    StudentCourse studentCourse = new StudentCourse();
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));
    // 受講生の更新で、DBエラーが起きたことにする
    doThrow(new RuntimeException("DBエラー")).when(repository).updateStudent(student);

    // 【実行・検証】①例外がそのまま呼び出し元に投げられること
    assertThrows(RuntimeException.class, () -> sut.updateStudent(studentDetail));
    // ②コースの更新は呼ばれないこと
    verify(repository, never()).updateStudentCourse(any());
  }
}

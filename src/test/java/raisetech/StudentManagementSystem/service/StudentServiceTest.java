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
 *
 * 【たとえ話】Serviceは「作業をする人」、Repositoryは「倉庫（DB）の係」、Converterは「情報を整理する係」です。
 * 作業をする人が、必要な係に正しくお願いできているかを調べます。
 * 本物の倉庫は使わず、ものまね役（偽物）の係にお願いするので、DBがなくてもテストできます。
 */
// @ExtendWith(MockitoExtension.class)：「ものまね役（@Mock）をこのテストで使うよ」という目印
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

  // @Mock：本物ではなく、ものまね役（偽物）のRepositoryを作る目印。
  // 偽物はDBには接続しない。「何回呼ばれたか」の記録だけ残るので、あとで確認できる
  @Mock
  private StudentRepository repository;

  // 同じく、ものまね役（偽物）のConverterを作る
  @Mock
  private StudentConverter converter;

  // sut（サット）は System Under Test（＝テストされる対象）の略。
  // ここでは「テストしたいStudentService」のこと
  private StudentService sut;

  // @BeforeEach：「各テストが始まる前に、毎回これを実行してね」という目印（テスト前の準備係）
  @BeforeEach
  void before() {
    // 偽物のRepositoryとConverterを渡して、テストしたいServiceを新しく作る。
    // テストのたびに作り直すので、前のテストの影響を受けない
    sut = new StudentService(repository, converter);
  }

  // 受講生の一覧検索（searchStudentList）が、Repositoryに「受講生」と「コース」の全件をお願いし、
  // そのリストをConverterに渡していることを確認するテスト
  // @Test：「これはテストだよ」という目印（JUnitが自動で実行する）。
  // テスト名は日本語で「何をテストして、どうなればOKか」を表している
  @Test
  void 受講生詳細の一覧検索_リポジトリとコンバーターの処理が適切に呼び出せていること() {
    // 【事前準備】偽物のRepositoryが「返事」として返すリスト（今回は空っぽ）を用意する
    List<Student> studentList = new ArrayList<>();
    List<StudentCourse> studentCourseList = new ArrayList<>();
    // when(...).thenReturn(...)：「このメソッドが呼ばれたら、このリストを返してね」という約束。
    // 偽物は約束した返事しかできないので、先に決めておく
    when(repository.search()).thenReturn(studentList);
    when(repository.searchAllStudentCoursesList()).thenReturn(studentCourseList);

    // 【実行】テストしたいメソッドを呼ぶ（戻り値の中身は、このテストでは調べない）
    // 下の1行は、戻り値を受け取る場合の書き方の例（今は使わないのでコメントにしてある）
    // List<StudentDetail> actual = sut.searchStudentList();
    sut.searchStudentList();

    // 【検証】作業をする人（Service）が、各係に正しくお願いしたかを確認する
    // 下の1行は、値を比べる場合の書き方の例（今は使わないのでコメントにしてある）
    // Assertions.assertEquals(expected, actual);
    // ①Repositoryへの「受講生を全件ください」が、ちょうど1回呼ばれたこと（times(1)＝1回）
    verify(repository, times(1)).search();
    // ②Repositoryへの「コースを全件ください」が、ちょうど1回呼ばれたこと
    verify(repository, times(1)).searchAllStudentCoursesList();
    // ③Converterが、①②でもらったリストを受け取って、ちょうど1回呼ばれたこと
    verify(converter, times(1)).convertStudentDetails(studentList, studentCourseList);

    // 【後処理】本物のDBは使っていないので、あと片付け（元に戻す作業）は必要ない
  }

  // 受講生1人分の検索（searchStudent）が、Repositoryを正しく呼び出し、
  // 受け取った結果をStudentDetailにまとめて返すことを確認するテスト
  @Test
  void 受講生詳細の検索_リポジトリの処理が適切に呼び出せていること() {
    // 【事前準備】テスト用のID（999番）と、その番号の受講生を用意する
    Integer id = 999;
    // ①テスト用の受講生を作り、番号（ID）をつける
    //   （Serviceの中で student.getId() を使うので、IDが入っていないと困る）
    Student student = new Student();
    student.setId(id);
    // ②コース一覧は空っぽのリストにしておく（このテストでは中身は大事ではない）
    List<StudentCourse> studentCourseList = new ArrayList<>();
    // ③偽物のRepositoryに「999番の受講生を探されたら、①の受講生を返してね」と約束させる
    when(repository.searchStudent(id)).thenReturn(student);
    // ④同じく「999番のコースを探されたら、②のコース一覧を返してね」と約束させる
    when(repository.searchStudentCourseList(id)).thenReturn(studentCourseList);

    // 【実行】テストしたいメソッドを呼んで、戻り値（actual＝実際の結果）を受け取る
    StudentDetail actual = sut.searchStudent(id);

    // 【検証】
    // ①受講生の検索が、999番で1回呼ばれたこと
    verify(repository, times(1)).searchStudent(id);
    // ②コースの検索が、受講生の番号999で1回呼ばれたこと
    verify(repository, times(1)).searchStudentCourseList(id);
    // ③戻り値に、Repositoryからもらった受講生とコース一覧が入っていること
    //   （assertEquals(期待する値, 実際の値)：同じなら合格、ちがえば失敗）
    assertEquals(student, actual.getStudent());
    assertEquals(studentCourseList, actual.getStudentCourseList());
  }

  // 受講生の新規登録（registerStudent）が、受講生とコースをRepositoryに登録し、
  // 削除フラグを未削除（false）にすることを確認するテスト
  @Test
  void 受講生詳細の登録_受講生とコースが登録され_コースに受講生IDと開始日時と終了日時が設定されること() {
    // 【事前準備】
    // 999番の受講生を用意する（本物ならDBが自動でつける番号を、先に自分でセットしておく）
    Student student = new Student();
    student.setId(999);
    // 削除フラグ（「削除済み」の印）を、わざと true（削除済み）にしておく。
    // 登録のあとに false（未削除）へ書きかわるかを確かめるため
    student.setIsDeleted(true);
    StudentCourse studentCourse = new StudentCourse();
    // 受講生とコース（1件）を1セットにして、登録する受講生詳細にする
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));

    // 【実行】登録の直前と直後の時刻をメモしておく（開始日時が「登録した時刻」かを、あとで調べるため）
    LocalDateTime before = LocalDateTime.now();
    sut.registerStudent(studentDetail);
    LocalDateTime after = LocalDateTime.now();

    // 【検証】①受講生とコースの登録が、それぞれ1回ずつ呼ばれたこと
    verify(repository, times(1)).registerStudent(student);
    verify(repository, times(1)).registerStudentCourse(studentCourse);
    // ②削除フラグが false（未削除）に書きかわっていること
    assertFalse(student.getIsDeleted());
    // ③コースに、受講生の番号（999）がセットされていること
    assertEquals(999, studentCourse.getStudentId());
    // ④開始日時が、実行の直前〜直後の間に入っていること（＝登録した時刻になっている）
    //   （isBefore＝より前、isAfter＝より後。「前でも後でもない」＝その間）
    LocalDateTime startAt = studentCourse.getCourseStartAt();
    assertTrue(!startAt.isBefore(before) && !startAt.isAfter(after));
    // ⑤終了日時が、開始日時のちょうど1年後であること（plusYears(1)＝1年足す）
    assertEquals(startAt.plusYears(1), studentCourse.getCourseEndAt());
  }

  // 受講生情報の更新（updateStudent）が、受講生とコースの更新をRepositoryに依頼することを確認するテスト
  @Test
  void 受講生詳細の更新_リポジトリの処理が適切に呼び出せていること() {
    // 【事前準備】受講生1人とコース1件を持つ受講生詳細を作る
    // ①テスト用の受講生を作る（更新する中身は、このテストでは大事ではないので空のまま）
    Student student = new Student();
    // ②テスト用のコースを1件作る
    StudentCourse studentCourse = new StudentCourse();
    // ③受講生とコース一覧（1件だけのリスト）を1セットにして、更新する受講生詳細にする
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));

    // 【実行】テストしたいメソッド（受講生詳細の更新）を呼ぶ
    // （Repositoryは偽物なので、本物のDBは何も変わらない）
    sut.updateStudent(studentDetail);

    // 【検証】
    // ①受講生の更新が、この受講生で1回呼ばれたこと
    verify(repository, times(1)).updateStudent(student);
    // ②コースの更新が、このコースで1回呼ばれたこと（コースは1件なので1回だけ）
    verify(repository, times(1)).updateStudentCourse(studentCourse);
  }

  // 【異常系＝うまくいかない場合のテスト】
  // 一覧検索（searchStudentList）の途中でDBのエラー（例外）が起きたとき、
  // エラーがそのまま呼び出し元へ伝わり、そのあとの作業（コース取得・変換）に進まないことを確認するテスト
  // （例外＝「問題が起きたよ！」という知らせ。出ると、その場で処理が止まる）
  @Test
  void 受講生詳細の一覧検索_受講生の全件取得で例外が発生した場合_例外が投げられ後続の処理は呼ばれないこと() {
    // 【事前準備】受講生の全件取得で、DBエラーが起きたことにする
    // （thenThrow：「このメソッドが呼ばれたら、値を返す代わりに、エラーを出してね」という約束）
    when(repository.search()).thenThrow(new RuntimeException("DBエラー"));

    // 【実行・検証】①エラーがそのまま呼び出し元に投げられること
    // （assertThrows：→の右側（ラムダ式）を実行して、指定した種類のエラーが出れば合格）
    assertThrows(RuntimeException.class, () -> sut.searchStudentList());
    // ②途中で止まるため、コース取得とConverterは呼ばれていないこと
    // （never()：1回も呼ばれていない。any()：どんな引数でもOK、という意味）
    verify(repository, never()).searchAllStudentCoursesList();
    verify(converter, never()).convertStudentDetails(any(), any());
  }

  // 【異常系】存在しない番号（ID）で検索（searchStudent）すると、
  // 今のプログラムでは NullPointerException（ヌルポ）というエラーになることを確認するテスト
  // （null＝「何も入っていない」状態。見つからなかった受講生がnullのまま student.getId() を使うため、エラーになる。
  //  将来「見つかりません」という専用のエラー処理を入れたら、このテストも書きかえる）
  @Test
  void 受講生詳細の検索_存在しないIDの場合_NullPointerExceptionが発生しコース検索は呼ばれないこと() {
    // 【事前準備】存在しない番号なので、偽物のRepositoryが null（見つからなかった）を返すことにする
    Integer id = 999;
    when(repository.searchStudent(id)).thenReturn(null);

    // 【実行・検証】①今のプログラムでは NullPointerException が出ること
    //   （あとでエラー処理を追加したら、ここで期待するエラーの種類を変える予定）
    assertThrows(NullPointerException.class, () -> sut.searchStudent(id));
    // ②エラーで止まるので、コースの検索は呼ばれていないこと
    verify(repository, never()).searchStudentCourseList(any());
  }

  // 【異常系】新規登録（registerStudent）で受講生の登録に失敗したとき、
  // エラーが呼び出し元へ伝わり、コースの登録には進まないことを確認するテスト
  // （※@Transactionalによる「登録の取り消し」はSpringの機能なので、この偽物を使ったテストでは確認できない）
  @Test
  void 受講生詳細の登録_受講生の登録で例外が発生した場合_例外が呼び出し元へ投げられコースは登録されないこと() {
    // 【事前準備】登録する受講生とコース（1件）を1セットにして用意する
    Student student = new Student();
    StudentCourse studentCourse = new StudentCourse();
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));
    // 受講生の登録で、DBエラーが起きたことにする
    // （戻り値のないメソッドに「エラーを出してね」と約束するときは、thenThrowではなく doThrow を使う）
    doThrow(new RuntimeException("DBエラー")).when(repository).registerStudent(student);

    // 【実行・検証】①エラーがそのまま呼び出し元に投げられること
    assertThrows(RuntimeException.class, () -> sut.registerStudent(studentDetail));
    // ②受講生の登録でエラーになって止まるので、コースの登録は呼ばれていないこと
    verify(repository, never()).registerStudentCourse(any());
  }

  // 【異常系】更新（updateStudent）で受講生の更新に失敗したとき、
  // エラーが呼び出し元へ伝わり、コースの更新には進まないことを確認するテスト
  @Test
  void 受講生詳細の更新_受講生の更新で例外が発生した場合_例外が呼び出し元へ投げられコースは更新されないこと() {
    // 【事前準備】更新する受講生とコース（1件）を1セットにして用意する
    Student student = new Student();
    StudentCourse studentCourse = new StudentCourse();
    StudentDetail studentDetail = new StudentDetail(student, List.of(studentCourse));
    // 受講生の更新で、DBエラーが起きたことにする（戻り値がないので doThrow を使う）
    doThrow(new RuntimeException("DBエラー")).when(repository).updateStudent(student);

    // 【実行・検証】①エラーがそのまま呼び出し元に投げられること
    assertThrows(RuntimeException.class, () -> sut.updateStudent(studentDetail));
    // ②受講生の更新でエラーになって止まるので、コースの更新は呼ばれていないこと
    verify(repository, never()).updateStudentCourse(any());
  }
}

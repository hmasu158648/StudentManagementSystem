package raisetech.StudentManagementSystem.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagementSystem.controller.converter.StudentConverter;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.repository.StudentRepository;

/**
 * 受講生に関する業務処理（検索・新規登録・更新）を担当するサービスクラスです。
 * コントローラーからの依頼を受け、リポジトリ（データベース操作）を組み合わせて処理します。
 * コントローラー＝受付係、サービス＝実際の作業をする担当者、リポジトリ＝データベースとやり取りする係、というイメージです。
 */
@Service // Springが管理するサービスとして登録する（コントローラーから自動で使えるようになる）
public class StudentService {

  // データベースを操作するリポジトリ。一度セットしたら変更しないのでfinalにしている
  private final StudentRepository repository;
  // 受講生とコース情報をStudentDetailの形に変換するConverter（同じくSpringが注入する）
  private final StudentConverter converter;

  /**
   * コンストラクターです。Springが、使うリポジトリとConverterを自動で渡してくれます（コンストラクターインジェクション）。
   *
   * @param repository       受講生情報を扱うリポジトリ
   * @param studentConverter 受講生詳細への変換を行うConverter
   */
  @Autowired // 「必要な部品（リポジトリとConverter）をSpringが探して渡してね」という目印
  public StudentService(StudentRepository repository, StudentConverter studentConverter) {
    // ①受け取ったリポジトリを、このクラスのフィールドに保存して、以降のメソッドで使えるようにする
    this.repository = repository;
    // ②受け取ったConverterも同じようにフィールドに保存する
    this.converter = studentConverter;
  }

  /**
   * 受講生の一覧を全件取得し、受講生ごとのコース情報をまとめた受講生詳細の一覧として返します。
   *
   * @return 受講生詳細の一覧（全件）
   */
  public List<StudentDetail> searchStudentList() {
    // ①リポジトリに依頼して、データベースから受講生を全件取得する
    List<Student> studentList = repository.search();
    // ②リポジトリに依頼して、データベースから全受講生分のコース情報を全件取得する
    List<StudentCourse> studentsCourseList = repository.searchAllStudentCoursesList();
    // ③converterで、受講生ごとにコース情報を紐づけてList<StudentDetail>の形に整える
    // ④整えた一覧をそのまま呼び出し元（コントローラー）へ返す
    return converter.convertStudentDetails(studentList, studentsCourseList);
  }

  /**
   * 受講生IDをもとに、受講生情報とその受講生に紐づくコース情報を検索し、
   * 受講生詳細（StudentDetail）としてまとめて返します。
   *
   * @param id 検索対象の受講生ID
   * @return 受講生情報とコース情報をまとめた受講生詳細
   */
  public StudentDetail searchStudent(Integer id) {
    // ①引数のIDに一致する受講生情報をデータベースから取得し、studentに格納する
    Student student = repository.searchStudent(id);
    // ②受講生のIDを使って、その受講生のコース情報一覧を取得する
    // （※該当する受講生がいないと student は null になり、次の student.getId() でエラーになる）
    List<StudentCourse> studentsCourseList = repository.searchStudentCourseList(student.getId());
    // ③取得した受講生情報(student)とコース情報一覧(studentsCourseList)を、コンストラクターに渡して
    //   StudentDetailのインスタンスを作成する（以前のようにsetterで1つずつセットする必要がない）
    // ④作成したStudentDetail（受講生＋その人のコース）を呼び出し元（コントローラー）へ返す
    return new StudentDetail(student, studentsCourseList);
  }

  /**
   * 受講生とコース情報を新規登録します。
   * 受講生を登録したあと、採番された受講生ID・開始日時（現在日時）・終了日時（1年後）を各コースにセットして登録します。
   * 処理の途中でエラーが発生した場合は、すべての登録を取り消します。
   *
   * @param studentDetail 登録する受講生情報とコース情報
   * @return 登録後の受講生詳細（採番された受講生idや開始日時がセットされている）
   */
  @Transactional
  // 全部成功したら確定、途中で失敗したら最初の状態に戻す
  // （受講生だけ登録されて、コースが登録されない状態を防ぐ）
  public StudentDetail registerStudent(StudentDetail studentDetail) {
    // 【流れ】受講生を登録 → 採番されたidを取得 → 各コースにid・開始日時・終了日時をセットして登録
    // 1-1. 受講生詳細から、受講生情報(student)を取り出しておく（この後で何度も使うため）
    Student student = studentDetail.getStudent();

    // 1-2. 受講生を登録する。登録すると、データベースが決めたid（自動採番）がstudentにセットされる
    repository.registerStudent(student);
    // 2. コース情報を1件ずつ取り出して、forEach（ラムダ式）で同じ処理を繰り返す
    //    （studentCourseが、取り出した1件のコース。コースの数だけ2-1・2-2が実行される）
    studentDetail.getStudentCourseList().forEach(studentCourse -> {
      // 2-1. コースに、受講生id・開始日時・終了日時をセットする（詳細は下のinitStudentDetailメソッド）
      initStudentDetail(studentCourse, student);
      // 2-2. 必要な値がそろったコースを、リポジトリを通してデータベースに登録する
      repository.registerStudentCourse(studentCourse);
    });

    // 3. id・開始日時・終了日時がセットされた登録後の受講生詳細を、呼び出し元（コントローラー）へ返す
    return studentDetail;
  }

  /**
   * 新規登録するコース情報に、受講生idと開始日時・終了日時を初期値としてセットします。
   *
   * @param studentCourse 初期値をセットするコース情報
   * @param student       コースの持ち主である受講生（登録済みで、idが採番されている）
   */
  private void initStudentDetail(StudentCourse studentCourse, Student student) {
    // (1) 今の日時を1回だけ取得しておく（開始日時と終了日時で同じ「今」を使うため）
    LocalDateTime now = LocalDateTime.now();

    // (2) どの受講生のコースかを示すため、受講生の id をセットする
    studentCourse.setStudentId(student.getId());
    // (3) 開始日時に登録時点の日時（今）をセットする
    studentCourse.setCourseStartAt(now);
    // (4) 終了日時に「今から1年後」の日時をセットする（plusYears(1)で1年分進める）
    studentCourse.setCourseEndAt(now.plusYears(1));
  }

  /**
   * １人の受講生情報と、その受講生に紐づくコース情報を更新します。
   * 処理の途中でエラーが発生した場合は、すべての更新を取り消します。
   *
   * @param studentDetail 更新する受講生情報とコース情報
   */
  @Transactional // 受講生とコースの更新を1セットとして扱い、途中で失敗したら全部元に戻す
  public void updateStudent(StudentDetail studentDetail) {
    // 【流れ】受講生情報を更新 → コース情報を1件ずつ更新（どちらかが失敗したら全部元に戻る）
    // ①studentDetailからstudentオブジェクトを取り出して受講生情報を更新する（idが一致する受講生が更新される）
    repository.updateStudent(studentDetail.getStudent());
    // ②コース情報の一覧を、forEachで1件ずつ取り出して処理する
    // ③取り出した1件ごとに repository.updateStudentCourse を呼び、データベースに反映する
    //   （「repository::updateStudentCourse」はメソッド参照で、
    //     「course -> repository.updateStudentCourse(course)」と同じ意味。コースごとのidで更新対象を特定する）
    studentDetail.getStudentCourseList().forEach(repository::updateStudentCourse);
  }
}

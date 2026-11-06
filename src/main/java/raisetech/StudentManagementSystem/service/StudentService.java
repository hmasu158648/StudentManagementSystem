package raisetech.StudentManagementSystem.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
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

  /**
   * コンストラクターです。Springが、使うリポジトリを自動で渡してくれます（コンストラクターインジェクション）。
   *
   * @param repository 受講生情報を扱うリポジトリ
   */
  @Autowired // 「必要な部品（リポジトリ）をSpringが探して渡してね」という目印
  public StudentService(StudentRepository repository) {
    // 受け取ったリポジトリを、このクラスのフィールドに保存して、以降のメソッドで使えるようにする
    this.repository = repository;
  }

  /**
   * 受講生の一覧を全件取得します。
   *
   * @return 受講生一覧（全件）
   */
  public List<Student> searchStudentList() {
    // ①リポジトリに依頼して、データベースから受講生を全件取得する
    // ②取得した結果をそのまま呼び出し元（コントローラー）へ返す
    return repository.search();
  }

  /**
   * 受講生コース情報を全件取得します（全受講生分）。
   *
   * @return 受講生コース情報一覧（全件）
   */
  public List<StudentsCourses> searchStudentCourseList() {
    // ①リポジトリに依頼して、データベースから受講生コース情報を全件取得する
    // ②取得した結果をそのまま呼び出し元（コントローラー）へ返す
    return repository.searchStudentCoursesList();
  }

  /**
   * 受講生とコース情報を新規登録します。
   * 受講生を登録したあと、採番された受講生IDと開始日時（現在日時）を各コースにセットして登録します。
   * 処理の途中でエラーが発生した場合は、すべての登録を取り消します。
   *
   * @param studentDetail 登録する受講生情報とコース情報
   */
  @Transactional // 全部成功したら確定、途中で失敗したら最初の状態に戻す（受講生だけ登録されて、コースが登録されない状態を防ぐ）
  public void registerStudent(StudentDetail studentDetail) {
    // 【流れ】受講生を登録 → 採番されたidを取得 → 各コースにidと開始日時をセットして登録
    // 1. まず受講生を登録する。登録すると、データベースが決めたid（自動採番）が受講生オブジェクトにセットされる
    repository.registerStudent(studentDetail.getStudent());
    // コースを1件ずつ取り出して、同じ処理を繰り返す（コースの数だけ2-1〜2-3が実行される）
    for (StudentsCourses studentCourses : studentDetail.getStudentsCourses()) {
      // 2-1. どの受講生のコースかを示すため、受講生の id をセットする
      studentCourses.setStudentId(studentDetail.getStudent().getId());
      // 2-2. 開始日に登録日（今の日時）をセットする（終了日は空のまま）
      studentCourses.setCourseStartAt(LocalDateTime.now());
      // 2-3. id・開始日時がそろったコースを、リポジトリを通してデータベースに登録する
      repository.registerStudentCourse(studentCourses);
    }
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
    List<StudentsCourses> studentsCourses =
        repository.searchStudentCourses(student.getId());

    // ③StudentDetailクラスのインスタンスを作成する
    StudentDetail studentDetail = new StudentDetail();

    // ④上記で作成したインスタンスのフィールドに値をセットする
    studentDetail.setStudent(student);
    studentDetail.setStudentsCourses(studentsCourses);

    // ⑤StudentDetailクラスのインスタンスを返す
    return studentDetail;
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
    // ②コース情報を1件ずつ取り出して更新する（コースごとのidで、更新するコースを特定する）
    for (StudentsCourses studentsCourses : studentDetail.getStudentsCourses()) {
      // ③取り出した1件のコース情報をデータベースに反映する
      repository.updateStudentCourse(studentsCourses);
    }
  }
}

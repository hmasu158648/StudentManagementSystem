package raisetech.StudentManagementSystem.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagementSystem.controller.converter.StudentConverter;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.service.StudentService;

/**
 * 受講生の一覧取得・新規登録・更新に関するリクエストを受け付けるRESTコントローラーです。
 * 処理結果は画面（テンプレート）ではなく、JSONなどのレスポンスボディとして返します。
 */
// http://localhost:8080
@RequiredArgsConstructor // Lombokが代わりにfinalフィールドのコンストラクターを作成してくれる。
// @RestController：このクラスのメソッドの戻り値を、画面ではなくレスポンスボディ（JSONや文字列）として返す
@RestController
public class StudentController {

  // 受講生・コース情報の検索や登録・更新を行うService（コンストラクター経由でSpringが注入する）
  private final StudentService service;
  // 受講生とコース情報をStudentDetailの形に変換するConverter（同じく注入される）
  private final StudentConverter converter;

  /**
   * 受講生一覧を取得します。
   * 受講生とコース情報を全件取得し、受講生ごとの詳細情報にまとめて返します。
   *
   * @return 受講生詳細の一覧（レスポンスボディとして返されます）
   */
  // 【受講生一覧】http://localhost:8080/studentList にGETリクエストすると呼ばれる
  // 引数は不要（一覧は全件取得するため、リクエストから受け取る値がない）
  @GetMapping("/studentList")
  public List<StudentDetail> getStudentList() {
    // ① Serviceを通して、受講生を全件取得する
    List<Student> students = service.searchStudentList();
    // ② Serviceを通して、受講生コース情報を全件取得する
    List<StudentsCourses> studentCourses = service.searchStudentCourseList();

    // ③ converterで、受講生ごとにコース情報を紐づけてList<StudentDetail>の形に整える
    // ④ 整えた一覧をそのまま返す（JSONに変換されてレスポンスボディになる）
    return converter.convertStudentDetails(students, studentCourses);
  }

  // ===== 受講生の新規登録（GET と POST の2歩で行う） =====

  /**
   * 受講生の新規登録用の初期データを用意します。
   * 入力欄が空の受講生詳細（受講生1人・コース1件）を用意してModelに格納します。
   * なお、@RestControllerのため、戻り値の文字列は画面名ではなくレスポンスボディの文字列としてそのまま返されます。
   *
   * @param model 受講生詳細を格納するModel
   * @return 文字列"registerStudent"（レスポンスボディとして返されます）
   */
  // ① http://localhost:8080/newStudent にGETリクエストすると、このメソッドが呼ばれる
  @GetMapping("/newStudent")
  public String newStudent(Model model) {
    // ② 白紙の申込用紙を用意してModelに格納する
    StudentDetail studentDetail = new StudentDetail();               // 申込用紙を1枚用意する
    studentDetail.setStudent(new Student());                          // 空の受講生欄を入れる
    studentDetail.setStudentsCourses(List.of(new StudentsCourses())); // 空のコース用紙を1枚入れた棚を申込用紙に入れる
    // ③ 用意した申込用紙を"studentDetail"という名前でModelに入れる
    model.addAttribute("studentDetail", studentDetail);
    // ④ @RestControllerのためテンプレートは表示されず、文字列がそのままレスポンスボディになる
    return "registerStudent";
  }

  /**
   * 入力された受講生情報を新規登録します。
   * リクエストボディ（JSON）で受け取った受講生詳細をもとに、受講生情報とコース情報を新規登録します。
   *
   * @param studentDetail リクエストボディから受け取った受講生詳細
   * @return 登録処理の成功を示すメッセージ（ステータス200）
   */
  // ⑤ POSTリクエスト（/registerStudent）が送信されると、このメソッドが呼ばれる
  //    リクエストボディのJSONは、@RequestBodyによってSpring Bootが studentDetail に自動で詰め込んでくれる
  @PostMapping("/registerStudent")
  public ResponseEntity<String> registerStudent(@RequestBody StudentDetail studentDetail) {
    // ⑥ Serviceを通して、新規受講生情報を登録する
    service.registerStudent(studentDetail);
    // ⑦ 登録が成功したことを示すメッセージを、ステータス200で返す
    return ResponseEntity.ok("新規登録処理が成功しました");
  }

  /**
   * 入力された内容で受講生情報を更新します。
   * リクエストボディ（JSON）で受け取った受講生詳細をもとに、受講生情報とコース情報を更新します。
   *
   * @param studentDetail リクエストボディから受け取った受講生詳細
   * @return 更新処理の成功を示すメッセージ（ステータス200）
   */
  // ① POSTリクエスト（/updateStudent）が送信されると、このメソッドが呼ばれる
  //    リクエストボディのJSONは、@RequestBodyによって studentDetail に自動で詰め込まれる
  @PostMapping("/updateStudent")
  public ResponseEntity<String> updateStudent(@RequestBody StudentDetail studentDetail) {
    // ② Serviceを通して、受講生情報とコース情報を更新する
    service.updateStudent(studentDetail);
    // ③ 更新が成功したことを示すメッセージを、ステータス200で返す
    return ResponseEntity.ok("更新処理が成功しました");
  }
}

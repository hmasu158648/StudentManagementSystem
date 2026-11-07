package raisetech.StudentManagementSystem.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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

  /**
   * 受講生一覧を取得します。
   * 受講生とコース情報を全件取得し、受講生ごとの詳細情報にまとめて返します。
   *
   * @return 受講生詳細の一覧（レスポンスボディとして返されます）
   */
  // ① 【受講生一覧】http://localhost:8080/studentList にGETリクエストすると呼ばれる
  //    引数は不要（一覧は全件取得するため、リクエストから受け取る値がない）
  @GetMapping("/studentList")
  public List<StudentDetail> getStudentList() {
    // ② Serviceに依頼して、受講生とコース情報をまとめた受講生詳細の一覧を受け取る
    // ③ 受け取った一覧をそのまま返す（JSONに変換されてレスポンスボディになる）
    return service.searchStudentList();
  }

  /**
   * 受講生を1人検索します。
   * URLで指定されたIDの受講生情報とコース情報を、受講生詳細としてまとめて返します。
   *
   * @param id URLに含まれる受講生ID
   * @return 該当する受講生詳細（レスポンスボディとして返されます）
   */
  // ① 例：http://localhost:8080/student/1 にGETリクエストすると呼ばれ、URLの「1」がidに入る
  //    （@PathVariableが、URLの{id}部分を引数idに取り出してくれる）
  @GetMapping("/student/{id}")
  public StudentDetail getStudent(@PathVariable Integer id) {
    // ② Serviceを通して、idに一致する受講生とそのコース情報を検索する
    // ③ 検索結果の受講生詳細をそのまま返す（JSONに変換されてレスポンスボディになる）
    return service.searchStudent(id);
  }

  /**
   * 入力された受講生情報を新規登録します。
   * リクエストボディ（JSON）で受け取った受講生詳細をもとに、受講生情報とコース情報を新規登録します。
   *
   * @param studentDetail リクエストボディから受け取った受講生詳細
   * @return 登録した受講生詳細（ステータス200）
   */
  // ① POSTリクエスト（/registerStudent）が送信されると、このメソッドが呼ばれる
  //    リクエストボディのJSONは、@RequestBodyによってSpring Bootが studentDetail に自動で詰め込んでくれる
  @PostMapping("/registerStudent")
  public ResponseEntity<StudentDetail> registerStudent(@RequestBody StudentDetail studentDetail) {
    // ② Serviceを通して、新規受講生情報を登録し、登録後の受講生詳細（採番されたidなどを含む）を受け取る
    StudentDetail responseStudentDetail = service.registerStudent(studentDetail);
    // ③ 登録後の受講生詳細を、ステータス200（成功）のレスポンスボディとして返す
    return ResponseEntity.ok(responseStudentDetail);
  }

  /**
   * 入力された内容で受講生情報を更新します。
   * リクエストボディ（JSON）で受け取った受講生詳細をもとに、
   * 受講生情報とコース情報を更新します。
   * <p>
   * キャンセルフラグ（is_Deleted）の更新もここで行います。
   *
   * @param studentDet ail リクエストボディから受け取った受講生詳細
   * @return 更新処理の成功を示すメッセージ（ステータス200）
   */
  // ① POSTリクエスト（/updateStudent）が送信されると、このメソッドが呼ばれる
  //    リクエストボディのJSONは、@RequestBodyによって studentDetail に自動で詰め込まれる
  @PutMapping("/updateStudent")
  public ResponseEntity<String> updateStudent(@RequestBody StudentDetail studentDetail) {
    // ② Serviceを通して、受講生情報とコース情報を更新する
    service.updateStudent(studentDetail);
    // ③ 更新が成功したことを示すメッセージを、ステータス200で返す
    return ResponseEntity.ok("更新処理が成功しました");
  }
}

package raisetech.StudentManagementSystem.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.exception.TestException;
import raisetech.StudentManagementSystem.service.StudentService;

/**
 * 受講生の一覧取得・新規登録・更新に関するリクエストを受け付けるRESTコントローラーです。
 * 処理結果は画面（テンプレート）ではなく、JSONなどのレスポンスボディとして返します。
 *
 * <p>【提供するAPI】
 * <ul>
 *   <li>GET  /studentList     … 受講生詳細の一覧を取得</li>
 *   <li>GET  /student/{id}    … 指定したidの受講生詳細を取得</li>
 *   <li>POST /registerStudent … 受講生詳細を新規登録</li>
 *   <li>PUT  /updateStudent   … 受講生詳細を更新</li>
 *   <li>GET  /testException   … 例外処理の動作確認専用（常にTestExceptionを発生させる）</li>
 * </ul>
 * このクラスは受付と入力チェックだけを行い、実際の処理はServiceに任せます。
 * 例外が発生したときの返し方は、共通の例外処理クラス（GlobalExceptionHandler）が決めています。
 */
// @Tag：Swagger UI上でAPIをまとめる見出し（付けないと「student-controller」と表示される）
@Tag(name = "受講生管理", description = "受講生とコース情報を扱うAPI")
// @Validated：メソッドの引数に付けた入力チェック（@Minなど）を有効にする
@Validated
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
   * @return 受講生詳細の一覧
   */
  // ① 【受講生一覧】http://localhost:8080/studentList にGETリクエストすると呼ばれる
  //    引数は不要（一覧は全件取得するため、リクエストから受け取る値がない）
  // 【以下の@Operation・@ApiResponseなどは、Swagger UI（API仕様書の画面）に表示する説明です。処理の動きには影響しません】
  // @Operation：このAPIの概要(summary)・詳しい説明(description)・返しうるレスポンス(responses)を書く
  // @ApiResponse：ステータスごとの説明（200＝成功）。@Content/@Schemaで、返ってくるJSONの型を示す
  // @ArraySchema：返ってくるのが StudentDetail の「配列（リスト）」であることを示す
  @Operation(
      summary = "受講生一覧取得",
      description = """
          登録されている受講生の一覧を、各受講生のコース情報とあわせて取得します。
          
          - 削除済み（isDeleted が true）の受講生も含まれます
          - 受講生が登録されていない場合は、空のリストを返します
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "取得成功",
              content = @Content(mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = StudentDetail.class))))
      })
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
  // ※ @Min(1)：idが1以上かを入力チェックする（0以下ならエラーになり、処理は実行されない）
  //    数値以外（例：/student/abc）は、Integerに変換できないため、こちらもエラーになる
  // 【Swagger UI用の説明】ステータス400は、IDが数値以外で入力チェックに失敗した場合
  //   ステータス500は、IDが0以下の場合と、存在しないIDを指定した場合（現状。今後400・404に変更予定）
  // （※存在しないIDを指定した場合は、Service側でエラーになる点に注意。詳細はStudentServiceのコメント参照）
  @Operation(
      summary = "受講生取得",
      description = "指定したIDの受講生と、その受講生のコース情報をまとめて取得します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "取得成功",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = StudentDetail.class))),
          @ApiResponse(responseCode = "400", description = "IDが数値以外の場合",
              content = @Content),
          @ApiResponse(responseCode = "500", description = "IDが0以下、または存在しないIDの場合は現状500（今後400・404に変更予定）",
              content = @Content)
      })
  @GetMapping("/student/{id}")
  // @Parameter：Swagger UIに表示する、URLの{id}の説明・入力例(example)・必須かどうか。入力チェックそのものは@Min(1)が行う
  public StudentDetail getStudent(@Parameter(description = "受講生ID（1以上）", example = "1", required = true) @PathVariable @Min(1) Integer id) {
    // ② Serviceを通して、idに一致する受講生とそのコース情報を取得する
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
  //    @Valid：詰め込んだ内容を、Student・StudentCourseに付けた入力チェック（必須・文字数など）で確認する。
  //    チェックに失敗すると、Serviceは呼ばれずエラーになる
  // 【Swagger UI用の説明】登録APIの仕様（IDや日時は自動で入ること、登録例のJSON、200と400のレスポンス）を示す
  // requestBody：リクエストボディとして送るJSONの説明と登録例（@ExampleObject）。例では、送信不要の項目（id・日時など）は省いている
  // （Springの@RequestBodyと名前が同じなので、こちらは完全な名前で書いている）
  @Operation(
      summary = "受講生登録",
      description = """
          受講生とコース情報を新規登録します。
          
          - 受講生IDとコースIDは、データベースが自動で採番します（送信は不要です）
          - コースの開始日時には登録時の日時、終了日時にはその1年後が自動でセットされます
          - 削除フラグは、未削除（false）で登録されます
          - 登録後の受講生詳細（採番されたIDを含む）を返します
          """,
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "登録する受講生詳細",
          required = true,
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = StudentDetail.class),
              examples = @ExampleObject(name = "登録例", value = """
                  {
                    "student": {
                      "name": "山田 太郎",
                      "kanaName": "ヤマダ タロウ",
                      "nickname": "タロウ",
                      "email": "taro@example.com",
                      "area": "兵庫県",
                      "age": 30,
                      "sex": "男性",
                      "remark": "特になし"
                    },
                    "studentCourseList": [
                      { "courseName": "Javaコース" }
                    ]
                  }
                  """))),
      responses = {
          @ApiResponse(responseCode = "200", description = "登録成功",
              content = @Content(mediaType = "application/json",
                  schema = @Schema(implementation = StudentDetail.class))),
          @ApiResponse(responseCode = "400", description = "入力チェックに違反した場合（名前が空、メールアドレスの形式が不正など）",
              content = @Content)
      })
  @PostMapping("/registerStudent")
  public ResponseEntity<StudentDetail> registerStudent(@RequestBody @Valid StudentDetail studentDetail) {
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
   * 削除フラグ（isDeleted）の更新もここで行います（データを消さずに削除済みにする論理削除）。
   *
   * @param studentDetail リクエストボディから受け取った受講生詳細
   * @return 更新処理の成功を示すメッセージ（ステータス200）
   */
  // ① PUTリクエスト（/updateStudent）が送信されると、このメソッドが呼ばれる
  //    リクエストボディのJSONは、@RequestBodyによって studentDetail に自動で詰め込まれる
  //    @Valid：詰め込んだ内容を入力チェックし、失敗するとServiceは呼ばれずエラーになる
  // 【Swagger UI用の説明】更新APIの仕様（更新対象の項目、論理削除の方法、返すメッセージ）を示す
  // 成功時は JSON ではなく文字列を返すため、mediaType が text/plain になっている
  @Operation(
      summary = "受講生更新",
      description = """
          受講生とコース情報を更新します。
          
          - 受講生は、ID以外のすべての項目が更新対象です
          - 削除フラグ（isDeleted）を true にすると、データを消さずに削除済みとして扱います（論理削除）
          - コース情報は、コース名のみ更新されます（開始日時・終了日時は変更されません）
          """,
      responses = {
          @ApiResponse(responseCode = "200", description = "更新成功",
              content = @Content(mediaType = "text/plain",
                  schema = @Schema(type = "string", example = "更新処理が成功しました"))),
          @ApiResponse(responseCode = "400", description = "入力チェックに違反した場合",
              content = @Content)
      }
  )
  @PutMapping("/updateStudent")
  public ResponseEntity<String> updateStudent(@RequestBody @Valid StudentDetail studentDetail) {
    // ② Serviceを通して、受講生情報とコース情報を更新する
    service.updateStudent(studentDetail);
    // ③ 更新が成功したことを示すメッセージを、ステータス200で返す
    return ResponseEntity.ok("更新処理が成功しました");
  }

  /**
   * 例外処理の動作確認専用のメソッドです。
   * 他の機能には影響せず、常にTestExceptionを発生させます。
   *
   * @throws TestException 常に発生する（動作確認用の例外）
   */
  // ① http://localhost:8080/testException にGETリクエストすると呼ばれる
  //    発生した例外は、共通の例外処理クラス（GlobalExceptionHandler）が受け止める
  // 【Swagger UI用の説明】このAPIは常に400を返す、動作確認専用であることを示す
  @Operation(
      summary = "例外処理の動作確認",
      description = "例外処理の確認専用のAPIです。常に TestException を発生させ、400 を返します。",
      responses = {
          @ApiResponse(responseCode = "400", description = "常に返されるエラー",
              content = @Content)
      })
  @GetMapping("/testException")
  public void throwTestException() throws TestException {
    throw new TestException("このエラーは動作確認のために意図的に発生させたものです。");
  }
}

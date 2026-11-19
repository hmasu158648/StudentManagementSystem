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
 *
 * <p>【たとえ話】お店でいうと、Controllerは「受付係」です。
 * お客さん（ブラウザやアプリ）の注文（リクエスト）を聞いて、まちがいがないか確認し、
 * 実際の作業は奥の担当者（Service）にお願いして、結果をお客さんに返します。
 * 「JSON」は、データを文字で表す決まった書き方で、アプリ同士のやり取りによく使われます。
 */
// @Tag：Swagger UI（API仕様書の画面）で、APIをまとめる見出しをつける目印
// （つけないと「student-controller」という名前で表示される）
@Tag(name = "受講生管理", description = "受講生とコース情報を扱うAPI")
// @Validated：メソッドの引数につけた入力チェック（@Minなど）を、有効にする目印
@Validated
// @RequiredArgsConstructor：Lombok（ライブラリ）が、下のfinalフィールド用のコンストラクターを自動で作ってくれる
// （自分で書く手間が省ける。このコンストラクターを通して、SpringがServiceを渡してくれる）
@RequiredArgsConstructor
// @RestController：「このクラスは受付係だよ。返す値は、画面ではなくデータ（JSONや文字）として返すよ」という目印
@RestController
public class StudentController {

  // 受講生・コース情報の検索や登録・更新をする担当者（Service）。
  // コンストラクター経由でSpringが渡してくれる（final＝一度入れたら変えない）
  private final StudentService service;

  /**
   * 受講生一覧を取得します。
   * 受講生とコース情報を全件取得し、受講生ごとの詳細情報にまとめて返します。
   *
   * @return 受講生詳細の一覧
   */
  // ① 【受講生一覧】ブラウザで http://localhost:8080/studentList を開く（GETリクエスト）と、このメソッドが呼ばれる
  //    引数は不要（全員分を返すので、お客さんから聞くことがない）
  // 【以下の@Operation・@ApiResponseなどは、Swagger UI（API仕様書の画面）に出す「説明書き」です。
  //   プログラムの動きには関係しません】
  // @Operation：このAPIの名前(summary)・詳しい説明(description)・返事の種類(responses)を書く
  // @ApiResponse：返事（ステータスコード）ごとの説明。200は「成功」の番号。
  //   @Content/@Schemaで、返ってくるJSONの形を示す
  // @ArraySchema：返ってくるのが、StudentDetailがいくつも並んだ「リスト（配列）」だと示す
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
    // ② Service（作業をする人）に「一覧をください」とお願いして、
    //    受講生とコース情報をまとめた受講生詳細の一覧を受け取る
    // ③ 受け取った一覧を、そのままお客さんに返す（自動でJSONに変換されて、返事の本文（レスポンスボディ）になる）
    return service.searchStudentList();
  }

  /**
   * 受講生を1人検索します。
   * URLで指定されたIDの受講生情報とコース情報を、受講生詳細としてまとめて返します。
   *
   * @param id URLに含まれる受講生ID
   * @return 該当する受講生詳細（レスポンスボディとして返されます）
   */
  // ① 例：http://localhost:8080/student/1 を開く（GETリクエスト）と呼ばれ、URLの最後の「1」がidに入る
  //    （@PathVariable：URLの{id}の部分を、引数のidに取り出してくれる目印）
  // ※ @Min(1)：idが1以上かをチェックする（0以下ならエラーになり、このメソッドの中身は実行されない）
  //    数字以外（例：/student/abc）は、整数（Integer）に直せないので、こちらもエラーになる
  // 【Swagger UI用の説明】ステータス400は、IDが数字以外でチェックに失敗した場合。
  //   ステータス500は、IDが0以下の場合と、存在しないIDを指定した場合（今はこうなる。今後400・404に変える予定）
  //   （※ステータスコード：返事の結果を表す番号。200＝成功、400＝送った内容がまちがい、500＝プログラム側のエラー）
  // （※存在しないIDのときは、Service側でエラーになる点に注意。詳しくはStudentServiceのコメント参照）
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
  // @Parameter：Swagger UIに出す、URLの{id}の説明・入力例(example)・必ず必要か、という説明書き。
  // （実際の入力チェックをするのは@Min(1)で、@Parameterは説明を書いているだけ）
  public StudentDetail getStudent(@Parameter(description = "受講生ID（1以上）", example = "1", required = true) @PathVariable @Min(1) Integer id) {
    // ② Service（作業をする人）に、idに合う受講生とそのコース情報を探してもらう
    // ③ 見つかった受講生詳細を、そのまま返す（自動でJSONに変換されて、返事の本文になる）
    return service.searchStudent(id);
  }

  /**
   * 入力された受講生情報を新規登録します。
   * リクエストボディ（JSON）で受け取った受講生詳細をもとに、受講生情報とコース情報を新規登録します。
   *
   * @param studentDetail リクエストボディから受け取った受講生詳細
   * @return 登録した受講生詳細（ステータス200）
   */
  // ① POSTリクエスト（/registerStudent）で、登録したい情報が送られてくると、このメソッドが呼ばれる
  //    （POST＝「新しくデータを送るよ」という種類のリクエスト）
  //    送られてきたJSON（リクエストボディ）は、@RequestBodyの働きで、Spring Bootが studentDetail に自動で詰めてくれる
  //    @Valid：詰めた内容が、Student・StudentCourseに書いたルール（必ず入力・文字数など）を守っているかチェックする。
  //    ルールを破っていたら、Service（作業をする人）には渡さず、エラーを返す
  // 【Swagger UI用の説明】登録APIの説明書き（IDや日時は自動で入ること、登録例のJSON、200と400の返事）
  // requestBody：送るJSONの説明と登録例（@ExampleObject）。例には、送らなくてよい項目（id・日時など）は書いていない
  // （Springの@RequestBodyと名前が同じなので、こちらは長い正式な名前で書いている）
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
    // ② Service（作業をする人）に登録をお願いして、登録後の受講生詳細
    //    （データベースが自動でつけた番号（id）などが入ったもの）を受け取る
    StudentDetail responseStudentDetail = service.registerStudent(studentDetail);
    // ③ 登録後の受講生詳細を、ステータス200（成功）をつけて、返事の本文として返す
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
  // ① PUTリクエスト（/updateStudent）で、直したい情報が送られてくると、このメソッドが呼ばれる
  //    （PUT＝「すでにあるデータを書きかえるよ」という種類のリクエスト）
  //    送られてきたJSONは、@RequestBodyの働きで、自動で studentDetail に詰められる
  //    @Valid：詰めた内容をルールどおりかチェックし、まちがいがあればServiceは呼ばずにエラーを返す
  // 【Swagger UI用の説明】更新APIの説明書き（直せる項目、論理削除のやり方、返すメッセージ）
  // 成功したときはJSONではなく、ふつうの文字を返すので、mediaTypeが text/plain（ただの文字）になっている
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
    // ② Service（作業をする人）に、受講生情報とコース情報の更新をお願いする
    service.updateStudent(studentDetail);
    // ③ 更新がうまくいったことを知らせるメッセージを、ステータス200（成功）で返す
    //   （ここまでエラーが起きなかったので、成功とみなしている）
    return ResponseEntity.ok("更新処理が成功しました");
  }

  /**
   * 例外処理の動作確認専用のメソッドです。
   * 他の機能には影響せず、常にTestExceptionを発生させます。
   *
   * @throws TestException 常に発生する（動作確認用の例外）
   */
  // ① http://localhost:8080/testException を開く（GETリクエスト）と呼ばれる
  //    下の throw で、わざとエラー（例外）を出す。出たエラーは、
  //    エラー専門の係（GlobalExceptionHandler）が受け止めて、お客さんへの返事を作る
  //    （「エラー用の仕組みが、ちゃんと動くか」を試すためだけのメソッド）
  // 【Swagger UI用の説明】このAPIは必ず400を返す、動作確認専用だと示す
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

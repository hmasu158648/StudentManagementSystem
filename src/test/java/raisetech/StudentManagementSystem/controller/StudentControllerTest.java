package raisetech.StudentManagementSystem.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.service.StudentService;

/**
 * StudentController（リクエストの受付窓口）のテストクラスです。
 * 本物のサーバーやDBは起動せず、擬似的にHTTPリクエストを送って、
 * 「URLに対して正しいステータスが返るか」「Serviceを正しく呼び出しているか」を確認します。
 * あわせて、受講生（Student）の入力チェック（ルールを守っているか）も確認します。
 *
 * 【たとえ話】Controllerはお店の「受付係」、Serviceは「作業をする人」、DBは「倉庫」です。
 * このテストでは、受付係の仕事だけを調べたいので、作業をする人と倉庫は「ものまね役（偽物）」にしています。
 */
// @WebMvcTest：「受付係（Controller）だけを動かしてテストするよ」という目印。
// 引数のStudentControllerだけを対象にするので、Service・Repository・DBは読み込まれない
@WebMvcTest(StudentController.class)
class StudentControllerTest {

  // MockMvc（モックエムブイシー）：本物のサーバーを動かさなくても、
  // 「このURLにアクセスしたよ」というふりができる道具。Springが自動で用意してくれる
  @Autowired
  private MockMvc mockMvc;

  // @MockitoBean：本物のServiceの代わりに、ものまね役（偽物）のServiceを使う、という目印。
  // 偽物なので、DB（倉庫）には接続しない。「何回呼ばれたか」という記録だけが残るので、あとで確認できる
  @MockitoBean
  private StudentService service;

  // Validator（バリデーター）は「入力チェックの先生」のようなもの。
  // たとえば「名前は空っぽはダメ」「IDは1以上」といったルールを、データクラスに書いてある。
  // その先生に「このデータ、ルールを守れているか見て」とお願いすると、
  // ルールを破っているところを、一覧にして教えてくれる。
  // 右側の Validation.buildDefaultValidatorFactory().getValidator() は、
  // その先生を呼んでくる、おなじみの決まり文句（そのまま覚えて使えばOK）。
  // ※ サーバーにリクエストを送らなくても、データだけでチェックできるのが便利なところ
  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  // 受講生の一覧を見るためのURL（/studentList）にアクセスしたとき、
  // 「成功（200）」が返ることと、受付係がServiceに「一覧をお願い」と1回頼んだことを確認するテスト
  // （throws Exception：アクセスのふりをする処理は、失敗するかもしれないので付けている）
  @Test
  void 受講生詳細の一覧検索が実行できて_サービスの一覧検索が1回呼ばれること() throws Exception {
    // 【実行】ブラウザで /studentList を開いたときと同じように、GETでアクセスしたふりをする
    // （偽物のServiceは何も約束していないので、空っぽのリストを返す。今回は中身は調べない）
    mockMvc.perform(get("/studentList"))
        // 【検証】①結果の番号（ステータスコード）が200（＝成功）であること
        .andExpect(status().isOk());

    // ②受付係（Controller）が、Serviceの一覧検索（searchStudentList）を、ちょうど1回呼んだこと
    verify(service, times(1)).searchStudentList();
  }

  /**
   * 入力チェックに合格する（ルールを守っている）受講生を作ります。
   * 各テストでは、この受講生の1か所だけを変えます。
   * そうすると、「変えたところが原因でルール違反になった」とはっきり分かります（比べるときと同じ考え方です）。
   */
  private Student createValidStudent() {
    // ①空の受講生を作る
    Student student = new Student();
    // ②名前・ふりがな（全角カタカナ）・メールアドレスを、ルールを守った正しい形で入れる
    student.setName("山田太郎");
    student.setKanaName("ヤマダタロウ");
    student.setEmail("taro@example.com");
    // ③できあがった受講生を、テストで使えるように返す
    return student;
  }

  // 全部ルールどおりの受講生なら、入力チェックで「違反なし」になることを確認するテスト
  @Test
  void 受講生詳細の受講生で適切な値を入力した時に入力チェックに異常が発生しないこと() {
    // 【事前準備】ルールを守っている受講生を用意する
    Student student = createValidStudent();

    // 【実行】入力チェックの先生（validator）に、受講生を見てもらう。
    // 結果は「ルール違反の一覧」で返ってくる（違反がなければ空っぽ）
    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    // 【検証】違反の数が0件であること（assertEquals(期待する値, 実際の値)：同じなら合格）
    Assertions.assertEquals(0, violations.size());
  }

  // ふりがなに、全角カタカナではなくひらがなを入れると、入力チェックでルール違反になることを確認するテスト
  @Test
  void 受講生詳細の受講生でふりがなにカタカナ以外を用いた時に入力チェックに掛かること() {
    // 【事前準備】ルールを守っている受講生を用意し、ふりがなだけをひらがなに変える（わざとルール違反にする）
    Student student = createValidStudent();
    student.setKanaName("やまだたろう");

    // 【実行】入力チェックの先生に、受講生を見てもらう
    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    // 【検証】
    // ①違反は1件だけであること（変えたのはふりがなだけなので、1件のはず）
    Assertions.assertEquals(1, violations.size());
    // ②違反した項目の名前が kanaName（ふりがな）であること
    // （violations.iterator().next()：違反の一覧から1件目を取り出す。getPropertyPath()：どの項目が違反か）
    Assertions.assertEquals("kanaName", violations.iterator().next().getPropertyPath().toString());
  }
}
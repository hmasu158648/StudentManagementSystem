package raisetech.StudentManagementSystem.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.service.StudentService;

/**
 * StudentController（リクエストの受付窓口）のテストクラスです。
 * 本物のサーバーやDBは起動せず、擬似的にHTTPリクエストを送って、
 * 「URLに対して正しいステータスが返るか」「Serviceを正しく呼び出しているか」を確認します。
 * あわせて、受講生（Student）と受講生コース情報（StudentCourse）の入力チェック（ルールを守っているか）も確認します。
 *
 * <p>【たとえ話】Controllerはお店の「受付係」、Serviceは「作業をする人」、DBは「倉庫」です。
 * このテストでは、受付係の仕事だけを調べたいので、作業をする人と倉庫は「ものまね役（偽物）」にしています。
 *
 * <p>【テストって何？】プログラムが思ったとおりに動くかを、自動で確かめる「ミニ試験」です。
 * 合格すれば緑、まちがっていれば赤で知らせてくれるので、直したあとの安心につながります。
 */
// @WebMvcTest：「受付係（Controller）だけを動かしてテストするよ」という目印。
// 引数のStudentControllerだけを対象にするので、Service・Repository・DBは読み込まれない
@WebMvcTest(StudentController.class)
class StudentControllerTest {

  // ==================== ① 定数（テストで共通に使う、変更しない値） ====================

  /**
   * 「ルールを守った、正しい」受講生詳細のJSON（＝データを文字で表した書き方）です。
   * 登録・更新のテストで、お客さんが送ってくる「注文の中身」として使います。
   * student（受講生）と、studentCourseList（受講するコースのリスト）が入っています。
   * （static final＝すべてのテストで共通に使う、変更しない値。""" ～ """ は、複数行の文字をそのまま書ける書き方）
   */
  private static final String VALID_STUDENT_DETAIL_JSON = """
      {
        "student": {
          "name": "山田太郎",
          "kanaName": "ヤマダタロウ",
          "email": "taro@example.com"
        },
        "studentCourseList": [
          { "courseName": "Javaコース" }
        ]
      }
      """;

  /**
   * 「ルールを破っている」受講生詳細のJSONです。
   * 上の正しいJSONとの違いは、ふりがな（kanaName）が、全角カタカナではなくひらがなになっている点だけです。
   * 入力チェックで、ちゃんとはじかれる（400が返る）かを確かめるために使います。
   */
  private static final String INVALID_STUDENT_DETAIL_JSON = """
      {
        "student": {
          "name": "山田太郎",
          "kanaName": "やまだたろう",
          "email": "taro@example.com"
        },
        "studentCourseList": [
          { "courseName": "Javaコース" }
        ]
      }
      """;

  // ==================== ② mockMvc（アクセスのふり役） ====================

  /**
   * MockMvc（モックエムブイシー）は、本物のサーバーを動かさなくても、
   * 「このURLにアクセスしたよ」というふりができる道具です。Springが自動で用意してくれます。
   */
  @Autowired
  private MockMvc mockMvc;

  // ==================== ③ service（ものまね役のService） ====================

  /**
   * 本物のServiceの代わりに使う、ものまね役（偽物）の「作業をする人」です。
   * 偽物なので、DB（倉庫）にはつながりません。「何回呼ばれたか」という記録だけが残るので、あとで確認できます。
   *
   * <p>【依存性の注入（DI）とは？】
   * 受付係（Controller）は、仕事をするために作業をする人（Service）を必要としています。
   * このように「Aさんの仕事には、Bさんが必要」という関係を「依存」といいます。
   * 受付係は、作業をする人を自分で探しに行きません。
   * かわりに Spring が、「はい、作業をする人をどうぞ」と、外から渡してくれます。
   * この「外から渡してもらうしくみ」を、依存性の注入（DI）といいます。
   * 渡す側の Spring は、渡すものを「道具箱」に入れて管理しています。
   * この道具箱の正式な名前が「DIコンテナ」です。
   *
   * <p>【なぜ {@code @Mock} ではなく {@code @MockitoBean} なのか】
   * このテストでは、受付係を自分では作らず、{@code @WebMvcTest} が Spring に作らせています。
   * 作業をする人を渡すのも Spring なので、自分で偽物を手渡すことはできません。
   * そこで、偽物を Spring の道具箱（DIコンテナ）に入れておきます。
   * 本物の Service がすでに入る予定なら、本物のかわりに偽物が入ります。
   * すると Spring は、受付係を作るときに、道具箱から偽物を取り出して渡してくれます。
   * {@code @MockitoBean} は、この「偽物を作る」と「偽物を DIコンテナ（道具箱）に入れる」を、まとめてやってくれます。
   * <ul>
   *   <li>{@code @Mock}：偽物を作るだけ。DIコンテナには入らない。自分で渡せるテスト向き（StudentServiceTest で使用）</li>
   *   <li>{@code @MockitoBean}：偽物を作って DIコンテナに入れる。Spring が渡すテスト向き（ここで使用）</li>
   * </ul>
   */
  // @MockitoBean：偽物のServiceを使う、という目印
  // （※ 古い @MockBean の後継。Spring Boot 3.4 以降は @MockitoBean を使う）
  @MockitoBean
  private StudentService service;

  // ==================== ④ validator（入力チェックの先生） ====================

  /**
   * Validator（バリデーター）は「入力チェックの先生」のようなものです。
   * たとえば「名前は空っぽはダメ」「IDは1以上」といったルールを、データクラスに書いてあります。
   * その先生に「このデータ、ルールを守れているか見て」とお願いすると、
   * ルールを破っているところを、一覧にして教えてくれます。
   * ※ サーバーにリクエストを送らなくても、データだけでチェックできるのが便利なところです。
   */
  // 右側の Validation.buildDefaultValidatorFactory().getValidator() は、
  // その先生を呼んでくる、おなじみの決まり文句（そのまま覚えて使えばOK）
  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  // ==================== 【正常系】MockMvc：受付係がうまく動く場合のテスト ====================

  /**
   * 受講生の一覧を見るURL（/studentList）にアクセスしたときの動きを確認するテストです。
   * 確かめることは2つです。
   * <ul>
   *   <li>結果が「成功（200）」で返ってくること</li>
   *   <li>受付係（Controller）が、Serviceに「一覧をお願い」と、ちょうど1回頼んだこと</li>
   * </ul>
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
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
   * 受講生を1人だけ探すURLにアクセスしたとき、結果が「成功（200）」で、
   * Serviceに「この番号（ID）の受講生をお願い」と、ちょうど1回頼んだことを確認するテストです。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の検索が実行できて_サービスの検索がIDを渡して１回呼ばれること() throws Exception {
    // 【実行】番号「1」を指定して、受講生を探すURLにGETでアクセスしたふりをする
    // （{id}の部分に、get()の2番目の引数「1」が入る）
    Integer id = 1;
    mockMvc.perform(get("/student/{id}", id))
        // 【検証】①結果の番号（ステータスコード）が200（成功）であること
        .andExpect(status().isOk());

    // ②受付係（Controller）が、Serviceの検索（searchStudent）を、番号「1」を渡して、ちょうど1回呼んだこと
    verify(service, times(1)).searchStudent(id);
  }

  /**
   * 受講生を探すURLにアクセスしたとき、偽物のServiceが渡してくれた受講生の情報が、
   * そのままJSON（データを文字で表した書き方）になって返ってくることを確認するテストです。
   * 前のテストは「呼ばれたか」を調べましたが、このテストは「返ってきた中身」を調べます。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の検索で_サービスから受け取った受講生詳細がJSONで返ること() throws Exception {
    // 【事前準備】番号（ID）が1の受講生を用意し、コースが0件の受講生詳細に包む
    // （List.of()＝中身が空っぽのリスト）
    Student student = createValidStudent();
    Integer id = 1;
    student.setId(id);
    StudentDetail studentDetail = new StudentDetail(student, List.of());
    // 偽物のServiceに「番号1で探しに来られたら、この受講生詳細を渡してね」と約束させる
    // （when(...).thenReturn(...)＝「もし～されたら、～を返す」という約束）
    when(service.searchStudent(1)).thenReturn(studentDetail);

    // 【実行】番号「1」を指定して、受講生を探すURLにGETでアクセスしたふりをする
    mockMvc.perform(get("/student/{id}", id))
        // 【検証】①ステータスコードが200（成功）であること
        .andExpect(status().isOk())
        // ②返ってきたJSONの student → id が 1 であること
        // （jsonPath＝JSONの中の「ここの値」を指定する書き方。$ はJSON全体の一番上を表す）
        .andExpect(jsonPath("$.student.id").value(1))
        // ③返ってきたJSONの student → name が「山田太郎」であること
        .andExpect(jsonPath("$.student.name").value("山田太郎"));
  }

  /**
   * 正しい内容の受講生詳細を送って登録をお願いしたとき、
   * 結果が「成功（200）」で、Serviceに「登録をお願い」と、ちょうど1回頼んだことを確認するテストです。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の登録が実行できて_サービスの登録処理が１回呼ばれること() throws Exception {
    // 【実行】POSTで、登録する内容（正しいJSON）を送ったふりをする
    mockMvc.perform(post("/registerStudent")
            // 送る内容は、JSONという書き方だよ、と伝える
            .contentType(MediaType.APPLICATION_JSON)
            // 送る内容（ルールを守った受講生詳細）
            .content(VALID_STUDENT_DETAIL_JSON))
        // 【検証】①ステータスコードが200（成功）であること
        .andExpect(status().isOk());

    // ②Serviceの登録（registerStudent）が、ちょうど1回呼ばれたこと
    // （any(StudentDetail.class)＝受講生詳細なら、中身は何でもよい）
    verify(service, times(1)).registerStudent(any(StudentDetail.class));
  }

  /**
   * 正しい内容の受講生詳細を送って更新（内容の書きかえ）をお願いしたとき、
   * 結果が「成功（200）」で、Serviceに「更新をお願い」と、ちょうど1回頼んだことを確認するテストです。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の更新が実行できて_サービスの更新処理が1回呼ばれること() throws Exception {
    // 【実行】PUT（＝すでにあるデータを書きかえるときの送り方）で、正しいJSONを送ったふりをする
    mockMvc.perform(put("/updateStudent")
            // 送る内容は、JSONという書き方だよ、と伝える
            .contentType(MediaType.APPLICATION_JSON)
            // 送る内容（ルールを守った受講生詳細）
            .content(VALID_STUDENT_DETAIL_JSON))
        // 【検証】①ステータスコードが200（成功）であること
        .andExpect(status().isOk());

    // ②Serviceの更新（updateStudent）が、ちょうど1回呼ばれたこと
    // （any(StudentDetail.class)＝受講生詳細なら、中身は何でもよい）
    verify(service, times(1)).updateStudent(any(StudentDetail.class));
  }

  // ==================== 【異常系】MockMvc：うまくいかない場合のテスト ====================

  /**
   * 受講生を探すURLの番号（ID）に、数字ではなく文字（abc）を指定したとき、
   * 「送った内容がまちがい（400）」が返り、Serviceには頼まない（呼ばれない）ことを確認するテストです。
   * 番号は整数のはずなのに文字が来たので、受付係の段階で追い返されます。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の検索でIDに数字以外を指定した場合_400が返りサービスは呼ばれないこと() throws Exception {
    // 【実行】番号の代わりに、わざと文字「abc」を指定してアクセスしたふりをする
    mockMvc.perform(get("/student/{id}", "abc"))
        // 【検証】①ステータスコードが400（送った内容がまちがい）であること
        .andExpect(status().isBadRequest());

    // ②受付係が追い返したので、Serviceの検索は1回も呼ばれていないこと
    // （never()＝1回も呼ばれていない。any()＝どんな値でもよい）
    verify(service, never()).searchStudent(any());
  }

  /**
   * ルールを破った内容（ふりがながひらがな）で登録をお願いしたとき、
   * 「送った内容がまちがい（400）」が返り、Serviceには頼まない（呼ばれない）ことを確認するテストです。
   * まちがったデータが、奥の作業をする人まで届かず、受付係の段階で止められるかを調べます。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の登録で入力チェックに掛かった場合_400が返りサービスは呼ばれないこと() throws Exception {
    // 【実行】POSTで、ルールを破った内容（まちがったJSON）を送ったふりをする
    mockMvc.perform(post("/registerStudent")
            .contentType(MediaType.APPLICATION_JSON)
            .content(INVALID_STUDENT_DETAIL_JSON))
        // 【検証】①ステータスコードが400（送った内容がまちがい）であること
        .andExpect(status().isBadRequest());

    // ②入力チェックで止められたので、Serviceの登録は1回も呼ばれていないこと
    verify(service, never()).registerStudent(any());
  }

  /**
   * ルールを破った内容（ふりがながひらがな）で更新をお願いしたとき、
   * 「送った内容がまちがい（400）」が返り、Serviceには頼まない（呼ばれない）ことを確認するテストです。
   * 登録のときと同じく、まちがったデータは受付係の段階で止められるかを調べます。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 受講生詳細の更新で入力チェックに掛かった場合_400が返りサービスは呼ばれないこと() throws Exception {
    // 【実行】PUTで、ルールを破った内容（まちがったJSON）を送ったふりをする
    mockMvc.perform(put("/updateStudent")
            .contentType(MediaType.APPLICATION_JSON)
            .content(INVALID_STUDENT_DETAIL_JSON))
        // 【検証】①ステータスコードが400（送った内容がまちがい）であること
        .andExpect(status().isBadRequest());

    // ②入力チェックで止められたので、Serviceの更新は1回も呼ばれていないこと
    verify(service, never()).updateStudent(any());
  }

  /**
   * 例外（エラー）の確認用URL（/testException）にアクセスしたとき、
   * 「例外処理クラス（GlobalExceptionHandler）」が働いて、「送った内容がまちがい（400）」が返ることを確認するテストです。
   * 【たとえ話】お店でトラブルが起きたとき、決まった係の人が、決まった言い方でお客さんに伝えているか、を調べます。
   *
   * @throws Exception アクセスのふりをする処理は、失敗する可能性があるため付けている
   */
  @Test
  void 例外確認用のAPIを呼ぶと_例外処理クラスにより400が返ること() throws Exception {
    // 【実行】わざと例外が起きるURLに、GETでアクセスしたふりをする
    mockMvc.perform(get("/testException"))
        // 【検証】例外処理クラスが受け止めて、ステータスコードが400になっていること
        .andExpect(status().isBadRequest())
        .andExpect(content().string("このエラーは動作確認のために意図的に発生させたものです。"));
  }

  /**
   * 入力チェックに合格する（ルールを守っている）受講生を作ります。
   * 各テストでは、この受講生の1か所だけを変えます。
   * そうすると、「変えたところが原因でルール違反になった」とはっきり分かります（比べるときと同じ考え方です）。
   *
   * @return ルールをすべて守っている受講生
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

  // ==================== 【正常系】validator：ルールを守っているデータのテスト ====================

  /**
   * 全部ルールどおりの受講生なら、入力チェックで「違反なし（0件）」になることを確認するテストです。
   * 「正しいデータが、まちがって怒られない」ことを確かめています。
   */
  @Test
  void 受講生詳細の受講生で適切な値を入力した時に入力チェックに異常が発生しないこと() {
    // 【事前準備】ルールを守っている受講生を用意する
    Student student = createValidStudent();

    // 【実行】入力チェックの先生（validator）に、受講生を見てもらう。
    // 結果は「ルール違反の一覧」で返ってくる（違反がなければ空っぽ）
    // （Set＝同じものを2つ入れない入れ物。ConstraintViolation＝「ルール違反1件分の情報」。
    //  <Student> は「受講生についての違反」という意味）
    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    // 【検証】違反の数が0件であること
    // （assertThat(実際の値).isEqualTo(期待する値)：「実際の値が、期待する値と同じか」を調べる。
    //  同じなら合格、ちがえば不合格。size()＝一覧の件数）
    // 下の1行は、前に使っていた書き方（JUnitのassertEquals）で、今は使わないのでコメントにしてある
    // Assertions.assertEquals(0, violations.size());
    assertThat(violations.size()).isEqualTo(0);
  }

  /**
   * コース名がきちんと入っている受講生コース情報なら、入力チェックで「違反なし（0件）」になることを確認するテストです。
   * 「正しいデータが、まちがって怒られない」ことを確かめています。
   */
  @Test
  void 受講生コース情報で適切な値を入力した時に入力チェックに異常が発生しないこと() {
    // 【事前準備】コース名に「Javaコース」を入れた、ルールを守っている受講生コース情報を用意する
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setCourseName("Javaコース");

    // 【実行】入力チェックの先生（validator）に、受講生コース情報を見てもらう
    // （<StudentCourse> は「受講生コース情報についての違反」という意味）
    Set<ConstraintViolation<StudentCourse>> violations = validator.validate(studentCourse);

    // 【検証】違反の数が0件であること
    assertThat(violations.size()).isEqualTo(0);
  }

  // ==================== 【異常系】validator：ルールを破っているデータのテスト ====================

  /**
   * ふりがなに、全角カタカナではなくひらがなを入れると、入力チェックでルール違反になることを確認するテストです。
   * 「まちがったデータが、ちゃんと見つかる」ことを確かめています。
   * 違反は1件だけで、その場所がふりがな（kanaName）であることまで調べます。
   */
  @Test
  void 受講生詳細の受講生でふりがなにカタカナ以外を用いた時に入力チェックに掛かること() {
    // 【事前準備】ルールを守っている受講生を用意し、ふりがなだけをひらがなに変える（わざとルール違反にする）
    Student student = createValidStudent();
    student.setKanaName("やまだたろう");

    // 【実行】入力チェックの先生に、受講生を見てもらう
    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    // 【検証】
    // ①違反は1件だけであること（変えたのはふりがなだけなので、1件のはず）
    assertThat(violations.size()).isEqualTo(1);
    // ②違反した項目の名前が kanaName（ふりがな）だけであること
    // （extracting：違反の一覧から「どの項目が違反か」（getPropertyPath）だけを取り出す。
    //  containsOnly：取り出した名前が、指定したものだけであれば合格）
    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsOnly("kanaName");
  }

  /**
   * コース名が空っぽ（""）の受講生コース情報は、入力チェックでルール違反になることを確認するテストです。
   * 「まちがったデータが、ちゃんと見つかる」ことを確かめています。
   * 違反は1件だけで、その場所がコース名（courseName）であることまで調べます。
   */
  @Test
  void 受講生コース情報でコース名が空の時に入力チェックに掛かること() {
    // 【事前準備】コース名を空っぽにした受講生コース情報を用意する（わざとルール違反にする）
    StudentCourse studentCourse = new StudentCourse();
    studentCourse.setCourseName("");

    // 【実行】入力チェックの先生に、受講生コース情報を見てもらう
    Set<ConstraintViolation<StudentCourse>> violations = validator.validate(studentCourse);

    // 【検証】
    // ①違反は1件だけであること（変えたのはコース名だけなので、1件のはず）
    assertThat(violations.size()).isEqualTo(1);
    // ②違反した項目の名前が courseName（コース名）だけであること
    assertThat(violations)
        .extracting(violation -> violation.getPropertyPath().toString())
        .containsOnly("courseName");
  }
}
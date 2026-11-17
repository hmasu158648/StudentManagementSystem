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
 */
// @WebMvcTest：Controller（Web層）だけを起動してテストする目印。
// 引数のStudentControllerだけを対象にするので、Service・Repository・DBは読み込まれない
@WebMvcTest(StudentController.class)
class StudentControllerTest {

  // MockMvc：サーバーを立てずに、擬似的にHTTPリクエストを送れる道具（Springが用意して注入する）
  @Autowired
  private MockMvc mockMvc;

  // @MockitoBean：ControllerがServiceを使うため、本物ではなく偽物（モック）のServiceをSpringに登録する。
  // 偽物なので、DBには接続せず、呼び出しの記録だけが残る
  @MockitoBean
  private StudentService service;

  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  // 一覧検索（GET /studentList）が正常（200）で終わり、
  // Serviceの一覧検索を1回呼び出すことを確認するテスト
  @Test
  void 受講生詳細の一覧検索が実行できて_サービスの一覧検索が1回呼ばれること() throws Exception {
    // 【実行】GET /studentList にリクエストを送り、結果を検証する
    // （偽物のServiceには戻り値の約束をしていないので、Mockitoの標準動作で空のリストが返り、レスポンスも[]になる）
    mockMvc.perform(get("/studentList"))
        // 【検証】①ステータスコードが200（OK）であること
        .andExpect(status().isOk());

    // ②Controllerが、Serviceの一覧検索（searchStudentList）をちょうど1回呼び出したこと
    verify(service, times(1)).searchStudentList();
  }

  /**
   * 入力チェックに合格する（ルール違反のない）受講生を作ります。
   * 各テストではこの受講生の1項目だけを変えることで、確かめたいルールだけを検証できるようにしています。
   */
  private Student createValidStudent() {
    Student student = new Student();
    student.setName("山田太郎");
    student.setKanaName("ヤマダタロウ");
    student.setEmail("taro@example.com");
    return student;
  }

  // 正しい値を入れた受講生は、入力チェックで違反が出ないことを確認するテスト
  @Test
  void 受講生詳細の受講生で適切な値を入力した時に入力チェックに異常が発生しないこと() {
    Student student = createValidStudent();

    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    Assertions.assertEquals(0, violations.size());
  }

  // ふりがなに全角カタカナ以外（ひらがな）を入れると、入力チェックに掛かることを確認するテスト
  @Test
  void 受講生詳細の受講生でふりがなにカタカナ以外を用いた時に入力チェックに掛かること() {
    Student student = createValidStudent();
    student.setKanaName("やまだたろう");

    Set<ConstraintViolation<Student>> violations = validator.validate(student);

    // ①違反は1件だけであること
    Assertions.assertEquals(1, violations.size());
    // ②違反した項目が kanaName であること
    Assertions.assertEquals("kanaName", violations.iterator().next().getPropertyPath().toString());
  }
}
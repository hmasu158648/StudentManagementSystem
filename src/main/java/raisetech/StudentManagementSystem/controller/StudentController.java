package raisetech.StudentManagementSystem.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import raisetech.StudentManagementSystem.controller.converter.StudentConverter;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.service.StudentService;

/**
 * 受講生の一覧表示・新規登録・更新に関するリクエストを受け付けるコントローラーです。
 * 画面（Thymeleafテンプレート）の表示と、コース一覧のJSON返却を担当します。
 */
// http://localhost:8080
@RequiredArgsConstructor // Lombokが代わりにfinalフィールドのコンストラクターを作成してくれる。
@Controller
public class StudentController {

  private final StudentService service;
  private final StudentConverter converter;

  /**
   * 受講生一覧画面を表示します。
   * 受講生とコース情報を全件取得し、受講生ごとの詳細情報にまとめて画面へ渡します。
   *
   * @param model 画面へ渡すデータを格納するModel
   * @return 表示するテンプレート名（studentList.html）
   */
  // 【受講生一覧】http://localhost:8080/studentList を開くと呼ばれる
  @GetMapping("/studentList")
  public String getStudentList(Model model) {
    List<Student> students = service.searchStudentList();
    List<StudentsCourses> studentCourses = service.searchStudentCourseList();

    // converterでList<StudentDetail>の形に整えてモデルに渡す
    // ここではHTMLで記述した${studentList}と同じ名前にする
    model.addAttribute("studentList", converter.convertStudentDetails(students, studentCourses));
    // ここはテンプレートエンジンのファイル名と同名にする
    return "studentList";
  }

  /**
   * 受講生の更新画面を表示します。
   * URLで指定されたIDの受講生詳細を検索し、画面へ渡します。
   *
   * @param id    URLに含まれる受講生ID
   * @param model 画面へ渡すデータを格納するModel
   * @return 表示するテンプレート名（updateStudent.html）
   */
  // 例：http://localhost:8080/student/1 の「1」がidに入る
  @GetMapping("/student/{id}")
  public String getStudent(@PathVariable Integer id, Model model) {
    // URLのIDで検索した受講生１人分のStudentDetailオブジェクトを、
    // "studentDetail"という名前でModelに登録する
    model.addAttribute("studentDetail", service.searchStudent(id));
    // updateStudent.htmlを表示する（Modelの情報は"studentDetail"という名前で参照できる）
    return "updateStudent";
  }

  // ===== 受講生の新規登録（GET と POST の2歩で行う） =====

  /**
   * 受講生の新規登録画面を表示します。
   * 入力欄が空の受講生詳細（受講生1人・コース1件）を用意して画面へ渡します。
   *
   * @param model 画面へ渡すデータを格納するModel
   * @return 表示するテンプレート名（registerStudent.html）
   */
  // ① ブラウザで http://localhost:8080/newStudent を開くと、このメソッドが呼ばれる
  @GetMapping("/newStudent")
  public String newStudent(Model model) {
    // ② 白紙の申込用紙を用意して画面に渡す
    StudentDetail studentDetail = new StudentDetail();               // 申込用紙を1枚用意する
    studentDetail.setStudent(new Student());                          // 空の受講生欄を入れる
    studentDetail.setStudentsCourses(List.of(new StudentsCourses())); // 空のコース用紙を1枚入れた棚を申込用紙に入れる
    model.addAttribute("studentDetail", studentDetail);
    //    templates/registerStudent.html を表示する（③ は HTML 側へ）
    return "registerStudent";
  }

  /**
   * 入力された受講生情報を新規登録します。
   * 入力値の変換にエラーがあれば登録画面を再表示し、問題なければ登録後に一覧画面へ移動します。
   *
   * @param studentDetail 画面から入力された受講生詳細
   * @param result        入力値の変換結果（エラー情報）
   * @return エラー時は登録画面、成功時は一覧画面へのリダイレクト先
   */
  // ⑤ HTML で「登録」ボタンが押されると、このメソッドが呼ばれる
  //    入力内容は、Spring Boot が studentDetail の中に自動で詰め込んでくれる
  // GetでregisterStudent.htmlに送ったモデルは使われず、
  // このPostハンドラメソッドでは引数の@ModelAttribute studentDetailをspringが見て
  // 自動でモデル用のインスタンスを作成し用意してくれる。
  @PostMapping("/registerStudent")
  public String registerStudent(@ModelAttribute StudentDetail studentDetail, BindingResult result) {
    // ⑤-2 詰め込みの途中で問題があったか確認する
    //      （例：数字の欄に文字が入っていて、変換できなかった場合など）
    //      問題があれば、登録画面をもう一度表示する
    if (result.hasErrors()) {
      return "registerStudent";
    }

    // 新規受講生情報を登録する処理
    service.registerStudent(studentDetail);
    // ⑥ 一覧画面の URL へ移動する（redirect: は「画面を表示」ではなく「URL へ移動」の意味）
    return "redirect:/studentList";
  }

  /**
   * 入力された内容で受講生情報を更新します。
   * 入力値の変換にエラーがあれば更新画面を再表示し、問題なければ更新後に一覧画面へ移動します。
   *
   * @param studentDetail 画面から入力された受講生詳細
   * @param result        入力値の変換結果（エラー情報）
   * @return エラー時は更新画面、成功時は一覧画面へのリダイレクト先
   */
  @PostMapping("/updateStudent")
  public String updateStudent(@ModelAttribute StudentDetail studentDetail, BindingResult result) {
    // 入力値の変換に問題があれば、更新画面をもう一度表示する
    if (result.hasErrors()) {
      return "updateStudent";
    }
    // 問題がなければ、受講生情報とコース情報を更新する
    service.updateStudent(studentDetail);
    // 更新後は一覧画面の URL へ移動する
    return "redirect:/studentList";
  }
}

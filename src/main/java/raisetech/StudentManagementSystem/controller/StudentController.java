package raisetech.StudentManagementSystem.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import raisetech.StudentManagementSystem.controller.converter.StudentConverter;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;
import raisetech.StudentManagementSystem.domain.StudentDetail;
import raisetech.StudentManagementSystem.service.StudentService;

// http://localhost:8080
@RequiredArgsConstructor // Lombokが代わりにfinalフィールドのコンストラクターを作成してくれる。
@Controller
public class StudentController {

  private final StudentService service;
  private final StudentConverter converter;
  // @Autowired
  // public StudentController(StudentService service, StudentConverter converter) {
  //   this.service = service;
  //   this.converter = converter;
  // }

  // 【受講生一覧】http://localhost:8080/studentList を開くと呼ばれる
  @GetMapping("/studentList")
  public String getStudentList(Model model) {
    List<Student> students = service.searchStudentList();
    List<StudentsCourses> studentCourses = service.searchStudentCourseList();

    // ここではHTMLで記述した${studentList}と同じ名前にする
    model.addAttribute("studentList", converter.convertStudentDetails(students, studentCourses));
    // ここはテンプレートエンジンのファイル名と同名にするw
    return "studentList";
  }

  // 【コース一覧】@Controller では戻り値が画面名として扱われるため、
  // JSON で返すには @ResponseBody を付ける
  @GetMapping("/studentCourseList")
  @ResponseBody
  public List<StudentsCourses> getStudentCourseList() {
    return service.searchStudentCourseList();
  }

  // ===== 受講生の新規登録（GET と POST の2歩で行う） =====

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

  // ⑤ HTML で「登録」ボタンが押されると、このメソッドが呼ばれる
  //    入力内容は、Spring Boot が studentDetail の中に自動で詰め込んでくれる
  @PostMapping("/registerStudent")
  public String registerStudent(@ModelAttribute StudentDetail studentDetail, BindingResult result) {
    // ⑤-2 詰め込みの途中で問題があったか確認する
    //      （例：数字の欄に文字が入っていて、変換できなかった場合など）
    //      問題があれば、登録画面をもう一度表示する
    if (result.hasErrors()) {
      return "registerStudent";
    }

    // 今回の課題：ここに新規受講生情報を登録する処理を実装する
    // 余裕のある人はコース情報も一緒に登録できるように実装する。コースは単体で良い。
    service.registerStudent(studentDetail);
    System.out.println(studentDetail.getStudent().getName() + "さんの登録が完了しました");
    // ⑥ 一覧画面の URL へ移動する（redirect: は「画面を表示」ではなく「URL へ移動」の意味）
    return "redirect:/studentList";
  }
}

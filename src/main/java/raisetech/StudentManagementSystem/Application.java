package raisetech.StudentManagementSystem;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

  private final Map<String, String> students = new HashMap<>();
  private static final String NO_REGISTERED_MESSAGE = "さんは登録されていません";

  public static void main(String[] args) {
    // localhost:8080
    SpringApplication.run(Application.class, args);
  }

  @GetMapping("/students")
  public Map<String, String> getStudentInfo() {
    return students;
  }

  @GetMapping("/student")
  public String getStudent(@RequestParam String name) {
    String age = students.get(name);
    if (age == null) {
      return name + NO_REGISTERED_MESSAGE;
    }
    return name + " " + age + "歳";
  }

  @GetMapping("/student/age")
  public String getStudentAge(@RequestParam String name) {
    return students.getOrDefault(name, "未登録");
  }

  @PostMapping("/student")
  public String registerStudent(@RequestParam String name, @RequestParam String age) {
    if (students.containsKey(name)) {
      return name + "さんは既に登録されています。";
    }
    students.put(name, age);
    return name + "さんを登録しました";
  }

  @PostMapping("/student/age")
  public String updateStudentAge(@RequestParam String name, @RequestParam String age) {
    if (!students.containsKey(name)) {
      return name + NO_REGISTERED_MESSAGE;
    }
    students.put(name, age);
    return name + "さんの年齢を" + age + "歳に更新しました";
  }

  @PostMapping("/student/delete")
  public String deleteStudent(@RequestParam String name) {
    if (!students.containsKey(name)) {
      return name + NO_REGISTERED_MESSAGE;
    }
    students.remove(name);
    return name + "さんの登録を削除しました";
  }
}

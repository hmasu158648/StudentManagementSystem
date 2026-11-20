package raisetech.StudentManagementSystem.controller.converter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;
import raisetech.StudentManagementSystem.domain.StudentDetail;

/**
 * 「受講生の一覧」と「コースの一覧」を合体させて、「受講生詳細（StudentDetail）の一覧」を作るクラスです。
 *
 * <p>【たとえ話】
 * 先生が「生徒名簿」と「習いごと一覧表」を別々に持っているとします。
 * このクラスは、それを見ながら「この生徒は、この習いごとをしているよ」と、生徒ごとにまとめ直す係です。
 *
 * <p>（今は「受講生＋コース → 受講生詳細」の一方通行だけで、逆向きの変換はありません）
 * Springが管理する部品なので、Service（作業をする人）から使われます。
 */
// @Component：「Springが管理する部品だよ」という目印。
// これがあるので、Serviceを作るときにSpringが自動でこのクラスを渡してくれる
@Component
public class StudentConverter {

  /**
   * 受講生の一覧とコースの一覧から、受講生ごとの詳細（受講生＋その人のコース）を作ります。
   * 受講生のidと、コースに書かれた受講生idが同じものを、その受講生のコースとして結びつけます。
   * （学籍番号が同じなら同じ人のもの、という考え方です）
   *
   * @param studentList       受講生の一覧
   * @param studentCourseList コースの一覧（全員分が混ざっている）
   * @return 受講生ごとにコースをまとめた、受講生詳細の一覧
   */
  public List<StudentDetail> convertStudentDetails(List<Student> studentList, List<StudentCourse> studentCourseList) {
    // ①②受講生の一覧(studentList)と、全員分のコースの一覧(studentCourseList)を、引数として受け取っている
    // ③結果を入れる空の箱（リスト）を用意する。ここに、受講生1人分の詳細を順番に入れていく
    List<StudentDetail> studentDetails = new ArrayList<>();
    // ④ forEach：受講生を1人ずつ取り出して、{ } の中の処理をくり返す
    //   （student -> { ... } は「取り出した1人をstudentと呼んで、この処理をしてね」という書き方で、ラムダ式という）
    studentList.forEach(student -> {
      // ⑤受講生詳細を入れる、新しい入れ物を作る（最初は空っぽ）
      StudentDetail studentDetail = new StudentDetail();
      // ⑥入れ物に、受講生の情報（名前など）を入れる
      studentDetail.setStudent(student);

      // 全員分のコースの中から、この受講生のコースだけを選び出して、新しいリストに入れる
      List<StudentCourse> convertStudentCourseList = studentCourseList.stream()
          // ⑦コースに書かれた受講生idが、この受講生のidと同じものだけを残す（ふるいにかけるイメージ）
          // ⑧残ったコースを、リストにまとめる（.toList()）
          .filter(studentCourse -> studentCourse.getStudentId().equals(student.getId()))
          .toList();
      // ⑨選び出したこの人のコースを、入れ物（studentDetail）に入れる
      studentDetail.setStudentCourseList(convertStudentCourseList);
      // ⑩「受講生＋その人のコース」がそろった入れ物を、結果の箱（studentDetails）に追加する（1人分が完成）
      studentDetails.add(studentDetail);
    });
    // ⑪全員分が終わったら、完成した箱を返す。
    // （呼び出したServiceが受け取り、Controllerを通して、JSONという文字のデータとしてお客さんに返される）
    return studentDetails;
  }
}

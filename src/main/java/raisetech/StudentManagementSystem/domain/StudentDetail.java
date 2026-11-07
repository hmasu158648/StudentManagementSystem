package raisetech.StudentManagementSystem.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;

/**
 * 受講生詳細（受講生1人分の情報と、その受講生が受講するコース情報の一覧）をまとめたドメインオブジェクトです。
 * データベースの1つのテーブルに対応するのではなく、画面やAPI（JSON）でやり取りするために
 * 「受講生」と「そのコース一覧」を1セットにしたものです。
 */
// @Data：Lombokが、getter・setter・toString・equals・hashCodeを自動で作ってくれる
@Data
// @NoArgsConstructor：引数なしのコンストラクターを自動で作る
//   （new StudentDetail() ができる。リクエストボディのJSONをこのクラスに変換するときにも使われる）
@NoArgsConstructor
// @AllArgsConstructor：全フィールドを引数に取るコンストラクターを自動で作る
//   （new StudentDetail(student, studentCourseList) のように、作成と同時に値をセットできる）
@AllArgsConstructor
public class StudentDetail {

  // 受講生1人分の基本情報（名前・メールアドレスなど）
  private Student student;
  // その受講生が受講するコース情報の一覧（コースは複数持てるのでList）
  private List<StudentCourse> studentCourseList;
}

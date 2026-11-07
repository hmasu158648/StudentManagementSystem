package raisetech.StudentManagementSystem.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;

/**
 * 受講生情報および受講生コース情報を扱うリポジトリです。
 * MyBatisのマッパーとして、studentsテーブルとstudents_coursesテーブルへのSQLを実行します。
 */
// @Mapper：MyBatisがこのインターフェースの実装クラスを自動で作ってくれる目印（自分で実装は書かない）
// このインターフェースにはメソッドの宣言だけがあり、SQLはすべてXMLファイル(mapper/studentRepository.xml)に書かれている
// Serviceがメソッドを呼ぶと、XMLの中で同じ名前(id)のSQLが実行される
@Mapper
public interface StudentRepository {

  /**
   * 受講生を全件検索します（論理削除済みの受講生も含みます）。
   *
   * @return 受講生一覧
   */
  // ① このメソッドにはSQLを書いていない。同じ名前(search)のSQLが、XMLファイル(mapper/studentRepository.xml)に書かれている
  // ② XML側で studentsテーブルの全行・全列を取得し、各行をStudentに詰めてListにまとめて返す
  List<Student> search();

  /**
   * 受講生をidで1人だけ検索します。
   *
   * @param id 受講生ID
   * @return 該当する受講生（存在しない場合はnull）
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(searchStudent)のSQLがXMLファイルに書かれている
  // ② XML側で、引数のid が #{id} の部分に入り、idが一致する1行だけを取得してStudentに詰めて返す（なければnull）
  Student searchStudent(Integer id);

  /**
   * 受講生コース情報の全件検索を行います。
   *
   * @return 受講生コース情報一覧（全件）
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(searchAllStudentCoursesList)のSQLがXMLファイルに書かれている
  // ② XML側で students_coursesテーブルの全行・全列を取得し、各行をStudentCourseに詰めてListにまとめて返す
  List<StudentCourse> searchAllStudentCoursesList();

  /**
   * 受講生IDに紐づく受講生コース情報を検索します。
   *
   * @param studentId 受講生ID
   * @return 該当する受講生コース情報一覧
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(searchStudentCourseList)のSQLがXMLファイルに書かれている
  // ② XML側で、引数のstudentId が #{studentId} の部分に入り、student_idが一致するコース情報だけを取得してListにまとめて返す
  List<StudentCourse> searchStudentCourseList(Integer studentId);

  /**
   * 受講生を新規登録します。
   * 採番されたidは引数のstudentに設定されます。
   *
   * @param student 登録する受講生
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(registerStudent)の<insert>がXMLファイルに書かれている
  // ② XML側で、引数のstudentの各フィールド（#{name}など）の値を、VALUESの対応する位置に入れる
  // ③ studentsテーブルに1行追加する。削除フラグ(is_deleted)は引数の値ではなく、SQLに直接書かれた false（未削除）で登録される
  // ④ 追加後、自動採番されたidがstudentのidフィールドに反映される（XMLのuseGeneratedKeys / keyProperty。後でコース登録に使える）
  void registerStudent(Student student);

  /**
   * 受講生コース情報を新規登録します。
   * コース開始日時・終了日時は引数の値をそのまま登録し、採番されたidは引数に設定されます。
   *
   * @param studentCourse 登録する受講生コース情報
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(registerStudentCourse)の<insert>がXMLファイルに書かれている
  // ② XML側で、引数のstudentCourseの値（#{studentId}、#{courseName}、#{courseStartAt}、#{courseEndAt}）を、
  //    VALUESの対応する位置に入れる（列の並びとVALUESの並びが同じ順番で対応する）
  // ③ students_coursesテーブルに1行追加する（開始日時・終了日時はServiceでセットされた値が入る）
  // ④ 追加後、自動採番されたidがstudentCourseのidフィールドに反映される（XMLのuseGeneratedKeys / keyProperty）
  void registerStudentCourse(StudentCourse studentCourse);

  /**
   * 受講生情報を更新します。
   * 削除フラグ(is_deleted)も更新対象のため、論理削除にも利用できます。
   *
   * @param student 更新する受講生（idで更新対象を特定）
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(updateStudent)の<update>がXMLファイルに書かれている
  // ② XML側で、引数のstudentの各フィールドの値を、SETの各列に入れる（削除フラグis_deletedも含む）
  // ③ WHEREのidが一致する1行だけを更新する（idを指定しないと全行が更新されてしまう）
  void updateStudent(Student student);

  /**
   * 受講生コース情報のコース名を更新します。
   *
   * @param studentCourse 更新する受講生コース情報（idで更新対象を特定）
   */
  // ① このメソッドにもSQLを書いていない。同じ名前(updateStudentCourse)の<update>がXMLファイルに書かれている
  // ② XML側で、引数（studentCourse）のcourseNameを、SETのcourse_nameに入れる
  // ③ WHEREのidが一致するコース1行だけを更新する（コース名以外の列は変わらない）
  void updateStudentCourse(StudentCourse studentCourse);
}

package raisetech.StudentManagementSystem.repository;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;

/**
 * 受講生情報および受講生コース情報を扱うリポジトリです。
 * MyBatisのマッパーとして、studentsテーブルとstudents_coursesテーブルへのSQLを実行します。
 */
// @Mapper：MyBatisがこのインターフェースの実装クラスを自動で作ってくれる目印（自分で実装は書かない）
// Serviceがメソッドを呼ぶと、付けられたアノテーションのSQLが実行される
@Mapper
public interface StudentRepository {

  /**
   * 受講生を全件検索します（論理削除済みの受講生も含みます）。
   *
   * @return 受講生一覧
   */
  // ① studentsテーブルの全行・全列を取得する
  // ② 取得した各行は、列名とフィールド名を対応させてStudentに詰められ、Listにまとめて返る
  @Select("SELECT * FROM students")
  List<Student> search();

  /**
   * 受講生をidで1人だけ検索します。
   *
   * @param id 受講生ID
   * @return 該当する受講生（存在しない場合はnull）
   */
  // ① 引数のid が #{id} の部分に入る
  // ② idが一致する1行だけを取得し、Studentに詰めて返す（なければnull）
  @Select("SELECT * FROM students WHERE id = #{id}")
  Student searchStudent(Integer id);

  /**
   * 受講生コース情報の全件検索を行います。
   *
   * @return 受講生コース情報一覧（全件）
   */
  // ① students_coursesテーブルの全行・全列を取得する
  // ② 各行をStudentCourseに詰め、Listにまとめて返す
  @Select("SELECT * FROM students_courses")
  List<StudentCourse> searchAllStudentCoursesList();

  /**
   * 受講生IDに紐づく受講生コース情報を検索します。
   *
   * @param studentId 受講生ID
   * @return 該当する受講生コース情報一覧
   */
  // ① 引数のstudentId が #{studentId} の部分に入る
  // ② student_idが一致するコース情報だけを取得し、Listにまとめて返す
  @Select("SELECT * FROM students_courses WHERE student_id = #{studentId}")
  List<StudentCourse> searchStudentCourseList(Integer studentId);

  /**
   * 受講生を新規登録します。
   * 採番されたidは引数のstudentに設定されます。
   *
   * @param student 登録する受講生
   */
  // ① 引数のstudentの各フィールド（#{name}など）の値を、VALUESの対応する位置に入れる
  // ② studentsテーブルに1行追加する（SQL内にコメントは書けないため、ここで説明する）
  //    削除フラグ(is_deleted)は引数の値ではなく、SQLに直接書かれた false（未削除）で登録される
  @Insert("""
      INSERT INTO students(name, kana_name, nickname,email,area,age,sex,remark,is_deleted)
      VALUES(#{name}, #{kanaName},#{nickname},#{email},#{area},#{age},#{sex},#{remark},
      false)
      """)
  // ③ 追加後、データベースが自動採番したidを取得する
  // ④ 自動採番されたidをstudentのidフィールドに反映する（後でコース登録に使える）
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void registerStudent(Student student);

  /**
   * 受講生コース情報を新規登録します。
   * コース開始日時・終了日時は引数の値をそのまま登録し、採番されたidは引数に設定されます。
   *
   * @param studentCourse 登録する受講生コース情報
   */
  // ① 引数のstudentCourseの値（#{studentId}、#{courseName}、#{courseStartAt}、#{courseEndAt}）を、
  //    VALUESの対応する位置に入れる（列の並びとVALUESの並びが同じ順番で対応する）
  // ② students_coursesテーブルに1行追加する（開始日時・終了日時はServiceでセットされた値が入る）
  @Insert("""
      INSERT INTO students_courses(student_id, course_name,course_start_at,course_end_at)
      VALUES(#{studentId}, #{courseName}, #{courseStartAt},#{courseEndAt})
      """)
  // ③ 追加後、データベースが自動採番したidを取得する
  // ④ 自動採番されたidをstudentCourseのidフィールドに反映する
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void registerStudentCourse(StudentCourse studentCourse);

  /**
   * 受講生情報を更新します。
   * 削除フラグ(is_deleted)も更新対象のため、論理削除にも利用できます。
   *
   * @param student 更新する受講生（idで更新対象を特定）
   */
  // ① 引数のstudentの各フィールドの値を、SETの各列に入れる（削除フラグis_deletedも含む）
  // ② WHEREのidが一致する1行だけを更新する（idを指定しないと全行が更新されてしまう）
  @Update("""
          UPDATE students
          SET name=#{name}, kana_name=#{kanaName},nickname=#{nickname},email=#{email},area=#{area},age=#{age},sex=#{sex},remark=#{remark}, is_deleted = #{isDeleted}
          WHERE id=#{id}
      """)
  void updateStudent(Student student);

  /**
   * 受講生コース情報のコース名を更新します。
   *
   * @param studentsCourse 更新する受講生コース情報（idで更新対象を特定）
   */
  // ① 引数（studentsCourse）のcourseNameを、SETのcourse_nameに入れる
  // ② WHEREのidが一致するコース1行だけを更新する（コース名以外の列は変わらない）
  @Update("""
      UPDATE students_courses
      SET course_name=#{courseName}
      WHERE id = #{id}
      """)
  void updateStudentCourse(StudentCourse studentsCourse);
}

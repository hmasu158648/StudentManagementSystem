package raisetech.StudentManagementSystem.repository;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;

/**
 * 受講生情報および受講生コース情報を扱うリポジトリです。
 * MyBatisのマッパーとして、studentsテーブルとstudents_coursesテーブルへのSQLを実行します。
 */
@Mapper
public interface StudentRepository {

  /**
   * 論理削除されていない受講生を全件検索します。
   *
   * @return 受講生一覧（削除済みを除く）
   */
  @Select("SELECT * FROM students WHERE is_deleted = false")
  List<Student> search();

  /**
   * 受講生をidで1人だけ検索します。
   *
   * @param id 受講生ID
   * @return 該当する受講生（存在しない場合はnull）
   */
  @Select("SELECT * FROM students WHERE id = #{id}")
  Student searchStudent(Integer id);

  /**
   * 受講生コース情報の全件検索を行います。
   *
   * @return 受講生コース情報一覧（全件）
   */
  @Select("SELECT * FROM students_courses")
  List<StudentsCourses> searchStudentCoursesList();

  /**
   * 受講生IDに紐づく受講生コース情報を検索します。
   *
   * @param studentId 受講生ID
   * @return 該当する受講生コース情報一覧
   */
  @Select("SELECT * FROM students_courses WHERE student_id = #{studentId}")
  List<StudentsCourses> searchStudentCourses(Integer studentId);

  /**
   * 受講生を新規登録します。
   * 削除フラグ(is_deleted)は登録時に必ずFALSEとし、採番されたidは引数のstudentに設定されます。
   *
   * @param student 登録する受講生
   */
  @Insert("""
      INSERT INTO students(name, kana_name, nickname,email,area,age,sex,remark,is_deleted)
      VALUES(#{name}, #{kanaName},#{nickname},#{email},#{area},#{age},#{sex},#{remark},FALSE)
      """)
  // 自動採番されたidをstudentのidフィールドに反映する
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void registerStudent(Student student);

  /**
   * 受講生コース情報を新規登録します。
   * コース終了日時は登録時点では未定のためNULLとし、採番されたidは引数に設定されます。
   *
   * @param studentCourses 登録する受講生コース情報
   */
  @Insert("""
      INSERT INTO students_courses(student_id, course_name,course_start_at,course_end_at)
      VALUES(#{studentId}, #{courseName}, #{courseStartAt},NULL)
      """)
  // 自動採番されたidをstudentCoursesのidフィールドに反映する
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void registerStudentCourse(StudentsCourses studentCourses);

  /**
   * 受講生情報を更新します。
   * 削除フラグ(is_deleted)も更新対象のため、論理削除にも利用できます。
   *
   * @param student 更新する受講生（idで更新対象を特定）
   */
  @Update("""
          UPDATE students
          SET name=#{name}, kana_name=#{kanaName},nickname=#{nickname},email=#{email},area=#{area},age=#{age},sex=#{sex},remark=#{remark}, is_deleted = #{isDeleted}
          WHERE id=#{id}
      """)
  void updateStudent(Student student);

  /**
   * 受講生コース情報のコース名を更新します。
   *
   * @param studentsCourses 更新する受講生コース情報（idで更新対象を特定）
   */
  @Update("""
      UPDATE students_courses
      SET course_name=#{courseName}
      WHERE id = #{id}
      """)
  void updateStudentCourse(StudentsCourses studentsCourses);
}

package raisetech.StudentManagementSystem.repository;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentCourse;

/**
 * 受講生情報を扱うリポジトリ。
 * <p>
 * * 全件検索や単一条件での検索、コース情報の検索が行えるクラスです。
 */

@Mapper
public interface StudentRepository {

  /**
   * 全件検索します。awww
   *
   * @return 全件検索した受講生情報の一覧
   */
  @Select("SELECT * FROM students")
  List<Student> searchStudents();

  @Select("SELECT * FROM students_courses")
  List<StudentCourse> searchStudentCourses();
}


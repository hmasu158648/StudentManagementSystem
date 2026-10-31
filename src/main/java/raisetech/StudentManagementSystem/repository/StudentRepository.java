package raisetech.StudentManagementSystem.repository;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import raisetech.StudentManagementSystem.data.Student;
import raisetech.StudentManagementSystem.data.StudentsCourses;

@Mapper
public interface StudentRepository {

  @Select("SELECT * FROM students")
  List<Student> search();

  @Select("SELECT * FROM students_courses")
  List<StudentsCourses> searchStudentCourses();

  @Insert("""
      INSERT INTO students(name, kana_name, nickname,email,area,age,sex,remark,is_deleted)
      VALUES(#{name}, #{kanaName},#{nickname},#{email},#{area},#{age},#{sex},#{remark},FALSE)
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void registerStudent(Student student);

  @Insert("""
      INSERT INTO students_courses(student_id, course_name,course_start_at,course_end_at)
      VALUES(#{studentId}, #{courseName}, #{courseStartAt},NULL)
      """)
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void registerStudentCourse(StudentsCourses studentCourses);
}


package raisetech.StudentManagementSystem.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({"id", "name", "kanaName", "nickname", "email", "area", "age", "sex"})
public class Student {

  private Integer id;
  private String name;
  private String kanaName;
  private String nickname;
  private String email;
  private String area;
  private Integer age;
  private String sex;
}

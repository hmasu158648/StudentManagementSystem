package raisetech.StudentManagementSystem.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 受講生コース情報（1人の受講生が受講する1つのコース）を表すクラスです。
 * データベースのstudents_coursesテーブルの1行に対応します。
 * 受講生1人に対して、コースは複数持てます（studentIdで受講生と結びつきます）。
 */
// @Schema：Swagger UI（API仕様書の画面）に表示する、このクラスの説明（処理の動きには影響しない）
@Schema(description = "受講生コース情報")
// @Data：Lombokが、getter・setter・toString・equals・hashCodeを自動で作ってくれる
// （getCourseName()やsetStudentId(...)などを自分で書かなくてよい）
@Data
// @JsonPropertyOrder：JSONにしてレスポンスで返すときの、項目の並び順を指定する
// （id → studentId → courseName → courseStartAt → courseEndAt の順で出力される）
@JsonPropertyOrder({"id", "studentId", "courseName", "courseStartAt", "courseEndAt"})
public class StudentCourse {

  // コースを区別するためのID（データベースが自動で採番する。students_coursesテーブルのid列）
  @Schema(description = "コースID（データベースが自動採番）", example = "1")
  private Integer id;

  // このコースを受講している受講生のID（studentsテーブルのidと同じ値。student_id列）
  @Schema(description = "受講生ID（studentsテーブルのid）", example = "1")
  private Integer studentId;

  // コース名（例：Javaコースなど。course_name列）
  // @NotBlank：空・空白だけは不可（必須）／ @Size(max = 50)：50文字以内
  @Schema(description = "コース名", example = "Javaコース")
  @NotBlank
  @Size(max = 50)
  private String courseName;

  // コースの開始日時（登録時にServiceが「今」をセットする。course_start_at列）
  @Schema(description = "コース開始日時（登録時に自動でセット）", example = "2026-11-12T10:00:00")
  private LocalDateTime courseStartAt;

  // コースの終了日時（登録時にServiceが「今から1年後」をセットする。course_end_at列）
  @Schema(description = "コース終了日時（登録時に開始日時の1年後を自動でセット）", example = "2027-11-12T10:00:00")
  private LocalDateTime courseEndAt;
}

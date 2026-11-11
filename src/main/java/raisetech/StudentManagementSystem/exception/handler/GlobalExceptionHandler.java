package raisetech.StudentManagementSystem.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import raisetech.StudentManagementSystem.exception.TestException;

/**
 * 全コントローラー共通の例外処理クラスです。
 * どのコントローラーで例外が発生しても、ここで受け止めて、利用者に返すエラー内容（ステータスとメッセージ）を決めます。
 * コントローラーごとに同じ例外処理を書かなくてよいため、処理が1か所にまとまります。
 *
 * <p>【例外が発生したときの流れ】
 * <ol>
 *   <li>コントローラーのメソッドで TestException が投げられる（例：GET /testException）</li>
 *   <li>Springが、この例外を受け止められるメソッド（下の handleTestException）を探して呼ぶ</li>
 *   <li>ログに警告を出力し、利用者にはステータス400とメッセージを返す</li>
 * </ol>
 */
// @Slf4j：Lombokが、ログ出力用の log を自動で用意してくれる（log.warn(...) などが使える）
@Slf4j
// @RestControllerAdvice：全コントローラーで発生した例外を、ここでまとめて処理する目印
// （戻り値は、画面ではなくレスポンスボディ（JSONや文字列）として返される）
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * TestExceptionを受け止め、400とメッセージを返します。
   *
   * @param exception 発生したTestException
   * @return ステータス400（Bad Request）と、例外のメッセージ
   */
  // ① @ExceptionHandler：TestExceptionが投げられたら、このメソッドが呼ばれる
  @ExceptionHandler(TestException.class)
  public ResponseEntity<String> handleTestException(TestException exception) {
    // ② サーバー側のログに、警告（WARN）として例外の内容を出力する
    //    {} の部分に exception.getMessage() の値が入り、最後に exception を渡すと、発生場所（スタックトレース）も出力される
    log.warn("TestExceptionが発生しました：{}", exception.getMessage(), exception);
    // ③ 利用者側には、ステータス400（badRequest）と、例外のメッセージだけを返す
    //    （内部の詳細はログにだけ残し、利用者には見せない）
    return ResponseEntity.badRequest().body(exception.getMessage());
  }
}

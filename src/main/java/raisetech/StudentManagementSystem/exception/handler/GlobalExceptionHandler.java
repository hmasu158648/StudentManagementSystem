package raisetech.StudentManagementSystem.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import raisetech.StudentManagementSystem.exception.TestException;

/**
 * 全コントローラー共通の例外処理クラスです。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * TestExceptionを受け止め、400とメッセージを返します。
   */
  @ExceptionHandler(TestException.class)
  public ResponseEntity<String> handleTestException(TestException exception) {
    log.warn("TestExceptionが発生しました：{}", exception.getMessage(), exception);
    return ResponseEntity.badRequest().body(exception.getMessage());
  }
}

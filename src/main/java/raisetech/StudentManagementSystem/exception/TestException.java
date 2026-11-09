package raisetech.StudentManagementSystem.exception;

/**
 * 例外の動作確認用に作った、独自の例外クラスです。
 * Exceptionを継承した「チェック例外」なので、投げるメソッドには throws の宣言が必要です。
 * 発生すると、共通の例外処理クラス（GlobalExceptionHandler）が受け取り、ステータス400でメッセージを返します。
 */
public class TestException extends Exception {

  // メッセージも原因も指定しない、もっとも簡単なコンストラクター
  public TestException() {
    super();
  }

  // エラーの内容を表すメッセージを指定するコンストラクター（例：new TestException("利用できません")）
  public TestException(String message) {
    super(message);
  }

  // cause（原因）は、この例外のもとになった別の例外を持たせるためのものです。
  // ログに出るスタックトレースでは、一番下の「Caused by」が根本原因です。
  public TestException(String message, Throwable cause) {
    super(message, cause);
  }

  // 原因となった例外だけを指定するコンストラクター（メッセージは原因の例外から自動で作られる）
  public TestException(Throwable cause) {
    super(cause);
  }
}

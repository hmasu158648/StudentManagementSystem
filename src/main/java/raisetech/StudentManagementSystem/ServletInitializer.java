package raisetech.StudentManagementSystem;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * WARファイルとして外部のTomcatなどにデプロイするときの、アプリケーション起動の入り口となるクラスです。
 * 普段のmainメソッド(Application)での起動ではなく、外部のサーバーがアプリを起動するときに使われます。
 */
// SpringBootServletInitializerを継承：外部のサーバー(Tomcatなど)からSpring Bootアプリを起動できるようにする
public class ServletInitializer extends SpringBootServletInitializer {

	// 親クラスのconfigureメソッドを上書きして、起動時の設定を自分で指定する
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// ① 起動の元になるクラスとして Application.class を指定する
		// ② 指定した設定を持つビルダーを返し、サーバーがこれをもとにアプリを起動する
		return application.sources(Application.class);
	}

}

package minwebsrv.webserver;

// https://docs.oracle.com/javase/jp/6/api/java/net/ServerSocket.html
import java.net.ServerSocket;
// https://docs.oracle.com/javase/jp/6/api/java/net/Socket.html
import java.net.Socket;

import minwebsrv.servletimpl.WebApplication;

public class Main {

	public static void main(String[] args) throws Exception {

		System.out.println("miniWebSrv launching...");

		// ServletコンテナにWebApplicationサーブレットを登録
		WebApplication app = WebApplication.createInstance( "testbbs" );
		app.addServlet("/ShowBBS", "ShowBBS");
        app.addServlet("/PostBBS", "PostBBS");

		try( ServerSocket server = new ServerSocket(8001) ){

			for( ;; ) {

				// このソケットに対する接続要求を待機
				// 接続が行われるまで処理をブロック
				Socket socket = server.accept();

				// クライアントソケットを渡して、サーバスレッドを起動
				// ソケットへの通信が来るたびにスレッドが作成される
				ServerThread serverThread = new ServerThread( socket );
				Thread thread = new Thread( serverThread );
				thread.start();

			}

		}
	}

}

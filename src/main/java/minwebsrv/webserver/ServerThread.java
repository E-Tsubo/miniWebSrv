package minwebsrv.webserver;

import static java.lang.System.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import minwebsrv.servletimpl.ServletInfo;
import minwebsrv.servletimpl.ServletService;
import minwebsrv.servletimpl.WebApplication;
import minwebsrv.util.MyURLDecoder;
import minwebsrv.util.SendResponse;
import minwebsrv.util.Util;

// https://docs.oracle.com/javase/jp/6/api/java/lang/Runnable.html
// インスタンスを 1 つのスレッドで実行するすべてのクラスでは、
// Runnable インタフェースを実装する必要があります。
// このクラスは、引数のないメソッド run を定義しなければいけません。
class ServerThread implements Runnable {

	//Serverパラメータ
	private static final String DOCUMENT_ROOT = "C:\\dev\\work\\httpdocs";
	private static final String ERROR_DOCUMENT = "C:\\dev\\work\\httpdocs";
	private static final String SERVER_NAME = "localhost:8001";
	//private static final String SERVER_LOG = "C:\\dev\\work\\httpdocs\\http.log";

	private Socket socket;

	private static int thread_num = 0;

	ServerThread(Socket socket) {

		this.socket = socket;
	}

	private static void addRequestHeader(Map<String, String> requestHeader, String line) {
		int colonPos = line.indexOf(':');
		if (colonPos == -1)
			return;

		String headerName = line.substring(0, colonPos).toUpperCase();
		String headerValue = line.substring(colonPos + 1).trim();
		requestHeader.put(headerName, headerValue);
	}

	@Override
	public void run() {

		// デバッグコード
		int threaqd_num_saved = ++thread_num;
		out.println( "-Create Thread Num " + threaqd_num_saved );

		OutputStream output = null;

		try {

			// ソケットからバイトを読み込むための入力ストリーム
			InputStream input = socket.getInputStream();

			String line;
			String requestLine = null;
			String method = null;
			Map<String, String> requestHeader = new HashMap<String, String>();

			/*// 旧ソースコード.サーブレット対応により廃止
			String path = null;
			String ext = null;
			String host = null;
			 */

			// ヘッダ行の読み込み
			while ((line = Util.readLine(input)) != null) {

				// ヘッダ/ボディの境目である空白行にてbreak
				if (line.equals("")) {
					break;
				}

				// GETリクエストヘッダからアクセス先URLを抽出
				// 以下のように空白でsplit
				// GET /index.html HTTP/1.1
				// extは拡張子を取得
				if (line.startsWith("GET")) {
					/*// 旧ソースコード.サーブレット対応により廃止
					path = MyURLDecoder.decode(line.split(" ")[1], "UTF-8");
					String[] tmp = path.split("\\.");
					ext = tmp[tmp.length - 1];
					*/

					method = "GET";
					requestLine = line;
				}
				// POST要求への返答
				else if (line.startsWith("POST")) {
					method = "POST";
					requestLine = line;
				}
				else {
					// // 旧ソースコード.サーブレット対応により廃止
					//host = line.substring("Host: ".length());
					addRequestHeader(requestHeader, line);
				}
			}

			//if( path == null ) {
			if (requestLine == null) {
				return;
			}


			String reqUri = MyURLDecoder.decode(requestLine.split(" ")[1], "UTF-8");
			String[] pathAndQuery = reqUri.split("\\?");
			String path = pathAndQuery[0];
			String query = null;
			if (pathAndQuery.length > 1) {
				query = pathAndQuery[1];
			}


			// レスポンス用にアウトプットストリームを用意
			output = new BufferedOutputStream(socket.getOutputStream());

			// 要求URLに対応したWebアプリケーションを検索
			String appDir = path.substring(1).split("/")[0];
            WebApplication webApp = WebApplication.searchWebApplication(appDir);
            if (webApp != null) {
            	// Webアプリケーション内から、アクセス先となっているサーブレットをロード
                ServletInfo servletInfo
                    = webApp.searchServlet(path.substring(appDir.length() + 1));
                if (servletInfo != null) {
                	// サーブレットの処理を実行
                    ServletService.doService(method, query, servletInfo, requestHeader, input, output);
                    return;
                }
            }

            String ext = null;
            String[] tmp = reqUri.split("\\.");
            ext = tmp[tmp.length - 1];
            if (path.endsWith("/")) {
				path += "index.html";
				ext = "html";
			}

			// アクセス先の静的ファイルを取得
			FileSystem fs = FileSystems.getDefault();
			Path pathObj = fs.getPath(DOCUMENT_ROOT + path);
			Path realPath;

			try {
				realPath = pathObj.toRealPath();
			}
			// 該当ファイルが見つからない場合は、404ステータスでエラーページを返す
			// 専用の関数があるみたい
			catch (NoSuchFileException ex) {

				SendResponse.sendNotFoundResponse(output, ERROR_DOCUMENT);
				return;
			}

			// ディレクトリサーバル対策で、DOCUMENT_ROOT以外のパスにはエラーを返す
			if (!realPath.startsWith(DOCUMENT_ROOT)) {

				SendResponse.sendNotFoundResponse(output, ERROR_DOCUMENT);
				return;
			}
			// ディレクトリの場合は、/を付けてリダイレクトさせる
			// ApacheでいうDirectoryIndex
			else if (Files.isDirectory(realPath)) {

				String host = requestHeader.get("HOST");
				String location = "http://"
						+ ((host != null) ? host : SERVER_NAME)
						+ path + "/";
				SendResponse.sendMovePermanentlyResponse(output, location);
				return;
			}

			try (InputStream fis = new BufferedInputStream(Files.newInputStream(realPath))) {
				SendResponse.sendOkResponse(output, fis, ext);
			} catch (FileNotFoundException ex) {
				SendResponse.sendNotFoundResponse(output, ERROR_DOCUMENT);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

			try {

				if (output != null) {
					output.close();
				}
				socket.close();

				// デバッグコード
				out.println( "|->Close Thread Num " + threaqd_num_saved );

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}

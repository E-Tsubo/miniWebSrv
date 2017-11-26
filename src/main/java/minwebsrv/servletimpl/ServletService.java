package minwebsrv.servletimpl;

import static java.lang.System.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import minwebsrv.servlet.http.HttpServlet;
import minwebsrv.servlet.http.HttpServletRequest;
import minwebsrv.servlet.http.HttpServletResponse;
import minwebsrv.util.Constants;
import minwebsrv.util.SendResponse;

public class ServletService {

	private static HttpServlet createServlet( ServletInfo info ) throws Exception {

		// デバッグコード
		out.println( "--" + "Create Servlet Instance : " + info.servletClassName );

		Class<?> clazz = info.webApp.classLoader.loadClass(info.servletClassName);
		return (HttpServlet)clazz.newInstance();
	}

	// ServerThreadから起動されるメソッド
	public static void doService(String method, String query, ServletInfo info,
            						Map<String, String> requestHeader,
            						InputStream input, OutputStream output) throws Exception {

		// 初回呼び出し時にサーブレットのインスタンスを作成
		if (info.servlet == null) {
			info.servlet = createServlet(info);
		}

		// サーブレットの出力をバッファリングするためのストリームを用意
		ByteArrayOutputStream outputBuffer =  new ByteArrayOutputStream();
		HttpServletResponseImpl resp = new HttpServletResponseImpl(outputBuffer);

		// リクエスト要求に従って処理を分岐
		HttpServletRequest req;
		if (method.equals("GET")) {
			// GETリクエストのクエリストリングをMap型変数に格納して、サーブレットメソッドへ引き渡し
			Map<String, String[]> map;
			map = stringToMap(query);
			req = new HttpServletRequestImpl("GET", map);
		}
		else if (method.equals("POST")) {
			// POSTリクエストのボディの情報をContent-Lengthで指定されたバイト数読み込み
			// 内容をMap型変数へ格納
			int contentLength = Integer.parseInt(requestHeader.get("CONTENT-LENGTH"));
			Map<String, String[]> map;
			String line = readToSize(input, contentLength);
			map = stringToMap(line);
			req = new HttpServletRequestImpl("POST", map);
		}
		else {
			throw new AssertionError("BAD METHOD:" + method);
		}

		// サーブレットのメイン処理を起動
		// minwebsrv/servlet/http/httpservletを継承していれば、
		// serviceメソッド内で、doGET/doPOSTに分岐され、
		// WebApp側で実装された処理が起動される
		info.servlet.service(req, resp);

		// ステータス200を返却
		if (resp.status == HttpServletResponse.SC_OK) {
			SendResponse.sendOkResponseHeader(output, resp.contentType);
			resp.printWriter.flush();
			byte[] outputBytes = outputBuffer.toByteArray();
			for (byte b: outputBytes) {
				output.write((int)b);
			}
		}
		// ステータス404を返却
		else if (resp.status == HttpServletResponse.SC_FOUND) {
			String redirectLocation;
			if (resp.redirectLocation.startsWith("/")) {
				String host = requestHeader.get("HOST");
				redirectLocation = "http://"
						+ ((host != null) ? host : Constants.SERVER_NAME)
						+ resp.redirectLocation;
			} else {
				redirectLocation = resp.redirectLocation;
			}
			SendResponse.sendFoundResponse(output, redirectLocation);
		}
	}

	// HTTPのGET/POSTリクエストのクエリストリング/ボディを分解し、Map型変数に格納するメソッド
	// &でクエリを区切り、=で分解することで、HashMapへ落とし込んでいる.
    private static Map<String, String[]> stringToMap(String str) {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        if (str != null) {
            String[] paramArray = str.split("&");
            for (String param : paramArray) {
                String[] keyValue = param.split("=");
                if (parameterMap.containsKey(keyValue[0])) {
                    String[] array = parameterMap.get(keyValue[0]);
                    String[] newArray = new String[array.length + 1];
                    System.arraycopy(array, 0, newArray, 0, array.length);
                    newArray[array.length] = keyValue[1];
                    parameterMap.put(keyValue[0], newArray);
                } else {
                    parameterMap.put(keyValue[0], new String[] {keyValue[1]});
                }
            }
        }
        return parameterMap;
    }

    private static String readToSize(InputStream input, int size)
            throws Exception{
        int ch;
        StringBuilder sb = new StringBuilder();
        int readSize = 0;

        while (readSize < size && (ch = input.read()) != -1) {
            sb.append((char)ch);
            readSize++;
        }
        return sb.toString();
    }
}

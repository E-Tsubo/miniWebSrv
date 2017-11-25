package minwebsrv.servlet.http;

import minwebsrv.servlet.ServletException;

// java.lang.Object
// 上位を拡張 javax.servlet.GenericServlet
// 上位を拡張 javax.servlet.http.HttpServlet
// http://mergedoc.osdn.jp/tomcat-servletapi-5-ja/javax/servlet/http/HttpServlet.html
// 上記、Javaの標準サーブレットクラスをクローン
// 差分はメソッドを減らしているところ.(必要最低限)
public class HttpServlet {
	protected void doGet( HttpServletRequest req, HttpServletResponse resp )
				throws ServletException, java.io.IOException {

    }

	protected void doPost( HttpServletRequest req, HttpServletResponse resp )
            	throws ServletException, java.io.IOException {
    }

	public void service( HttpServletRequest req, HttpServletResponse resp )
			throws ServletException, java.io.IOException {

		if (req.getMethod().equals("GET")) {
			doGet(req, resp);
		}
		else if( req.getMethod().equals("POST") ) {
			doPost(req, resp);
		}

	}

}

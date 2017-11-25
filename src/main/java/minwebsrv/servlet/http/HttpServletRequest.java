package minwebsrv.servlet.http;

import java.io.UnsupportedEncodingException;

// インターフェースとして、必要な処理を定義
// 実装はServletimplにて実施
public interface HttpServletRequest {
	String getMethod();
    String getParameter(String name);
    String[] getParameterValues(String name);
    void setCharacterEncoding(String env) throws UnsupportedEncodingException;
}

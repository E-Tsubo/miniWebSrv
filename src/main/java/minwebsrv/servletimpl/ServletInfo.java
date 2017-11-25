package minwebsrv.servletimpl;

import minwebsrv.servlet.http.HttpServlet;


// 1つのWebアプリケーションは複数のサーブレットから構成される。
// 1URL,1サーブレットに対応するため。
// Webアプリケーション内の(本プログラムではWebApplication)サーブレット管理に利用されるクラス
public class ServletInfo {

	WebApplication webApp;
    String urlPattern;
    String servletClassName;
    HttpServlet servlet; //サーブレット本体

    public ServletInfo( WebApplication webApp, String urlPattern, String servletClassName ) {
        this.webApp = webApp;
        this.urlPattern = urlPattern;
        this.servletClassName = servletClassName;
    }
}

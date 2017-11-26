package minwebsrv.servletimpl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


// Webアプリケーション全体を管理するクラス
// 内部に複数のサーブレットを保持する.
public class WebApplication {

	private static String WEBAPPS_DIR = "C:\\dev\\work\\httpdocs\\webapps";

	// 複数のWebアプリケーションクラスを保持可能
    private static Map<String, WebApplication> webAppCollection = new HashMap<String, WebApplication>();
    String directory;
    ClassLoader classLoader;

    // 複数のサーブレット利用可能
    private Map<String, ServletInfo> servletCollection = new HashMap<String, ServletInfo>();

    // 該当パス上のClassファイルをロードする処理を公司とラスタで実行
    private WebApplication(String dir) throws MalformedURLException {
        this.directory = dir;
        FileSystem fs = FileSystems.getDefault();

        Path pathObj = fs.getPath(WEBAPPS_DIR + File.separator + dir);
        this.classLoader = URLClassLoader.newInstance(new URL[]{pathObj.toUri().toURL()});
    }

    public static WebApplication createInstance(String dir) throws MalformedURLException {
        WebApplication newApp = new WebApplication(dir);
        webAppCollection.put(dir, newApp);

        return newApp;
    }

    public void addServlet(String urlPattern, String servletClassName) {
        this.servletCollection.put(urlPattern,
                                   new ServletInfo(this, urlPattern,
                                                   servletClassName));
    }

    public ServletInfo searchServlet(String path) {
        return servletCollection.get(path);
    }

    public static WebApplication searchWebApplication(String dir) {
        return webAppCollection.get(dir);
    }
}

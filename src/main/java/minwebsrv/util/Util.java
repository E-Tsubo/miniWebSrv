package minwebsrv.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class Util {

	// 受信したデータを行単位で読み込むためのメソッド
	public static String readLine( InputStream input ) throws Exception {

		int ch;
		String ret = "";

		while( ( ch = input.read() ) != -1 ) {

			if( ch == '\r' ) {
				// HTTPのHEADとBODYの境界を示す空白
				// この行はスキップするのみ
			}
			else if( ch == '\n' ) {
				break;
			}
			else {
				ret += (char)ch;
			}

		}

		if( ch == -1 ) {
			return null;
		}
		else {
			return ret;
		}
	}

	// レスポンス送信のために、1行の文字列を、バイト列として変換してOutputStreamへ書き込む。
	public static void writeLine( OutputStream output, String str ) throws IOException {

		for( char ch : str.toCharArray() ) {
			output.write( (int)ch );
		}

		output.write( (int)'\r' );
		output.write( (int)'\n' );
	}

	// 現在時刻から、HTTP仕様に合わせてフォーマットされた日付け文字列を返却
	public static	String getDateStringUtc() {

		Calendar cal = Calendar.getInstance( TimeZone.getTimeZone("UTC") );
		DateFormat df = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss", Locale.US );
		df.setTimeZone( cal.getTimeZone() );

		return df.format( cal.getTime() ) + " GMT";
	}

	// 拡張子に応じたContent-Typeを設定するためのメソッド
	public static String getContentType( String str ) {

		String ret = contentTypeMap.get( str.toLowerCase() );
		if( ret == null ) {
			return "application/octet-stream";
		}
		else {
			return ret;
		}
	}

	// 拡張子とContent-Typeの変換表
	private static final HashMap<String, String> contentTypeMap = new HashMap<String, String>() {

		private static final long serialVersionUID = 1L;
		{
			put("html", "text/html");
			put("htm", "text/html");
			put("txt", "text/plain");
			put("css", "text/css");
			put("png", "image/png");
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("gif", "image/gif");
		}
	};

}

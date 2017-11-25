package minwebsrv.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class MyURLDecoder {
    // 16進数2桁をASCIIコードで示すbyteを、intに変換する。
    private static int hex2int(byte b1, byte b2) {
        int digit;
        if (b1 >= 'A') {
            // 0xDFとの&で小文字を大文字に変換する
            digit = (b1 & 0xDF) - 'A' + 10;
        } else {
            digit = (b1 - '0');
        }
        digit *= 16;
        if (b2 >= 'A') {
            digit += (b2 & 0xDF) - 'A' + 10;
        } else {
            digit += b2 - '0';
        }

        return digit;
    }

    public static String decode(String src, String enc)
      throws UnsupportedEncodingException {
        byte[] srcBytes = src.getBytes("ISO_8859_1");
        // 変換後の方が長くなることはないので、srcBytesの
        // 長さの配列をいったん確保する。
        byte[] destBytes = new byte[srcBytes.length];

        int destIdx = 0;
        for (int srcIdx = 0; srcIdx < srcBytes.length; srcIdx++) {
            if (srcBytes[srcIdx] == (byte)'%') {
                destBytes[destIdx] = (byte)hex2int(srcBytes[srcIdx + 1],
                                                   srcBytes[srcIdx + 2]);
                srcIdx += 2;
            } else {
                destBytes[destIdx] = srcBytes[srcIdx];
            }
            destIdx++;
        }
        byte[] destBytes2 = Arrays.copyOf(destBytes, destIdx);

        return new String(destBytes2, enc);
    }
}

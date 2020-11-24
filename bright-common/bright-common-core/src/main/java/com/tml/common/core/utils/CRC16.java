package com.tml.common.core.utils;

/**
 * @author JacksonTu
 * @version 1.0
 * @description 16进制循环冗余校验码
 * @since 2020/9/4 11:19
 */
public class CRC16 {

    private static int[] strToToHexByte(String hexString) {
        hexString = hexString.replace(" ", "");
        if ((hexString.length() % 2) != 0) {
            hexString += " ";
        }

        int[] returnBytes = new int[hexString.length() / 2];

        for (int i = 0; i < returnBytes.length; i++)
            returnBytes[i] = (0xff & Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16));
        return returnBytes;
    }

    public static long GetModBusCRC(String DATA) {
        long functionReturnValue = 0;
        long i = 0;
        long J = 0;
        int[] v = null;
        v = strToToHexByte(DATA);
        long CRC = 0;
        CRC = 0xffffL;
        for (i = 0; i <= (v).length - 1; i++) {
            CRC = (CRC / 256) * 256L + (CRC % 256L) ^ v[(int) i];
            for (J = 0; J <= 7; J++) {
                long d0 = 0;
                d0 = CRC & 1L;
                CRC = CRC / 2;
                if (d0 == 1)
                    CRC = CRC ^ 0xa001L;
            }
        }
        CRC = CRC % 65536;
        functionReturnValue = CRC;
        return functionReturnValue;
    }

    public static void main(String[] args) {
        CRC16 d = new CRC16();
        String ds = d.coCRC16("03 03 00 00 00 01");
        System.out.println(ds);
    }

    public String coCRC16(String Str) {
        long lon = GetModBusCRC(Str.replace(" ", ""));
        int h1, l0;
        l0 = (int) lon / 256;
        h1 = (int) lon % 256;
        String s = "";
        if (Integer.toHexString(h1).length() < 2) {
            s = "0" + Integer.toHexString(h1);
        } else {
            s = Integer.toHexString(h1);
        }
        if (Integer.toHexString(l0).length() < 2) {
            s = s + "0" + Integer.toHexString(l0);
        } else {
            s = s + Integer.toHexString(l0);
        }
        return s;
    }
}

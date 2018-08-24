package com.apng;

/**
 * Apng Const
 *
 * @author ltf
 * @since 16/11/26, 下午3:26
 */
public class ApngConst {
    // signature
    public static final int PNG_SIG = -1991225785;
    public static final int PNG_SIG_VER = 218765834;

    // type code
    public static final int CODE_IHDR = 1229472850;

    public static final int CODE_iCCP = 1347179589;
    public static final int CODE_sRGB = 1934772034;
    public static final int CODE_sBIT = 1933723988;
    public static final int CODE_gAMA = 1732332865;
    public static final int CODE_cHRM = 1665684045;

    public static final int CODE_PLTE = 1347179589;

    public static final int CODE_tRNS = 1951551059;
    public static final int CODE_hIST = 1749635924;
    public static final int CODE_bKGD = 1649100612;
    public static final int CODE_pHYs = 1883789683;
    public static final int CODE_sPLT = 1934642260;

    public static final int CODE_acTL = 1633899596;
    public static final int CODE_fcTL = 1717785676;
    public static final int CODE_IDAT = 1229209940;
    public static final int CODE_fdAT = 1717846356;
    public static final int CODE_IEND = 1229278788;

    // ".ang" format [ self extended apng format, optimized for speed, quality and size ]
    //public static final int CODE_fcRC = 1717785155;
}

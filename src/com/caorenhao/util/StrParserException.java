package com.caorenhao.util;

/**
 * 字符串解析异常.
 *
 * @author renhao.cao.
 *         Created 2015-1-21.
 */
public class StrParserException extends Exception {

    private static final long serialVersionUID = -506554400205542215L;

    /**
     * 字符串解析异常内容.
     *
     * @param messgae
     */
    public StrParserException(String messgae) {
        super(messgae);
    }
}

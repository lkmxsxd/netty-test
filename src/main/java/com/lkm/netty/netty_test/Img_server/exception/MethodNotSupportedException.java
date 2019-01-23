package com.lkm.netty.netty_test.Img_server.exception;

/**
 * ^^^^^^^^^^^^^^^^^^^^|^^^^^^^^^^^^^^^^^^^^^
 * ^^^^^^^^^^^^^^^|    |    | ^^^^^^^^^^^^^^^人以群分
 * ^^^^^^^^^^^^^)_)  )_)  )_)^^^^^^^^^^^^^和为组织
 * ^^^^^^^^^^^^)___))___))___)/^^^^^^^^^聚于思想
 * ^^^^^^^^^^^)____)____)_____)//^^^^^获于感悟
 * ^^^^^^^^^_____|____|____|____///__
 * ---------/                   /---------
 * ^^^^^ ^^^^^ ^^^^  ^^^^^^^  ^^^^^    ^^^
 * 独学而无友 则孤陋而寡闻
 *
 * @Auto lkm
 * @Date 2019/1/15 16:34
 * @Tip 自定义异常类
 * @Version 1.0
 **/
public class MethodNotSupportedException extends RuntimeException {
    public MethodNotSupportedException() {
    }

    public MethodNotSupportedException(String message) {
        super(message);
    }

    public MethodNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotSupportedException(Throwable cause) {
        super(cause);
    }

    public MethodNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

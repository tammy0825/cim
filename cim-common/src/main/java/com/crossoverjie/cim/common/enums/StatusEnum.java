package com.crossoverjie.cim.common.enums;

import java.util.ArrayList;
import java.util.List;

public enum StatusEnum {

    /**
     * 成功
     */
    SUCCESS("0000", "成功"),

    /**
     * 成功
     */
    FALLBACK("2000", "FALL_BACK"),

    /**
     * 参数校验失败
     **/
    VALIDATION_FAIL("3000", "参数校验失败"),

    /**
     * 失败
     */
    FAIL("4000", "失败"),

    /**
     * 重复登录
     */
    REPEAT_LOGIN("5000", "账号重复登录，请退出一个账号！"),

    /**
     * 用户尚未注册
     */
    NOT_REGISTER("5001", "用户尚未注册"),

    /**
     * 注册失败
     */
    REGISTER_FAIL("6000", "注册失败"),

    /**
     * 用户名重复
     */
    REPEAT_USERNAME("6001", "用户名重复，请重新注册"),

    /**
     * 请求限流
     */
    REQUEST_LIMIT("7000", "请求限流"),

    /**
     * 账号不在线
     */
    OFF_LINE("8000", "你选择的账号不在线，请重新选择！"),

    ;


    /**
     * 枚举值码
     */
    private final String code;

    /**
     * 枚举描述
     */
    private final String message;

    /**
     * 构建一个 StatusEnum 。
     *
     * @param code    枚举值码。
     * @param message 枚举描述。
     */
    private StatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过枚举值码查找枚举值。
     *
     * @param code 查找枚举值的枚举值码。
     * @return 枚举值码对应的枚举值。
     * @throws IllegalArgumentException 如果 code 没有对应的 StatusEnum 。
     */
    public static StatusEnum findStatus(String code) {
        for (StatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("ResultInfo StatusEnum not legal:" + code);
    }

    /**
     * 获取全部枚举值。
     *
     * @return 全部枚举值。
     */
    public static List<StatusEnum> getAllStatus() {
        List<StatusEnum> list = new ArrayList<StatusEnum>();
        for (StatusEnum status : values()) {
            list.add(status);
        }
        return list;
    }

    /**
     * 获取全部枚举值码。
     *
     * @return 全部枚举值码。
     */
    public static List<String> getAllStatusCode() {
        List<String> list = new ArrayList<String>();
        for (StatusEnum status : values()) {
            list.add(status.code());
        }
        return list;
    }

    /**
     * 得到枚举值码。
     *
     * @return 枚举值码。
     */
    public String getCode() {
        return code;
    }

    /**
     * 得到枚举描述。
     *
     * @return 枚举描述。
     */
    public String getMessage() {
        return message;
    }

    /**
     * 得到枚举值码。
     *
     * @return 枚举值码。
     */
    public String code() {
        return code;
    }

    /**
     * 得到枚举描述。
     *
     * @return 枚举描述。
     */
    public String message() {
        return message;
    }
}

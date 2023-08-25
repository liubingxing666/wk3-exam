package com.ksyun.start.camp;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 代表此 API 的返回对象
 */
@Data
@Component
@NoArgsConstructor
public class ApiResponse {

    /**
     * 代表此 API 的响应返回码
     * 200 表示成功，非 200 表示失败
     */
    private int code;

    // 提示：调整此处 data 类型
    private Object data;
}

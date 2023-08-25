package com.ksyun.start.camp;

import com.ksyun.start.camp.service.ClientService;
import com.ksyun.start.camp.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 默认的客户端 API Controller
 */
@Component
@RestController
public class ClientController {
    @Autowired
    ApiResponse apiResponse;
    @Autowired
    ClientService clientService;
    // 在这里开始编写你的相关接口实现代码
    // 返回值对象使用 ApiResponse 类
    // 提示：调用 ClientService
    @GetMapping("/api/getInfo")
    public ApiResponse getInfo(){
        ApiResponse info= clientService.getInfo("full");
        return info;
    }

}

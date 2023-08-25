package com.ksyun.start.camp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.beans.IntrospectionException;
import java.util.*;

@Slf4j
@Component
@RestController
@RequestMapping("/api")
public class ServiceRegistryController {

    Integer rig=0;
    // 存储服务信息的注册表
    private Map<String, List<ServiceInformation>> serviceRegistry = new HashMap<>();
    private static final int HEARTBEAT_INTERVAL = 60;
    private Integer currentIndex=0;

    // 服务注册接口
    @PostMapping("/register")
    public  Map<String,Object> registerService(@RequestBody ServiceInformation serviceInformation) {
        Map<String,Object>resultMap=new LinkedHashMap<>();
        String serviceName = serviceInformation.getServiceName();
        String serviceId = serviceInformation.getServiceId();
        String ipAddress = serviceInformation.getIpAddress();
        Integer port = serviceInformation.getPort();
        // 检查是否已经注册了具有相同serviceId的服务
        List<ServiceInformation> instances = serviceRegistry.get(serviceName);
        if (instances != null) {
            for (ServiceInformation instance : instances) {
                if (instance.getServiceId().equals(serviceId)
                        && instance.getIpAddress().equals(ipAddress)
                        && instance.getPort().equals(port)) {
                    log.info("已经注册了相同的服务实例");
                    resultMap.put("code",500);
                    resultMap.put("data","已经注册了相同的服务实例");
                    return resultMap;
                }
            }
        }
        // 如果服务没有重复注册，则继续进行注册
        serviceRegistry.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(serviceInformation);
        System.out.println(serviceRegistry);
        log.info("注册服务成功");
        resultMap.put("code",200);
        resultMap.put("data","注册服务成功");
        return resultMap;
    }

    // 服务注销接口
    @PostMapping("/unregister")
    public  Map<String,Object> unregisterService(@RequestBody ServiceInformation serviceInformation) {
        Map<String,Object>resultMap=new LinkedHashMap<>();
        String serviceName = serviceInformation.getServiceName();
        List<ServiceInformation> instances = serviceRegistry.get(serviceName);
        if (instances != null) {
            for(ServiceInformation info:instances){
                if(info.getServiceId().equals(serviceInformation.getServiceId()))
                    rig++;
            }
            if(rig==0){
                resultMap.put("code",500);
                resultMap.put("data","该实例不存在，实例注销失败");
                return resultMap;
            }
            else
            {
                if(instances.removeIf(instance -> instance.getServiceId().equals(serviceInformation.getServiceId())
                                                &&instance.getIpAddress().equals(serviceInformation.getIpAddress())
                                                &&instance.getPort().equals(serviceInformation.getPort())
                )){
                    System.out.println(serviceRegistry);
                    resultMap.put("code",200);
                    resultMap.put("data","实例注销成功");
                    rig=0;
                }else{
                    resultMap.put("code",500);
                    resultMap.put("data","该实例id存在，但其他信息输入错误，实例注销失败");
                }
            }


        }else{
            resultMap.put("code",500);
            resultMap.put("data","该服务不存在，实例注销失败");
        }
        return resultMap;
    }

    // 客户端发送心跳接口
    @PostMapping("/heartbeat")
    public Map<String,Object> heartbeat(@RequestBody ServiceInformation serviceInformation) {
        if(!serviceRegistry.containsValue(serviceInformation.getServiceId()))
        {
            registerService(serviceInformation);
        }
       // System.out.println("heartInformation:"+serviceInformation);
        Map<String,Object>resultMap=new LinkedHashMap<>();
        String serviceName = serviceInformation.getServiceName();
        List<ServiceInformation> instances = serviceRegistry.get(serviceName);
        if (instances != null) {
            resultMap.put("code",200);
            resultMap.put("data","心跳发送成功");
            for (ServiceInformation instance : instances) {
                if (instance.getServiceId().equals(serviceInformation.getServiceId())) {
                    // 更新心跳时间
                    instance.setLastHeartbeatTime(System.currentTimeMillis());
                    System.out.println("HeartbeatTime"+instance.getLastHeartbeatTime());
                    log.info("Service heartbeat: {}", instance);
                    break;
                }
            }
        }else{
            resultMap.put("code",500);
            resultMap.put("data","心跳发送失败，该服务不存在");
        }
        return resultMap;
    }


    //检查服务缓存时间，超过60s就删除
    @Scheduled(fixedRate = 30000) // 每30秒执行一次定时任务
    public void checkHeartbeat() {
        System.out.println("serviceRegistry:"+serviceRegistry);
        cleanupExpiredServices();

    }

    @GetMapping("/discovery")
    public Map<String,Object> discoverService(@RequestParam(required = false) String name) {
        Map<String,Object>resultMap=new LinkedHashMap<>();

        if (name != null) {

            if(!serviceRegistry.containsKey(name)){
                resultMap.put("code","500");
                resultMap.put("data","服务不存在");
                return resultMap;
            }


            List<ServiceInformation> instances = new ArrayList<>();
            instances=serviceRegistry.get(name);
            List<Map<String,String>>resultInstance=new ArrayList<>();
//            if(instances.isEmpty()){
//                resultMap.put("code","500");
//                resultMap.put("data","服务不存在");
//                return resultMap;
//            }
            for(ServiceInformation info:instances)
            {
                Map<String,String>resultServiceMap=new LinkedHashMap<>();
                resultServiceMap.put("serviceId",info.getServiceId());
                resultServiceMap.put("serviceName",info.getServiceName());
                resultServiceMap.put("ipAddress",info.getIpAddress());
                resultServiceMap.put("port", info.getPort().toString());
                resultInstance.add(resultServiceMap);
            }

            if (resultInstance != null && !resultInstance.isEmpty()) {
                resultMap.put("code","200");
               // resultMap.put("data",resultInstance);
                // 负载均衡逻辑，使用简单的标准轮询
                Integer lunxunindex =currentIndex%resultInstance.size();
                List<Map<String, String>> selectedInstance = new ArrayList<>();
                selectedInstance.add(resultInstance.get(lunxunindex));
                resultMap.put("data", selectedInstance);
                currentIndex++;
                return resultMap;
            }
        } else {
            // 返回所有可用服务列表
            List<ServiceInformation> allInstances = new ArrayList<>();
            serviceRegistry.values().forEach(allInstances::addAll);
            List<Map<String,String>>allResultInstance=new ArrayList<>();
            for(ServiceInformation info:allInstances)
            {
                Map<String,String>resultServiceMap=new LinkedHashMap<>();
                resultServiceMap.put("serviceId",info.getServiceId());
                resultServiceMap.put("serviceName",info.getServiceName());
                resultServiceMap.put("ipAddress",info.getIpAddress());
                resultServiceMap.put("port", info.getPort().toString());
                allResultInstance.add(resultServiceMap);
            }
            resultMap.put("code","200");
            resultMap.put("data",allResultInstance);
            return resultMap;
        }
        return new LinkedHashMap<>();
    }



    public void cleanupExpiredServices() {
        long currentTime = System.currentTimeMillis();
        List<String> servicesToRemove = new ArrayList<>();
        for (Map.Entry<String, List<ServiceInformation>> entry : serviceRegistry.entrySet()) {
            String serviceName = entry.getKey();
            List<ServiceInformation> instances = entry.getValue();
            Iterator<ServiceInformation> iterator = instances.iterator();
            while (iterator.hasNext()) {
                ServiceInformation ss = iterator.next();
                long timeDiff = currentTime - ss.getLastHeartbeatTime();
                System.out.println("timediff: " + timeDiff);
                if (timeDiff > HEARTBEAT_INTERVAL * 1000) {
                    iterator.remove();
                }
            }
            // 如果服务列表为空，表示服务已经超时，将服务从注册表中移除
            if (instances.isEmpty()) {
                servicesToRemove.add(serviceName);
            }
        }
        // 移除超时的服务
        for (String serviceName : servicesToRemove) {
            serviceRegistry.remove(serviceName);
            log.info("Service '{}' has timed out and is removed from the registry.", serviceName);
        }
    }


    @GetMapping("/discovery/lunxun")
    public Map<String,Object> discoverServicerunxun(@RequestParam(required = false) String name) {
        Map<String,Object>resultMap=new LinkedHashMap<>();
        if (name != null) {
            List<ServiceInformation> instances = new ArrayList<>();
            instances=serviceRegistry.get(name);
            List<Map<String,String>>resultInstance=new ArrayList<>();
            for(ServiceInformation info:instances)
            {
                Map<String,String>resultServiceMap=new LinkedHashMap<>();
                resultServiceMap.put("serviceId",info.getServiceId());
                resultServiceMap.put("serviceName",info.getServiceName());
                resultServiceMap.put("ipAddress",info.getIpAddress());
                resultServiceMap.put("port", info.getPort().toString());
                resultInstance.add(resultServiceMap);
            }

            if (resultInstance != null && !resultInstance.isEmpty()) {
                resultMap.put("code","200");
                resultMap.put("data",resultInstance);
                return resultMap;
            }
        } else {
            // 返回所有可用服务列表
            List<ServiceInformation> allInstances = new ArrayList<>();
            serviceRegistry.values().forEach(allInstances::addAll);
            List<Map<String,String>>allResultInstance=new ArrayList<>();
            for(ServiceInformation info:allInstances)
            {
                Map<String,String>resultServiceMap=new LinkedHashMap<>();
                resultServiceMap.put("serviceId",info.getServiceId());
                resultServiceMap.put("serviceName",info.getServiceName());
                resultServiceMap.put("ipAddress",info.getIpAddress());
                resultServiceMap.put("port", info.getPort().toString());
                allResultInstance.add(resultServiceMap);
            }
            resultMap.put("code","200");
            resultMap.put("data",allResultInstance);
            return resultMap;
        }
        return new LinkedHashMap<>();
    }
}



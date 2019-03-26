package com.gupaoedu;

import com.gupaoedu.annotation.GPAutowired;
import com.gupaoedu.annotation.GPController;
import com.gupaoedu.annotation.GPRequestMapping;
import com.gupaoedu.annotation.GPService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class DispatcherServlet_xujj extends HttpServlet {
    //配置容器
    private  Properties contextConfig = new Properties();
    //类容器
    private List<String> classNames = new ArrayList<String>();
    //IOC容器
    private Map<String, Object> IOC = new HashMap<String, Object>();

    private Map<String, Method> handerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contextpath = req.getContextPath();
        String url = req.getRequestURI();
        url = url.replace(contextpath,"");
        Method method = handerMapping.get(url);
        String beanName = toLowerFirst(method.getDeclaringClass().getSimpleName());

        try {
            method.invoke(IOC.get(beanName),new Object[]{req,resp});
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
//        super.init(config);
        //加载配置
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //扫描类
        doScanPackage(contextConfig.getProperty("scanPackage"));
        //初始化扫描到的类，并放入IOC容器中
        try {
            doInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //注入
        try {
            doAutowired();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        //requestMapping  初始化 url 和 methed 对应关系

        doRequestMapping();
    }

    private void doRequestMapping() {
        if(IOC.isEmpty()){return;}
        for(String beanName : IOC.keySet()){
          Class clazz = IOC.get(beanName).getClass();
          String baseUrl ="";
          if(clazz.isAnnotationPresent(GPRequestMapping.class)){
              GPRequestMapping controllerMapping = (GPRequestMapping) clazz.getAnnotation(GPRequestMapping.class);
              baseUrl = controllerMapping.value();

              Method [] methods = clazz.getMethods();
                for(Method method : methods){
                    if(method.isAnnotationPresent(GPRequestMapping.class)){
                        GPRequestMapping methodMapping = method.getAnnotation(GPRequestMapping.class);
                        String nextUrl = methodMapping.value();
                        String url =( "/"+baseUrl+"/"+nextUrl).replaceAll("/+","/");
                        handerMapping.put(url, method);
                    }
                }
          }
        }
        System.out.println("handerMapping is done");
    }

    private void doAutowired() throws IllegalAccessException {
        if(IOC.isEmpty()){return;}
        for(String beanName : IOC.keySet()){
            Field[] fields = IOC.get(beanName).getClass().getDeclaredFields();
            for(Field field : fields){
                if(field.isAnnotationPresent(GPAutowired.class)){
                    GPAutowired gpAutowired = (GPAutowired) field.getAnnotation(GPAutowired.class);
                    String aliaseName = gpAutowired.value();
                    if("".equals(aliaseName)){
                        aliaseName=  toLowerFirst(field.getType().getSimpleName());
                    }
                    field.setAccessible(true);
                    field.set(IOC.get(beanName),IOC.get(aliaseName));
                }
            }

        }
        System.out.println("Autowired is done");
    }

    private void doInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(classNames.isEmpty()){return;}
        for(String className : classNames){
            Class clazz = Class.forName(className);
            if(clazz.isAnnotationPresent(GPController.class)){
                String beanName = toLowerFirst(clazz.getSimpleName());
                IOC.put(beanName, clazz.newInstance());
            }else if(clazz.isAnnotationPresent(GPService.class)){
                GPService gpService = (GPService) clazz.getAnnotation(GPService.class);
                String beanName = gpService.value();
                if("".equals(beanName.trim())){
                    beanName = toLowerFirst(clazz.getSimpleName());
                }
                IOC.put(beanName, clazz.newInstance());
            }
        }
        System.out.println("Instance is done");
    }

    private void doScanPackage(String scanPackage) {
        System.out.println("scanPackage="+scanPackage);
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.","/"));
        System.out.println(url);
        File classPath = new File(url.getFile());
        for(File file : classPath.listFiles()){
            if(file.isDirectory()){//如果是文件，递归
                doScanPackage(scanPackage+"."+file.getName());
            }else if(file.getName().endsWith(".class")){
                String name = scanPackage+"."+file.getName().replace(".class","");
                classNames.add(name);
            }
        }
        System.out.println("scanPackage is done");
    }

    private void doLoadConfig(String configName) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configName);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirst(String str){
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}

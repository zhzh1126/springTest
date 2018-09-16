package com.zz.mvcframework.servlet;
import com.zz.mvcframework.annotation.*;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Created by zz on 2018/9/1.
 */
public class DispatcherServlet extends HttpServlet {
    Properties contextConfig  = new Properties();
    //存放扫描并且通过反射实例化的对象
    HashMap<String,Object> iocObjectMap = new HashMap<>();
    ArrayList<String> classNameList = new ArrayList<>();
    ArrayList<Handler> handlerList = new ArrayList<>();
    @Override
    public void init(ServletConfig config) throws ServletException {
        //读取配置
        doLoadConfig(config);
        //通过解析配置文件中的内容，扫描出所有的类
        doScan(contextConfig.getProperty("scanPackage"));
        //实例化
        doInstance();
        //注入属性对象
        doAutowried();
        //生成handlerMapping
        doCreateHandlerMapping();
    }

    private void doCreateHandlerMapping() {
        for(Map.Entry<String,Object> entry : iocObjectMap.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(clazz.isAnnotationPresent(ZZController.class)){
                if(clazz.isAnnotationPresent(ZZRequestMapping.class)){
                    ZZRequestMapping zzRequestMapping = clazz.getAnnotation(ZZRequestMapping.class);
                    String controlPath = zzRequestMapping.value();
                    Method[] methods = clazz.getMethods();
                    //handlerList赋值
                    for (Method method :methods) {
                        if(method.isAnnotationPresent(ZZRequestMapping.class)){
                            String methodPath = method.getAnnotation(ZZRequestMapping.class).value();
                            String regex  = ("/"+controlPath+methodPath).replaceAll("/+","/");
                            Pattern pattern = Pattern.compile(regex);
                            handlerList.add(new Handler(pattern,method,entry.getValue()));
                        }
                    }
                }
            }

        }
    }

    private void doAutowried() {
        for(Map.Entry<String,Object> entry : iocObjectMap.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields) {
                if(field.isAnnotationPresent(ZZAutowired.class)){
                    String className = field.getAnnotation(ZZAutowired.class).value() ;
                    if("".equals(className)){
                        className = field.getType().getName() ;
                    }
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(),iocObjectMap.get(className));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void doInstance() {
        if(classNameList.isEmpty()){
            return;
        }
        for (String className:classNameList ) {
            try {
                Class<?> clazz = Class.forName(className);
                //只初始化有ZZController 和 ZZService 注解的类
                if(clazz.isAnnotationPresent(ZZController.class)||clazz.isAnnotationPresent(ZZService.class)){
                    iocObjectMap.put(lowerFirstCase(clazz.getSimpleName()),clazz.newInstance());

                }else if (clazz.isAnnotationPresent(ZZService.class)){
                    ZZService service = clazz.getAnnotation(ZZService.class);
                    Object object = clazz.newInstance();
                    String serviceName = service.value();
                    if("".equals(serviceName)){
                        //问题：这里为什么要用simpleName,为什么不用包全名？这样如果类名相同的话，会导致只有一个类对象
                        serviceName = lowerFirstCase(clazz.getSimpleName());
                    }
                    iocObjectMap.put(lowerFirstCase(serviceName),object);
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> inter:interfaces) {
                       iocObjectMap.put(inter.getName(),object);
                    }
                }else{
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doLoadConfig(ServletConfig config) {
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void doScan(String packagePath) {
       // packagePath = packagePath.replaceAll("\\.",File.separator);
        String temp =  packagePath.replaceAll("\\.","\\"+File.separator);
        URL url =  this.getClass().getClassLoader().getResource(temp);
        //test dubbger测试一下这里url.getFile()的返回值最后有没有“/”
        String  urlString = url.getFile();
       // File classDir = new File("D:/IDE/Workspaces/IDEA/springTest/target/classes/com%5czz%5cdemo");
        File classDir = new File(urlString);
        System.out.println(classDir.list());
        File[] files = classDir.listFiles();
        for(File file:classDir.listFiles()){
            if(file.isDirectory()){
               doScan(packagePath+"."+file.getName());
            }else{
                classNameList.add(packagePath+"."+file.getName().replace(".class",""));
            }
        }
    }

    /**
     * 字符串首字母小写
     * @param string
     * @return
     */
    private String   lowerFirstCase(String string){
        char[] chars = string.toCharArray();
        chars[0]+=32;
        return  chars.toString();
    }

    private class Handler {
        private Pattern pattern;//保存方法对应的实例
        private Method method;//保存映射的方法
        private Object controller;
        private Map<String,Integer> paramIndexMapping ;//参数顺序

        public Handler(Pattern pattern, Method method, Object controller) {
            this.pattern = pattern;
            this.method = method;
            controller = controller;
            this.paramIndexMapping = new HashMap<>();
            putParamIndexMappingn(method);
        }

        protected void putParamIndexMappingn(Method method) {

            //获取被ZZRequestParam注解的参数的index，
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i = 0;i<pa.length;i++){
                for (Annotation annotation:pa[i]) {
                    if(annotation instanceof ZZRequestParam){
                        String paramName = ((ZZRequestParam) annotation).name();
                        if(!"".equals(paramName)){
                            paramIndexMapping.put(paramName,i);
                        }

                    }
                }
            }
            //获取request,reponse 的index
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0;i<parameterTypes.length;i++) {
                Class<?> clazz = parameterTypes[i];
                if(clazz == HttpServletRequest.class){
                    paramIndexMapping.put(clazz.getName(),i);
                }
                if(clazz == HttpServletResponse.class){
                    paramIndexMapping.put(clazz.getName(),i);
                }
            }

            //获取其他参数的index
            Parameter[] parameters = method.getParameters();
            for (int i = 0;i<parameters.length;i++){
                //排除已存在的index
                if(!paramIndexMapping.containsValue(i))
                    paramIndexMapping.put(parameters[i].getName(),i);
            }
        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            controller = controller;
        }

        public Map<String, Integer> getParamIndexMapping() {
            return paramIndexMapping;
        }

        public void setParamIndexMapping(Map<String, Integer> paramIndexMapping) {
            this.paramIndexMapping = paramIndexMapping;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Handler handler = getHandlerByReq(req);
        if(handler==null){
            resp.getWriter().write("no found:404");
        }else{
            doHandler(handler,req,resp);

        }

    }

    private void doHandler(Handler handler,HttpServletRequest request,HttpServletResponse response) {
        Class<?>[] parameterTypes = handler.method.getParameterTypes();
        //获得方法的参数类型列表
        Map<String, String[]> requestParameterMap = request.getParameterMap();
        //存放方法实参的数组
        Object[] realParam = new Object[parameterTypes.length];
       for (Map.Entry<String,String []>  map : requestParameterMap.entrySet()){
           String value = Arrays.toString(map.getValue()).replaceAll("\\[|\\]","").replaceAll(",\\s",",");
           //如果参数名存在method形参中则将其存放到实参数组中
           if(!handler.paramIndexMapping.containsKey(map.getKey())){
               continue;
           }
           int index = handler.paramIndexMapping.get(map.getKey());
           realParam[index] = convert(parameterTypes[index],value);
       }


           int  index  = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
           realParam[index] =  request;
           index  = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
           realParam[index] =  response;
        try {
            handler.method.invoke(handler.controller,realParam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private  Object convert(Class<?> type,String value){
        if(type==Integer.class){
            return Integer.valueOf(value);
        }
        return  value;
    }
    private Handler getHandlerByReq(HttpServletRequest req) {
        String url  = req.getServletPath();
        for (Handler handler: handlerList ) {
            Matcher matcher = handler.pattern.matcher(url);
            if(matcher.matches()){
                return handler;
            }else {
                continue;
            }
        }
        return  null;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }

}

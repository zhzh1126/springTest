import com.zz.mvcframework.servlet.DispatcherServlet;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by zz on 2018/9/15.
 */
public class Test {
    @org.junit.Test
    public void test(){
        System.out.println("abcd");
    }

    public  void test4() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        System.out.println(dispatcherServlet.getClass().getResource("/"));
        System.out.println(dispatcherServlet.getClass().getResource(""));
        System.out.println(dispatcherServlet.getClass().getClassLoader().getResource(""));
    }

    public  void test3() {
        String [] stringArr = {"ab","zz","xy"};
        String strings = Arrays.toString(stringArr);
        String strings2 = strings.replaceAll("\\[|\\]","").replaceAll(".\\s",",");
        System.out.println(strings);
        System.out.println(strings2);

    }
    @org.junit.Test
    public  void test2() {
        String a = "a.b.c.d";
        String b = a.replaceAll("\\.","\\"+File.separator);
        System.out.println(b);
    }

    public  void test1() {
        URL url  = DispatcherServlet.class.getClassLoader().getResource("com/zz/demo/controller");
        System.out.println(url.getPath());
        try {

            //      Class<?> clazz = Class.forName(path+"com"+File.separator+"zz"+File.separator+"demo"+File.separator+"controller"+File.separator+"DemoController");
            Class<?> clazz = Class.forName("com.zz.demo.service.DemoService");
            System.out.println(clazz.getName());
            Field[] fields = clazz.getDeclaredFields();
            System.out.println(fields.length);

            for (int i = 0; i < fields.length; i++) {
                // fields[i].get;
                System.out.println(fields[i].getType().getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

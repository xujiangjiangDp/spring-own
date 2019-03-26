package com.gupaoedu.test;

import java.lang.reflect.Field;
import java.net.URL;

public class ClassTest {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchFieldException {
        Class clazz = Class.forName("com.gupaoedu.test.Persion");

        Persion persion = (Persion) clazz.newInstance();
        Field[] fields_1 = clazz.getFields();
        Field field_1 =  clazz.getField("name");
        Field[] fields_2 = clazz.getDeclaredFields();
        try {
            Field field_2 =  clazz.getDeclaredField("name");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        URL url_1 = clazz.getResource("/com/gupaoedu");
        URL url_2 = clazz.getClassLoader().getResource("com/gupaoedu");
    }
}

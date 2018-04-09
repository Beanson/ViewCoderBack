package UploadFile;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/10/31.
 */
public class Test {

    public static void main(String args[]){

        Jedis con = new Jedis("127.0.0.1");
        Pipeline pipe=con.pipelined();
        con.auth("beanson");

        Map map=new HashMap<String,String>();
        map=con.hgetAll("StudentOperation");
        for (Object entry : map.entrySet())
        {
            String str=((Map.Entry<String,String>)entry).getKey();
            pipe.hdel("StudentOperation",str);
        }

        pipe.sync();
        pipe.bgsave();
        try {
            pipe.close();
            con.close();
        } catch (IOException e) {
            con.close();
            e.printStackTrace();
        }
    }


    @org.junit.Test
    public void dala(){
        Path path= Paths.get("F","XAMPP","htdocs","CourseSupport","ftp");
        System.out.println(path.toUri());
        System.out.println(path.toString());
    }

    public void hello(String mark){

    }
}



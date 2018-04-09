package FrontEnd.myBatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by Administrator on 2016/4/26.
 */
public class MybatisUtils {

    private volatile static SqlSessionFactory sessionFactory;

    public static SqlSessionFactory getFactory(){

        if (sessionFactory == null) {

            synchronized (MybatisUtils.class) {
                if (sessionFactory == null) {

                    try {
                        String resource = "myBatis/conf.xml";
                        //加载 mybatis 的配置文件（它也加载关联的映射文件）
                        Reader reader = null; //构建 sqlSession 的工厂
                        //getResourceAsReader默认在resources文件夹下查找资源
                        reader = Resources.getResourceAsReader(resource);
                        sessionFactory = new SqlSessionFactoryBuilder().build(reader); //创建能执行映射文件中 sql 的 sqlSession

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /*SqlSessionFactory sessionFactory=null;
        try {
            String resource = "myBatis/conf.xml";
            //加载 mybatis 的配置文件（它也加载关联的映射文件）
            Reader reader = null; //构建 sqlSession 的工厂
            //getResourceAsReader默认在resources文件夹下查找资源
            reader = Resources.getResourceAsReader(resource);
            sessionFactory = new SqlSessionFactoryBuilder().build(reader); //创建能执行映射文件中 sql 的 sqlSession

        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return sessionFactory;
    }

    public static SqlSession getSession(){

        return getFactory().openSession();
    }
}

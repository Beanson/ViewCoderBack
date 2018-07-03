package viewcoder.tool.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import viewcoder.tool.config.GlobalConfig;
import java.io.IOException;
import java.io.Reader;


/**
 * Created by Administrator on 2016/4/26.
 * mybatis初始化操作
 */
public class MybatisUtils {

    private volatile static SqlSessionFactory sessionFactory;
    private final static String MYBATIS_CONFIG = "mybatis/conf.xml";

    public static SqlSessionFactory getFactory() {
        /*单例模式*/
        if (sessionFactory == null) {
            synchronized (MybatisUtils.class) {
                if (sessionFactory == null) {
                    try {
                        //加载 mybatis 的配置文件（它也加载关联的映射文件）
                        Reader reader = Resources.getResourceAsReader(MYBATIS_CONFIG);
                        //根据在GlobalConfig中加载的配置文件信息，创建能执行映射文件中sql的sqlSession
                        sessionFactory = new SqlSessionFactoryBuilder().build(reader, GlobalConfig.getPropertiesRef());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sessionFactory;
    }

    /**
     * 获取执行sql操作的句柄
     * @return
     */
    public static SqlSession getSession() {
        return getFactory().openSession();
    }
}

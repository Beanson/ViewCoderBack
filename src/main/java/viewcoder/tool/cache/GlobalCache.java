package viewcoder.tool.cache;


import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import viewcoder.operation.entity.ProjectProgress;

/**
 * Created by Administrator on 2018/5/12.
 */
public class GlobalCache {

    private static CacheManager cacheManager;
    private static Cache<Integer, ProjectProgress> projectProgressCache;
    private static Cache<String, Integer> registerVerifyCache;

    static {
        //初始化cache manager信息
        Configuration xmlConf = new XmlConfiguration(CacheManager.class.getResource("/cache/ehcache.xml"));
        cacheManager = CacheManagerBuilder.newCacheManager(xmlConf);
        cacheManager.init();

        //初始化各个cache到对象
        projectProgressCache = cacheManager.getCache("projectProgress", Integer.class, ProjectProgress.class);
        registerVerifyCache = cacheManager.getCache("registerVerifyCache", String.class, Integer.class);
    }

    /**
     * 返回project progress的cache
     *
     * @return
     */
    public static Cache<Integer, ProjectProgress> getProjectProgressCache() {
        return projectProgressCache;
    }

    /**
     * 返回registerVerifyCache的cache信息
     * @return
     */
    public static Cache<String, Integer> getRegisterVerifyCache() {
        return registerVerifyCache;
    }

    //测试方法
//    public static void main(String[] args) {
//        simulateCache.put("good",123);
//        simulateCache.put("good",456);
//        simulateCache.put("good",789);
//        Integer value = simulateCache.get("good");
//        System.out.println(value);
//        simulateCache.clear();
//        cacheManager.removeCache("simulate");
//        cacheManager.close();

//        projectProgressCache.clear();
//        cacheManager.removeCache("projectMark");
//        cacheManager.close();
//    }
}

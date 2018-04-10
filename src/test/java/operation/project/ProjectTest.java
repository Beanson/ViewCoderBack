package operation.project;

import FrontEnd.helper.common.Common;
import FrontEnd.helper.config.GlobalConfig;
import FrontEnd.helper.test.TestResponseOpt;
import FrontEnd.myBatis.entity.response.ResponseData;
import FrontEnd.myBatis.operation.common.CommonService;
import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static FrontEnd.myBatis.operation.common.CommonService.junitReqRespVerify;

/**
 * Created by Administrator on 2018/2/19.
 */
public class ProjectTest {

    private static Logger logger = Logger.getLogger(ProjectTest.class);
    private boolean TEST_INTEGRATION = GlobalConfig.getBooleanProperties(Common.PROJECT_TEST_INTEGRATION);


    /**
     * 获取该用户所有project对象测试
     */
    @Test
    public void getProjectDataTest() {
        Map<String, Object> map = new HashMap<>();
        //成功返回user_id为1的所有用户的数据
        map.put("user_id", 1);
        junitReqRespVerify(map,"getProjectListData",200);
    }


    /**
     * 创建新项目测试，分别创建空项目和PSD项目
     */
    @Test
    public void createEmptyProject(){
        Map<String, Object> map = new HashMap<>();
        //创建空project测试
        String project_file_name= CommonService.getTimeStamp()+"-index.html";
        map.put("user_id", 1);
        map.put("project_name", "my new project");
        map.put("project_file_name", project_file_name);
        map.put("last_modify_time", CommonService.getDateTime());
        map.put("project_data", "hello world");
        map.put("resource_size", "0");
        junitReqRespVerify(map, "createEmptyProject", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                try{
                    Map<String,Object>map= JSON.parseObject(responseData.getData().toString(),HashMap.class);
                    int id= (Integer) map.get("id");
                    ProjectTest.logger.debug("===create psd project id: "+id+" and delete mark: "+ TEST_INTEGRATION);
                    if(TEST_INTEGRATION){
                        deleteProjectTest(id);
                    }
                }catch (Exception e){
                    ProjectTest.logger.error("===createEmptyProject error:",e);
                }

            }
        });
    }

    @Test
    public void createPSDProjectTest(){
        Map<String, Object> map = new HashMap<>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/simple.psd").getFile());
        String project_file_name=CommonService.getTimeStamp()+"-index.html";

        //创建PSD的project成功测试
        map.put("user_id", 1);
        map.put("project_name", "my new project");
        map.put("project_file_name", project_file_name);
        map.put("last_modify_time", CommonService.getDateTime());
        map.put("psd_file", file);
        junitReqRespVerify(map, "createPSDProject", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                try{
                    ProjectTest.logger.debug("===create psd project get response: "+responseData);
                    Map<String,Object>map= JSON.parseObject(responseData.getData().toString(),HashMap.class);
                    int id= (Integer) map.get("id");
                    ProjectTest.logger.debug("===create psd project id: "+id+" and delete mark: "+ TEST_INTEGRATION);
                    if(TEST_INTEGRATION){
                        deleteProjectTest(id);
                    }
                }catch (Exception e){
                    ProjectTest.logger.error("===createPSDProjectTest error:",e);
                }
            }
        });

        //创建PSD的project失败测试，由于用户可用空间不足导致
        map.put("user_id", 10);
        junitReqRespVerify(map,"createPSDProject",402);
    }


    /**
     * 删除项目测试
     */
    @Test
    public void deleteProjectTest(){
        //deleteProjectMark标志是否进行整体测试
        if(!TEST_INTEGRATION){
            deleteProjectTest(163);
        }
    }
    private void deleteProjectTest(int project_id){
        Map<String, Object> map = new HashMap<>();
        //用户不是该project所有者，不能删除
        map.put("user_id", 100);
        map.put("project_id", project_id);
        junitReqRespVerify(map,"deleteProject",401);

        //用户是该project所有者，可以删除
        map.put("user_id", 1);
        map.put("project_id", project_id);
        junitReqRespVerify(map,"deleteProject",200);
    }


    /**
     * 拷贝项目测试
     */
    @Test
    public void copyProjectTest(){
        copyProjectTest(151);
    }

    private void copyProjectTest(int project_id){
        Map<String, Object> map = new HashMap<>();
        //测试正常拷贝项目
        //手动设置和自动化设置
        map.put("project_id", project_id);
        map.put("user_id", 1);
        junitReqRespVerify(map, "copyProject", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                ProjectTest.logger.debug("===copy project get response: "+responseData);
                Map<String,Object>map= JSON.parseObject(responseData.getData().toString(),HashMap.class);
                int id= (Integer) map.get("id");
                ProjectTest.logger.debug("===copied project id: "+id+" and delete mark: "+ TEST_INTEGRATION);
                if(TEST_INTEGRATION){
                    deleteProjectTest(id);
                }
            }
        });

        //测试用户空间不足，无法进行拷贝项目
        map.put("user_id", 10);
        junitReqRespVerify(map,"copyProject",401);
    }


    /**
     * 更改项目名称
     */
    @Test
    public void modifyProjectNameTest(){
        Map<String, Object> map = new HashMap<>();
        map.put("id", 151);
        map.put("project_name", "my new project"+(int)(Math.random()*100));
        junitReqRespVerify(map,"modifyProjectName",200);
    }

}













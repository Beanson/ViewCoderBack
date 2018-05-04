package operation.render;

import viewcoder.tool.common.Common;
import viewcoder.tool.common.Mapper;
import viewcoder.tool.config.GlobalConfig;
import viewcoder.tool.test.TestResponseOpt;
import viewcoder.tool.util.MybatisUtils;
import viewcoder.operation.entity.Project;
import viewcoder.operation.entity.UserUploadFile;
import viewcoder.operation.entity.response.ResponseData;
import viewcoder.operation.impl.common.CommonService;
import com.alibaba.fastjson.JSON;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/24.
 */
public class RenderTest {

    private static Logger logger = Logger.getLogger(RenderTest.class);
    private boolean TEST_INTEGRATION = GlobalConfig.getBooleanProperties(Common.PROJECT_TEST_INTEGRATION);

    @Test
    public void getProjectRenderData() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 151);
        CommonService.junitReqRespVerify(map, "getProjectRenderData", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                RenderTest.logger.debug("===get response: " + responseData);
            }
        });
    }

    @Test
    public void getUploadedResourceTest() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_id", 1);
        map.put("file_type", 1);
        CommonService.junitReqRespVerify(map, "getUploadedResource", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                RenderTest.logger.debug("get response data:" + responseData);
                List<UserUploadFile> list = JSON.parseArray(responseData.getData().toString(), UserUploadFile.class);
                RenderTest.logger.debug("get list data:" + list);
            }
        });
    }



    @Test
    public void insertNewResource() {
        Map<String, Object> map = new HashMap<String, Object>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/ly.png").getFile());
        String fileInfo[] = file.getName().split("\\.");

        //可以正常insert，用户有足够的空间插入新resource
        map.put("user_id", 1);
        map.put("project_id", 151);
        map.put("widget_type", "Common_Image");
        map.put("file_type", 1);
        map.put("is_folder", 0);
        map.put("time_stamp", CommonService.getTimeStamp());
        map.put("suffix", fileInfo[1]);
        map.put("file_name", fileInfo[0]);
        map.put("relative_path", "");
        map.put("file_size", file.length());
        map.put("video_image_name", null);
        map.put("create_time", CommonService.getDateTime());
        map.put("file", file);
        CommonService.junitReqRespVerify(map, "uploadResource", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                Map<String, Object> map = JSON.parseObject(responseData.getData().toString(), HashMap.class);
                int id = (Integer) map.get("id");
                int user_resource_remain = (Integer) map.get("user_resource_remain");
                int project_resource_space = (Integer) map.get("project_resource_space");
                RenderTest.logger.debug("get new file id:" + id + "; user_resource_remain:" + user_resource_remain +
                        "; project_resource_space:" + project_resource_space);
                if (TEST_INTEGRATION) {
                    deleteResourceTest(id, 151, 1);
                }
            }
        });

        //用户无足够空间插入
        map.put("user_id", 10);
        CommonService.junitReqRespVerify(map, "uploadResource", 403);

        //用户插入folder
        map.put("is_folder", 1);
        map.put("time_stamp", "");
        map.put("suffix", "");
        map.put("file_size", null);
        map.put("file_name", "新建文件夹");
        map.remove("file");
        CommonService.junitReqRespVerify(map, "uploadResource", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                Map<String, Object> map = JSON.parseObject(responseData.getData().toString(), HashMap.class);
                int id = (Integer) map.get("id");
                RenderTest.logger.debug("get folder id:" + id);
                if (TEST_INTEGRATION) {
                    deleteResourceTest(id, 151, 1);
                }
            }
        });
    }



    @Test
    public void uploadVideoImageTest() {
        if (!TEST_INTEGRATION) {
            uploadVideoImage(304);
        }
    }
    private void uploadVideoImage(int video_id){
        Map<String, Object> map = new HashMap<String, Object>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/ly.png").getFile());
        map.put("video_id", video_id);
        map.put("video_image", file);
        map.put("video_image_name", CommonService.getTimeStamp() + Common.IMG_PNG);
        CommonService.junitReqRespVerify(map, "uploadVideoImage", 200);
    }



    @Test
    public void deleteFolderResource(){
        Map<String, Object> map = new HashMap<String, Object>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("files/ly.png").getFile());
        String fileInfo[] = file.getName().split("\\.");

        //插入两个file，并且在下面新添加的的folder里面
        map.put("user_id", 1);
        map.put("project_id", 151);
        map.put("widget_type", "Video");
        map.put("file_type", 2);
        map.put("is_folder", 0);
        map.put("time_stamp", CommonService.getTimeStamp());
        map.put("suffix", fileInfo[1]);
        map.put("file_name", fileInfo[0]);
        map.put("relative_path", "new_folder/");
        map.put("file_size", file.length());
        map.put("video_image_name", null);
        map.put("create_time", CommonService.getDateTime());
        map.put("file", file);
        CommonService.junitReqRespVerify(map, "uploadResource",200);
        //插入新file，并且该file有video_image
        CommonService.junitReqRespVerify(map, "uploadResource",200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                Map<String, Object> map = JSON.parseObject(responseData.getData().toString(), HashMap.class);
                int id = (Integer) map.get("id");
                //上传更新该Video类型组件的video_image_name
                uploadVideoImage(id);
            }
        });

        //插入folder
        map.put("is_folder", 1);
        map.put("time_stamp", "");
        map.put("suffix", "");
        map.put("file_name", "new_folder");
        map.put("relative_path", "");
        map.remove("file");
        map.remove("widget_type");
        map.remove("file_size");
        CommonService.junitReqRespVerify(map, "uploadResource", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                //删除该folder，注意把两个文件都一并删除
                Map<String, Object> map = JSON.parseObject(responseData.getData().toString(), HashMap.class);
                int id = (Integer) map.get("id");
                if (TEST_INTEGRATION) {
                    deleteResourceTest(id, 151, 1);
                }
            }
        });
    }

//    @Test
//    public void getFolderSubResource(){
//        SqlSession sqlSession = MybatisUtils.getSession();
//        UserUploadFile userUploadFile=new UserUploadFile(151,1,2,"new_folder/");
//        List<UserUploadFile> list = sqlSession.selectList(Mapper.GET_FOLDER_SUB_RESOURCE, userUploadFile);
//        RenderTest.logger.debug("===Get resource list: \n"+list);
//    }


    @Test
    public void deleteResourceTest() {
        if (!TEST_INTEGRATION) {
            deleteResourceTest(327, 151, 1);
        }
    }
    private void deleteResourceTest(int id, int project_id, int user_id) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("project_id", project_id);
        map.put("user_id", user_id);
        CommonService.junitReqRespVerify(map, "deleteResource", 200, new TestResponseOpt() {
            @Override
            public void doResponseOpt(ResponseData responseData) {
                Map<String, Object> map = JSON.parseObject(responseData.getData().toString(), HashMap.class);
                int user_resource_remain = (Integer) map.get("user_resource_remain");
                int project_resource_space = (Integer) map.get("project_resource_space");
                RenderTest.logger.debug("User: " + user_id + ", resource_remain:" + user_resource_remain +
                        ", project_resource_space:" + project_resource_space);
            }
        });
    }


    @Test
    public void renameResource() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 304); //该resource_id对应的project_id为151
        map.put("new_file_name", "newName-" + CommonService.getTimeStamp());
        CommonService.junitReqRespVerify(map, "renameResource", 200);
    }


    @Test
    public void saveProjectDataTest() {
        SqlSession sqlSession = MybatisUtils.getSession();
        Project project = sqlSession.selectOne(Mapper.GET_PROJECT_DATA, 151);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", 151); //project_id
        map.put("last_modify_time", CommonService.getDateTime());
        map.put("project_data", project.getProject_data());
        map.put("project_file_name", project.getTimestamp());
        CommonService.junitReqRespVerify(map, "saveProjectData", 200);
    }


}

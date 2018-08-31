package viewcoder.tool.common;

/**
 * Created by Administrator on 2018/2/24.
 */
public class Mapper {

    //project表的数据库操作mapper*******************************************************************
    //select
    public static final String GET_PROJECT_LIST_DATA = "getProjectListData";
    public static final String GET_PROJECT_DATA = "getProjectData";
    public static final String GET_PROJECT_CHILDREN_LIST = "getProjectChildrenList";
    public static final String GET_PROJECT_VERSION_DATA = "getProjectVersionData";
    public static final String GET_ALL_RELATED_PROJECT = "getAllRelatedProject";
    public static final String GET_PROJECT_RESOURCE_SIZE = "getProjectResourceSize";
    public static final String GET_TARGET_STORE_DATA = "getTargetStoreData";
    public static final String GET_PROJECT_BY_TIMESTAMP = "getProjectDataByTimestamp";
    public static final String GET_PROJECT_NAME = "getProjectName";

    //insert
    public static final String CREATE_EMPTY_PROJECT = "createEmptyProject";
    public static final String CREATE_PSD_PROJECT = "createPSDProject";
    public static final String CREATE_COPY_PROJECT = "createCopyProject";
    public static final String CREATE_SIMULATE_PROJECT = "createSimulateProject";

    //delete
    public static final String DELETE_PROJECT_BY_ID = "deleteProjectById";
    public static final String DELETE_PROJECT_BY_FILE_NAME = "deleteProjectByFileName";

    //update
    public static final String SAVE_PROJECT_DATA = "saveProjectData";
    public static final String MODIFY_PROJECT_NAME = "modifyProjectName";
    public static final String UPDATE_PROJECT_RESOURCE_SIZE = "updateProjectResourceSize";
    public static final String UPDATE_PROJECT_OPENNESS = "updateProjectOpenness";
    public static final String UPDATE_USAGE_AMOUNT_PLUS = "updateUsageAmountPlus";
    public static final String UPDATE_CHILD_NUM_PLUS = "updateChildNumPlus";
    public static final String UPDATE_CHILD_NUM_MINUS = "updateChildNumMinus";



    //user_upload_file表的数据库操作mapper**************************************************************
    //select
    public static final String GET_ALL_RESOURCE_BY_PROJECT_ID = "getAllResourceByProjectId";
    public static final String GET_RESOURCE_DATA = "getResourceData";
    public static final String GET_RESOURCE_NAME_DATA = "getResourceNameData";
    public static final String GET_RESOURCE_BY_USERID_AND_FILETYPE = "getResourceByUserIdAndFileType";
    public static final String GET_RESOURCE_REF_COUNT = "getResourceRefCount";
    public static final String GET_FOLDER_SUB_RESOURCE = "getFolderSubResource";
    public static final String GET_ROOT_FOLDER_COUNT = "getRootFolderCount";

    //delete
    public static final String DELETE_RESOURCE_BY_ID = "deleteResourceById";
    public static final String DELETE_RESOURCE_BY_PROJECT_ID = "deleteResourceByProjectId";

    //insert
    public static final String INSERT_NEW_RESOURCE = "insertNewResource";
    public static final String INSERT_BATCH_NEW_RESOURCE = "insertBatchNewResource";

    //update
    public static final String UPDATE_RESOURCE_TIMESTAMP = "updateResourceTimeStamp";
    public static final String RENAME_RESOURCE_BY_ID = "renameResourceById";
    public static final String UPDATE_VIDEO_IMAGE = "updateVideoImage";



    //user 表数据库操作mapper************************************************************************
    //select
    public static final String GET_USER_DATA = "getUserData";
    public static final String LOGON_VALIDATION = "loginValidation";
    public static final String SIGN_ACCOUNT_CHECK = "signAccountCheck";
    public static final String REGISTER_ACCOUNT_CHECK = "registerAccountCheck";
    public static final String GET_ORIGIN_PORTRAIT_NAME = "getOriginPortraitName";
    public static final String GET_USER_RESOURCE_SPACE_INFO = "getUserResourceSpaceInfo";
    public static final String GET_TOTAL_POINTS = "getTotalPoints";
    public static final String GET_USER_SPACE_INFO = "getUserSpaceInfo";
    public static final String GET_PHONE_ACCOUNT = "getPhoneAccount";
    public static final String GET_USER_BY_OPEN_ID = "getUserByOpenId";
    public static final String GET_USER_MAIL_PHONE_DATA = "getUserMailPhoneData";

    //insert
    public static final String REGISTER_NEW_ACCOUNT = "registerNewAccount";

    //delete
    public static final String DELETE_USER_INFO = "deleteUserInDb";

    //update
    public static final String UPDATE_USER_INFO = "updateUserInfo";
    public static final String UPDATE_USER_RESOURCE_SPACE_USED = "updateUserResourceSpaceUsed";
    public static final String REDUCE_USER_RESOURCE_SPACE_USED = "reduceUserResourceSpaceUsed";
    public static final String ADD_USER_RESOURCE_SPACE_USED = "addUserResourceSpaceTotal";
    public static final String UPDATE_USER_LAST_SELECTED_INDUSTRY = "updateLastSelectedIndustry";
    public static final String UPDATE_USER_TOTAL_POINTS = "updateUserTotalPoints";
    public static final String REMOVE_EXPIRE_ORDER_SPACE = "removeExpireOrderSpace";
    public static final String UPDATE_EXPORT_DEFAULT_SETTING = "updateExportDefaultSetting";
    public static final String UPDATE_WECHAT_INFO_TO_USER = "updateWeChatInfoToUser";
    public static final String UPDATE_USER_ACK = "updateUserAck";



    //order表数据库操作mapper************************************************************************
    //select
    public static final String GET_ORDER_LIST = "getOrderList";
    public static final String GET_TARGET_ORDER_LIST = "getTargetOrderList";
    public static final String GET_ORDER_NUM_BY_TRADE_NO = "getOrderNumByTradeNo";
    public static final String GET_ORDER_BY_TRADE_NO = "getOrderByTradeNo";
    public static final String GET_ORDER_INSTANCE_BY_USER_ID = "getOrderInstanceByUserId";
    public static final String GET_EXPIRED_ORDER_INSTANCE = "getExpiredOrderInstance";
    public static final String GET_TO_EXPIRE_ORDER_INSTANCE = "getToExpireOrderInstance";

    //insert
    public static final String INSERT_NEW_ORDER_ITEM = "insertNewOrderItem";
    public static final String NEW_REGISTER_TRY_ORDER = "newRegisterTryOrder";

    //update
    public static final String UPDATE_ORDER_PAYMENT="updateOrderPayment";
    public static final String UPDATE_ORDER_INSTANCE_EXPIRE_DAYS = "updateOrderInstanceExpireDays";

    //delete
    public static final String DELETE_ORDER_ITEM = "deleteOrderItem";



    //feedback数据库操作mapper************************************************************************
    //select

    //insert
    public static final String INSERT_NEW_FEEDBACK = "insertNewFeedback";

    //update

    //delete



    //company数据库操作mapper************************************************************************
    //select
    public static final String GET_COMPANY_DISCOUNT_ORDER = "getCompanyDiscountOrder";

    //insert

    //update

    //delete
}














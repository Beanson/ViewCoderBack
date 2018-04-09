阿里云计算开放服务软件开发工具包Java版
Aliyun OSS SDK for Java

版权所有 （C）阿里云计算有限公司

Copyright (C) Alibaba Cloud Computing
All rights reserved.

http://www.aliyun.com

环境要求：
- J2SE Development Kit (JDK) 6.0或以上版
- Apache Commons （Codec、HTTP Client和Logging）、JDOM等第三方软件包（已包含在SDK的下载压缩包中）。
- 必须注册有Aliyun.com用户账户，并开通OSS服务。

2.8.2更新日志：
- 修复：正常请求返回RequestId

2.8.1更新日志：
- 修复：deleteObjects在quiet模式时连接泄漏的问题

2.8.0更新日志：
- 添加：doesObjectExist支持参数isOnlyInOSS

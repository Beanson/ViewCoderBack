package UploadFile;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by Administrator on 2016/8/13.
 */
public class Global {
   public static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
}

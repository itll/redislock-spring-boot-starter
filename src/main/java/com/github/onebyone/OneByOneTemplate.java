package com.github.onebyone;

/**
 * 一个接一个业务处理模版<br>
 * 防止在请求并发下，业务重复处理<br>
 * 使用redis缓存实现
 *
 */
public interface OneByOneTemplate {

    /**
     * 执行<br>
     * 默认，排队，默认排队时间10秒，并发锁有效期30秒
     *
     * @param oneByOne 一个接一个处理记录
     * @param callBack 回调
     * @return 执行结果
     */
    <T> T execute(OneByOne oneByOne, CallBack<T> callBack);

    /**
     * 执行<br>
     * waitInQueue：true:排队. false:不排队<br>
     * 默认并发锁有效期30秒
     *
     * @param oneByOne    一个接一个处理记录
     * @param waitInQueue 是否需要排队,true:排队. false:不排队
     * @param callBack    回调
     * @return
     */
    <T> T execute(OneByOne oneByOne, boolean waitInQueue, CallBack<T> callBack);

    /**
     * 执行<br>
     * waitInQueue：true:排队. false:不排队<br>
     * timeoutMsecs： 排队时间,单位毫秒.
     * 当waitInQueue为true时，并且timeoutMsecs取值小于等于零时，则使用默认的排队10秒。当waitInQueue为false时,timeoutMsecs随便取值，不生效<br>
     * expireMsecs：并发锁有效期,单位毫秒.建议取值大于30秒. 当expireMsecs取值小于等于零时，则使用默认的有效期30秒
     *
     * @param oneByOne     一个接一个处理记录
     * @param waitInQueue  是否需要排队,true:排队. false:不排队
     * @param timeoutMsecs 排队时间,单位毫秒
     * @param expireMsecs  并发锁有效期,单位毫秒
     * @param callBack     回调
     * @return 执行结果
     */
    <T> T execute(OneByOne oneByOne, boolean waitInQueue, int timeoutMsecs, int expireMsecs, CallBack<T> callBack);

}

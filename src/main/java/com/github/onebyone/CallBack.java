
package com.github.onebyone;

/**
 * 回调接口
 *
 */
public interface CallBack<T> {

    /**
     * 调用
     *
     * @return 结果
     */
    T invoke();

}

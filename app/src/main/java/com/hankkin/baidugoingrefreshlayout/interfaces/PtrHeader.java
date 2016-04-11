package com.hankkin.baidugoingrefreshlayout.interfaces;

import com.hankkin.baidugoingrefreshlayout.widget.config.RefreshState;

/**
 * 刷新header的回调
 */
public interface PtrHeader {

    /**
     * 刷新完成后，header恢复初始状态
     * @param state
     */
    void onReset(RefreshState state);

    /**
     * 下拉刷新状态
     * @param state
     */
    void onPullToRefresh(RefreshState state);

    /**
     * 松开刷新状态
     * @param state
     */
    void onReleaseToRefresh(RefreshState state);

    /**
     * 正在刷新状态
     * @param state
     */
    void onRefreshing(RefreshState state);

    /**
     * 得到header高度
     * @return
     */
    int getContentHeight();

    RefreshState getState();



}

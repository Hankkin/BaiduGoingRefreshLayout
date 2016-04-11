package com.hankkin.baidugoingrefreshlayout.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;
import com.hankkin.baidugoingrefreshlayout.interfaces.PtrHeader;
import com.hankkin.baidugoingrefreshlayout.widget.config.RefreshState;

/**
 * Created by Hankkin on 16/4/10.
 */
public class BaiDuRefreshListView extends ListView {
    private static final String TAG = "BaiDuRefreshListView";
    private static final int RATIO = 3;
    private float startY;   //开始Y坐标
    private float offsetY;  //Y轴偏移量
    private OnBaiduRefreshListener mOnRefreshListener;  //刷新接口
    private boolean isRecord;   //是否记录
    private boolean isEnd;  //是否结束
    private boolean isRefreable;    //是否刷新

    private float touchSclop;

    private PtrHeader headerView;

    public BaiDuRefreshListView(Context context) {
        this(context, null);
    }

    public BaiDuRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaiDuRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewConfiguration conf = ViewConfiguration.get(context);
        touchSclop = conf.getScaledTouchSlop();
        init(context);
    }

    public interface OnBaiduRefreshListener {
        void onRefresh();
    }

    /**
     * 回调接口，想实现下拉刷新的listview实现此接口
     */
    public void setOnBaiduRefreshListener(OnBaiduRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
        isRefreable = true;
    }

    /**
     * 刷新完毕，从主线程发送过来，并且改变headerView的状态和文字动画信息
     */
    public void setOnRefreshComplete() {
        //一定要将isEnd设置为true，以便于下次的下拉刷新
        isEnd = true;
        changeState(RefreshState.DONE);
    }

    private void init(Context context) {
        //关闭view的OverScroll
        setOverScrollMode(OVER_SCROLL_NEVER);
        headerView = new BaiDuPtrHeader(context);
        //给ListView添加头布局
        addHeaderView((View) headerView);

        isEnd = true;
        isRefreable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isEnd) {//如果现在时结束的状态，即刷新完毕了，可以再次刷新了，在onRefreshComplete中设置
            if (isRefreable) {//如果现在是可刷新状态   在setOnMeiTuanListener中设置为true
                RefreshState refreshState = headerView.getState();
                int headViewHeight = headerView.getContentHeight();
                switch (ev.getAction()) {
                    //用户按下
                    case MotionEvent.ACTION_DOWN:
                        //如果当前是在listview顶部并且没有记录y坐标
                        if (getFirstVisiblePosition() == 0 && !isRecord) {
                            //将isRecord置为true，说明现在已记录y坐标
                            isRecord = true;
                            //将当前y坐标赋值给startY起始y坐标
                            startY = ev.getY();
                        }
                        break;
                    //用户滑动
                    case MotionEvent.ACTION_MOVE:
                        //再次得到y坐标，用来和startY相减来计算offsetY位移值
                        float tempY = ev.getY();
                        //再起判断一下是否为listview顶部并且没有记录y坐标
                        if (getFirstVisiblePosition() == 0 && !isRecord) {
                            isRecord = true;
                            startY = tempY;
                        }
                        //如果当前状态不是正在刷新的状态，并且已经记录了y坐标
                        if (refreshState != RefreshState.REFRESHING && isRecord) {

                            offsetY = tempY - startY;
                            //计算当前滑动的高度
                            float currentHeight = (-headViewHeight + offsetY / 3);
                            //用当前滑动的高度和头部headerView的总高度进行比 计算出当前滑动的百分比 0到1
                            float currentProgress = 1 + currentHeight / headViewHeight;
                            //如果当前百分比大于1了，将其设置为1，目的是让第一个状态的椭圆不再继续变大
                            currentProgress = currentProgress > 1 ? 1 : currentProgress;

                            //如果当前的状态是放开刷新，并且已经记录y坐标
                            if (refreshState == RefreshState.RELEASE_TO_REFRESH && isRecord) {

                                setSelection(0);
                                //如果当前滑动的距离小于headerView的总高度
                                if (-headViewHeight + offsetY / RATIO < 0) {
                                    //将状态置为下拉刷新状态
                                    changeState(RefreshState.PULL_TO_REFRESH);
                                }
                                //如果当前y的位移值小于0，即为headerView隐藏了
                                else if (offsetY <= 0) {
                                    //将状态变为done
                                    changeState(RefreshState.DONE);
                                }
                            }
                            //如果当前状态为下拉刷新并且已经记录y坐标
                            if (refreshState == RefreshState.PULL_TO_REFRESH && isRecord) {
                                setSelection(0);
                                //如果下拉距离大于等于headerView的总高度
                                if (-headViewHeight + offsetY / RATIO >= 0) {
                                    //将状态变为放开刷新
                                    changeState(RefreshState.RELEASE_TO_REFRESH);
                                }
                                //如果当前y的位移值小于0，即为headerView隐藏了
                                else if (offsetY <= 0) {
                                    //将状态变为done
                                    changeState(RefreshState.DONE);
                                }
                            }
                            //如果当前状态为done并且已经记录y坐标
                            if (refreshState == RefreshState.DONE && isRecord) {
                                //如果位移值大于0
                                if (offsetY >= 0) {
                                    //将状态改为下拉刷新状态
                                    changeState(RefreshState.PULL_TO_REFRESH);
                                }
                            }
                            //如果为下拉刷新状态
                            if (refreshState == RefreshState.PULL_TO_REFRESH) {
                                //则改变headerView的padding来实现下拉的效果
                                ((View) headerView).setPadding(0, (int) (-headViewHeight + offsetY / RATIO), 0, 0);
                            }
                            //如果为放开刷新状态
                            if (refreshState == RefreshState.RELEASE_TO_REFRESH) {
                                //改变headerView的padding值
                                ((View) headerView).setPadding(0, (int) (-headViewHeight + offsetY / RATIO), 0, 0);
                            }
                        }
                        break;
                    //当用户手指抬起时
                    case MotionEvent.ACTION_UP:
                        //如果当前状态为下拉刷新状态
                        if (refreshState == RefreshState.PULL_TO_REFRESH) {
                            //平滑的隐藏headerView
                            this.smoothScrollBy((int) (-headViewHeight + offsetY / RATIO) + headViewHeight, 500);
                            //根据状态改变headerView
                            changeState(RefreshState.PULL_TO_REFRESH);
                        }
                        //如果当前状态为放开刷新
                        if (refreshState == RefreshState.RELEASE_TO_REFRESH) {
                            //平滑的滑到正好显示headerView
                            this.smoothScrollBy((int) (-headViewHeight + offsetY / RATIO), 500);
                            //回调接口的onRefresh方法
                            mOnRefreshListener.onRefresh();
                            //根据状态改变headerView
                            changeState(RefreshState.RELEASE_TO_REFRESH);
                        }
                        //这一套手势执行完，一定别忘了将记录y坐标的isRecord改为false，以便于下一次手势的执行
                        isRecord = false;
                        break;
                }
            }
        }
        return super.onTouchEvent(ev);
    }

    private void changeState(RefreshState state) {
        switch (state) {
            case DONE:
                headerView.onReset(state);
                break;
            case PULL_TO_REFRESH:
                headerView.onPullToRefresh(state);
                break;
            case RELEASE_TO_REFRESH:
                headerView.onReleaseToRefresh(state);
                break;
            case REFRESHING:
                headerView.onRefreshing(state);
                if (mOnRefreshListener != null) mOnRefreshListener.onRefresh();
                break;
            default:
                break;
        }
    }
}

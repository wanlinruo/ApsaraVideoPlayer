package com.aliyun.alivcsolution;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aliyun.alivcsolution.adapter.HomeViewPagerAdapter;
import com.aliyun.alivcsolution.adapter.MultilayerGridAdapter;
import com.aliyun.alivcsolution.model.ScenesModel;
import com.aliyun.alivcsolution.utils.PermissionUtils;
import com.aliyun.svideo.vodupload.AliyunVodUploadMainActivity;
import com.aliyun.vodplayerview.activity.AliyunPlayerSkinActivity;

/**
 * @author Mulberry
 */
public class MainActivity extends AppCompatActivity {

    /**
     * 小圆点指示器
     */
    private ViewGroup points;
    /**
     * 小圆点图片集合
     */
    private ImageView[] ivPoints;
    private ViewPager viewPager;
    /**
     * 当前页数
     */
    private int currentPage;
    /**
     * 总的页数
     */
    private int totalPage;
    /**
     * 每页显示的最大数量
     */
    private int mPageSize = 6;
    /**
     * 总的数据源
     */
    private List<ScenesModel> listDatas;
    /**
     * GridView作为一个View对象添加到ViewPager集合中
     */
    private List<View> viewPagerList;
    /**
     * module数据，播放器模块暂时只有两个
     */
    private int[] modules = new int[]{R.string.solution_upload, R.string.solution_player};

    String[] permission = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solution_main);
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, permission);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
        }
        iniViews();
        setDatas();

        buildHomeItem();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了
                //Toast.makeText(this, "get All Permisison", Toast.LENGTH_SHORT).show();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                showPermissionDialog();
            }
        }
    }

    //系统授权设置的弹框
    AlertDialog openAppDetDialog = null;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name) + "需要访问 \"外部存储器读写权限\",否则会影响视频下载的功能使用, 请到 \"应用信息 -> 权限\" 中设置！");
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("暂不设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        if (null == openAppDetDialog) {
            openAppDetDialog = builder.create();
        }
        if (null != openAppDetDialog && !openAppDetDialog.isShowing()) {
            openAppDetDialog.show();
        }
    }

    private void iniViews() {
        viewPager = (ViewPager) findViewById(R.id.home_viewPager);
        points = (ViewGroup) findViewById(R.id.points);
    }

    private void setDatas() {
        listDatas = new ArrayList<>();
        for (int i = 0; i < modules.length; i++) {
            switch (i) {
                case 0:
                    listDatas.add(new ScenesModel(getResources().getString(modules[i]), R.mipmap.icon_home_upload));
                    break;
                case 1:
                    listDatas.add(new ScenesModel(getResources().getString(modules[i]), R.mipmap.icon_home_player));
                    break;
            }
        }
    }

    private void buildHomeItem() {
        LayoutInflater inflater = LayoutInflater.from(this);
        totalPage = (int) Math.ceil(listDatas.size() * 1.0 / mPageSize);
        viewPagerList = new ArrayList<>();


        for (int i = 0; i < totalPage; i++) {
            //每个页面都是inflate出一个新实例
            GridView gridView = (GridView) inflater.inflate(R.layout.alivc_home_girdview, viewPager, false);
            gridView.setAdapter(new MultilayerGridAdapter(this, listDatas, i, mPageSize));
            //添加item点击监听
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent;
                    switch (position) {
                        case 0:
                            //视频上传
                            intent = new Intent(MainActivity.this, AliyunVodUploadMainActivity.class);
                            startActivity(intent);
                            break;
                        case 1:
                            //视频播放
                            intent = new Intent(MainActivity.this, AliyunPlayerSkinActivity.class);
                            startActivity(intent);
                            break;
                    }
                    int pos = position + currentPage * mPageSize;
                    Log.i("TAG", "position的值为：" + position + "-->pos的值为：" + pos);
                    Toast.makeText(MainActivity.this, "你点击了 " + listDatas.get(pos).getName(), Toast.LENGTH_SHORT).show();
                }
            });
            //每一个GridView作为一个View对象添加到ViewPager集合中
            viewPagerList.add(gridView);
        }

        //设置ViewPager适配器
        viewPager.setAdapter(new HomeViewPagerAdapter(viewPagerList));

        //小圆点指示器
        if (totalPage > 1) {
            ivPoints = new ImageView[totalPage];
            for (int i = 0; i < ivPoints.length; i++) {
                ImageView imageView = new ImageView(this);
                //设置图片的宽高
                imageView.setLayoutParams(new ViewGroup.LayoutParams(10, 10));
                if (i == 0) {
                    imageView.setBackgroundResource(R.mipmap.page_selected_indicator);
                } else {
                    imageView.setBackgroundResource(R.mipmap.page_normal_indicator);
                }
                ivPoints[i] = imageView;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.app_home_points_item_margin);//设置点点点view的左边距
                layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.app_home_points_item_margin);
                ;//设置点点点view的右边距
                points.addView(imageView, layoutParams);
            }
            points.setVisibility(View.VISIBLE);
        } else {
            points.setVisibility(View.GONE);
        }


        //设置ViewPager滑动监听
        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //改变小圆圈指示器的切换效果
                setImageBackground(position);
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setImageBackground(int selectItems) {
        for (int i = 0; i < ivPoints.length; i++) {
            if (i == selectItems) {
                ivPoints[i].setBackgroundResource(R.mipmap.page_selected_indicator);
            } else {
                ivPoints[i].setBackgroundResource(R.mipmap.page_normal_indicator);
            }
        }
    }

}

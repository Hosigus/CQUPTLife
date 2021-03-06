package com.superbug.moi.cquptlife.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.superbug.moi.cquptlife.R;
import com.superbug.moi.cquptlife.app.BaseActivity;
import com.superbug.moi.cquptlife.config.API;
import com.superbug.moi.cquptlife.model.bean.Life;
import com.superbug.moi.cquptlife.util.SPUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class LifeInfoActivity extends BaseActivity {

    private final int CET = 0;
    private final int NORMAL = 1;
    @Bind(R.id.tv_name)
    TextView tvName;
    @Bind(R.id.tv_id)
    TextView tvId;
    @Bind(R.id.tv_grade)
    TextView tvInfo1;
    @Bind(R.id.tv_faculty)
    TextView tvInfo4;
    @Bind(R.id.tv_major)
    TextView tvInfo3;
    @Bind(R.id.tv_class)
    TextView tvInfo2;
    @Bind(R.id.iv_pic)
    SimpleDraweeView mImageView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    private int type = CET;

    private Life life;
    private boolean hasCET;
    private boolean hasNORMAL;

    public static void jump(Context context, Life life) {
        Intent intent = new Intent(context, LifeInfoActivity.class);
        intent.putExtra("life", life);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_life_info);
        ButterKnife.bind(this);

        life = (Life) getIntent().getSerializableExtra("life");

        initToolbar();
        initPic();
        initInfo();
    }

    void initInfo() {
        tvName.setText(life.getName());
        tvId.setText(life.getId());
        tvInfo1.setText(life.getInfo1());
        tvInfo2.setText(life.getInfo2());
        tvInfo3.setText(life.getInfo3());
        tvInfo4.setText(life.getInfo4());
    }

    private void initPic() {

        String id = life.getId();

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());
        ProgressBarDrawable progressBarDrawable = new ProgressBarDrawable();
        progressBarDrawable.setColor(ContextCompat.getColor(this, R.color.blue_primary_color));
        progressBarDrawable.setBarWidth(4);
        progressBarDrawable.setPadding(0);
        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(300)
                .setFailureImage(ContextCompat.getDrawable(this, R.drawable.error))
                .setPlaceholderImage(ContextCompat.getDrawable(this, R.drawable.loading))
                .setProgressBarImage(progressBarDrawable)
                .build();
        mImageView.setHierarchy(hierarchy);
        showPic(id);
    }

    private void showPic(String id) {
        if (id.equals("2014213790") || id.equals("2015210408")) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> mImageView.setImageURI(Uri.parse("res://com.superbug.moi.cquptlife/" + R.drawable.error)), 700);
            return;
        }

        hasCET = (boolean) SPUtils.get(this, "hasCET", false);
        hasNORMAL = (boolean) SPUtils.get(this, "hasTEC", false);
        if (type == CET) {
            if (hasCET) {
                mImageView.setImageURI(Uri.parse(API.URL.studentCETPic + id + API.URL.studentCETPicEnd));
            } else {
                mImageView.setImageURI(Uri.parse("res://com.superbug.moi.cquptlife/" + R.drawable.ic_pic_no_use));
            }
        } else {
            if (hasNORMAL) {
                mImageView.setImageURI(Uri.parse(API.URL.studentPic + id));
            } else {
                mImageView.setImageURI(Uri.parse("res://com.superbug.moi.cquptlife/" + R.drawable.ic_pic_no_use));
            }
        }
    }

    private void initToolbar() {
        if (life.getType() == Life.Type.STUDENT) {
            mToolbar.setTitle(" " + getResources().getString(R.string.student_info));
        } else {
            mToolbar.setTitle(" " + getResources().getString(R.string.teacher_info));
        }
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_back));
        mToolbar.setNavigationOnClickListener(v -> finish());
        mToolbar.setOnMenuItemClickListener(new OnMenuItemClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_info, menu);
        return true;
    }

    @OnClick(R.id.iv_pic)
    void onPicClick() {
        if (type == CET && !hasCET) {
            showVerifyDialog(CET);
        } else if (type == NORMAL && !hasNORMAL) {
            showVerifyDialog(NORMAL);
        }
    }

    @OnLongClick(R.id.iv_pic)
    boolean onPicLongClick() {
        if ((type == CET && hasCET) || (type == NORMAL && hasNORMAL))
            Snackbar.make(mToolbar, "孩子，你是想下载么→_→", Snackbar.LENGTH_LONG).show();
        return true;
    }

    /* 显示验证dialog */
    private void showVerifyDialog(int type) {
        String apiKey = type == CET ? API.KEY.CET_PIC_KEY : API.KEY.NORMAL_PIC_KEY;
        String spKey = type == CET ? "hasCET" : "hasTEC";
        new MaterialDialog.Builder(this)
                .title("请输入密钥")
                .content("没有密钥的请贿赂管理员~")
                .input("密钥", null, (materialDialog, charSequence) -> {
                    String input = charSequence.toString();
                    if (input.equals(apiKey)) {
                        Snackbar.make(mToolbar, "OK~", Snackbar.LENGTH_LONG).show();
                        SPUtils.put(LifeInfoActivity.this, spKey, true);
                        showPic(life.getId());
                    } else {
                        Snackbar.make(mToolbar, "没有密钥的不要乱试", Snackbar.LENGTH_LONG)
                                .setAction("好吧", null)
                                .setActionTextColor(ContextCompat.getColor(this, R.color.accent_color))
                                .show();
                    }
                })
                .theme(Theme.LIGHT)
                .show();
    }

    private class OnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_switch:
                    type = type == CET ? NORMAL : CET;
                    showPic(life.getId());
            }
            return true;
        }
    }
}

package com.fuicui.gitdroid.gitdroid.favorite;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.fuicui.gitdroid.gitdroid.R;
import com.fuicui.gitdroid.gitdroid.favorite.dao.DBHelp;
import com.fuicui.gitdroid.gitdroid.favorite.dao.LocalRepoDao;
import com.fuicui.gitdroid.gitdroid.favorite.dao.RepoGroupDao;
import com.fuicui.gitdroid.gitdroid.favorite.model.LocalRepo;
import com.fuicui.gitdroid.gitdroid.favorite.model.RepoGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteFragment extends Fragment implements PopupMenu.OnMenuItemClickListener{

    @BindView(R.id.tvGroupType)
    TextView tvGroupType;
    @BindView(R.id.btnFilter)
    ImageButton btnFilter;
    @BindView(R.id.listView)
    ListView listView;

    private RepoGroupDao mRepoGroupDao;
    private LocalRepoDao mLocalRepoDao;
    private favoriteAdapter mAdapter;
    private int mItemId;
    private LocalRepo mCurrentLocalRepo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRepoGroupDao=new RepoGroupDao(DBHelp.getInstance(getContext()));
        mLocalRepoDao=new LocalRepoDao(DBHelp.getInstance(getContext()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_favorite,container,false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);

        mAdapter = new favoriteAdapter();
        listView.setAdapter(mAdapter);

        //默认加载的是全部
        setData(R.id.repo_group_all);

        //注册上下文菜单
        registerForContextMenu(listView);
    }
    @OnClick(R.id.btnFilter)
    public void showPopupMenu(View view){
        PopupMenu popupMenu = new PopupMenu(getContext(),view);

        //给PopupMenu填充本地Menu
        popupMenu.inflate(R.menu.menu_popup_repo_groups);
        popupMenu.setOnMenuItemClickListener(this);
        //我们自己在类别表里面其他的分类，怎么进行填充
        /**
         * 1.拿到Menu
         * 2.读取数据库的数据
         * 3.数据填充到menu上
         */
        Menu menu = popupMenu.getMenu();
        List<RepoGroup> repoGroups = mRepoGroupDao.queryForAll();
        for (RepoGroup repo:repoGroups) {
            menu.add(Menu.NONE,repo.getId(),Menu.NONE,repo.getName());
        }
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        /**
         * 1.改变标题
         * 2.数据改变
         */
        tvGroupType.setText(item.getTitle().toString());
        mItemId = item.getItemId();
        setData(mItemId);
        return true;
    }

    private void setData(int groupId) {
        switch (groupId){
            case R.id.repo_group_all:
                mAdapter.setDatas(mLocalRepoDao.queryAll());
                break;
            case R.id.repo_group_no:
                mAdapter.setDatas(mLocalRepoDao.queryNoGroup());
                break;
            default:
                mAdapter.setDatas(mLocalRepoDao.queryForId(mItemId));
                break;
        }
    }

    /**
     * 上下文菜单  ContextMenu
     * 1. 表明我们作用到谁的身上----作用到ListView上
     * 2. 创建出来上下文菜单---移动至，删除
     * 3. 监听点击的菜单哪一项
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()== R.id.listView){
            /**
             * 使用ContextMenuInfo实现类完成我们选择的仓库的获取，
             * position  利用adapter来获取我们当前作用的仓库
             */

            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo= (AdapterView.AdapterContextMenuInfo) menuInfo;
            int position=adapterContextMenuInfo.position;

            //当前操作的仓库
            mCurrentLocalRepo = mAdapter.getItem(position);


            //将Menu填充到上下文菜单
            MenuInflater menuInflater=getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.menu_context_favorite,menu);
            //将我们数据库类别表里面的类别数据填充到移动至的子菜单里面
            //得到子菜单
            SubMenu subMenu=menu.findItem(R.id.sub_menu_move).getSubMenu();
            List<RepoGroup> repoGroups=mRepoGroupDao.queryForAll();

            //利用增强for循环去添加子菜单
            for (RepoGroup repo:repoGroups) {
                subMenu.add(R.id.menu_group_move,repo.getId(),Menu.NONE,repo.getName());
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int id=item.getItemId();

        //点击的是删除
        if (id== R.id.delete){
            //删除作用的仓库
            mLocalRepoDao.delete(mCurrentLocalRepo);
            setData(mItemId);
            return true;
        }
        int groupId=item.getGroupId();
        if (groupId==R.id.menu_group_move){
            //移动的操作--未分类，网络连接。。。。（数据库表里面获得的)
            if (id==R.id.repo_group_no){
                //将我们的作用的仓库类别改为未分类，也就是类别为null
                mCurrentLocalRepo.setRepoGroup(null);
            }else {
                //得到我们点击的是哪一个类别，将我们当前的仓库类别改为当前点击的类别
                RepoGroup repoGroup = mRepoGroupDao.queryForId(id);
                mCurrentLocalRepo.setRepoGroup(repoGroup);
            }
//            数据库的更新
            mLocalRepoDao.createOrUpdate(mCurrentLocalRepo);
            setData(mItemId);
            return true;
        }

        return super.onContextItemSelected(item);
    }
}

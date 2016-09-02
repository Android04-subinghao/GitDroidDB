package com.fuicui.gitdroid.gitdroid.favorite.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.fuicui.gitdroid.gitdroid.favorite.model.LocalRepo;
import com.fuicui.gitdroid.gitdroid.favorite.model.RepoGroup;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Administrator on 2016/8/31.
 */
public class DBHelp extends OrmLiteSqliteOpenHelper{

    public static final String DB_NAME="repo_favorite.db";
    public static final int VERSION=2;

    private static DBHelp dbHelp;
    private Context mContext;

    public static synchronized DBHelp getInstance(Context context){
        if (dbHelp==null){
            dbHelp=new DBHelp(context.getApplicationContext());
        }
        return dbHelp;
    }

    public DBHelp(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        //对表进行创建
        try {
            //创建类别表（单纯的创建出来，里面是空的，没有数据）
            TableUtils.createTableIfNotExists(connectionSource, RepoGroup.class);
            TableUtils.createTableIfNotExists(connectionSource, LocalRepo.class);
            //将本地的数据填充到数据库表中
            new LocalRepoDao(this).createOrUpdate(LocalRepo.getDefaultLocalRepo(mContext));
            new RepoGroupDao(this).createOrUpdate(RepoGroup.getDefaultGroup(mContext));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        //对表进行更新---方法：先删除，再创建
        try {
            TableUtils.dropTable(connectionSource,RepoGroup.class,true);
            TableUtils.dropTable(connectionSource,LocalRepo.class,true);
            onCreate(database,connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

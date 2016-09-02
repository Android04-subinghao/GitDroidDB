package com.fuicui.gitdroid.gitdroid.favorite.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */

@DatabaseTable(tableName = "repostiory_group")
public class RepoGroup {
    private static InputStream sInputStream;
    /**
     * id : 1
     * name : 网络连接
     */
    //主键--不能重复，
    @DatabaseField(id=true)
    private int id;

    @DatabaseField(columnName = "NAME")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private static List<RepoGroup> sRepoGroupList;

    public static List<RepoGroup> getDefaultGroup(Context context) {
        if (sRepoGroupList!=null){
            return sRepoGroupList;
        }
        try {
            sInputStream = context.getAssets().open("repogroup.json");
            String content= IOUtils.toString(sInputStream);
            Gson gson=new Gson();
            sRepoGroupList=gson.fromJson(content,new TypeToken<List<RepoGroup>>(){}.getType());
            return sRepoGroupList;
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }
}

package com.mikkipastel.live500px.dao;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhotoItemCollectionDao {
    @SerializedName("success")
    private boolean success;
    @SerializedName("data")
    private List<PhotoItemDao> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<PhotoItemDao> getData() {
        return data;
    }

    public void setData(List<PhotoItemDao> data) {
        this.data = data;
    }
}

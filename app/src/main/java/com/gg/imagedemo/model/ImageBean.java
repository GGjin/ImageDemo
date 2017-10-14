package com.gg.imagedemo.model;

/**
 * Creator : GG
 * Time    : 2017/10/13
 * Mail    : gg.jin.yu@gmail.com
 * Explain :
 */

public class ImageBean {
    private String path;
    private String name;
    private long time;

    public ImageBean(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageBean imageBean = (ImageBean) o;

        if (path != null ? !path.equals(imageBean.path) : imageBean.path != null) return false;
        return name != null ? name.equals(imageBean.name) : imageBean.name == null;

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}

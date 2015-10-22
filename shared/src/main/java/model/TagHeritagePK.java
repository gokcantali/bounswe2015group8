package model;

import java.io.Serializable;

/**
 * Created by xyllan on 22.10.2015.
 */
public class TagHeritagePK implements Serializable {
    private long tagId;
    private long heritageId;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public long getHeritageId() {
        return heritageId;
    }

    public void setHeritageId(long heritageId) {
        this.heritageId = heritageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagHeritagePK that = (TagHeritagePK) o;

        if (tagId != that.tagId) return false;
        if (heritageId != that.heritageId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (tagId ^ (tagId >>> 32));
        result = 31 * result + (int) (heritageId ^ (heritageId >>> 32));
        return result;
    }
}
